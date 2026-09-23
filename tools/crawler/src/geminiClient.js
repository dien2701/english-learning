import crypto from 'node:crypto';
import path from 'node:path';
import { config } from './config.js';
import { readJsonIfExists, writeJson, sleep } from './cache.js';
import { TOPIC_SLUGS, TOPICS } from './topics.js';

const LEVELS = new Set(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']);

const RESPONSE_SCHEMA = {
  type: 'ARRAY',
  items: {
    type: 'OBJECT',
    properties: {
      word: { type: 'STRING' },
      meaningVi: { type: 'STRING' },
      partOfSpeechVi: { type: 'STRING' },
      exampleMeaning: { type: 'STRING' },
      topicSlug: { type: 'STRING', enum: [...TOPIC_SLUGS] },
      level: { type: 'STRING', enum: [...LEVELS] },
    },
    required: ['word', 'meaningVi', 'partOfSpeechVi', 'topicSlug', 'level'],
    propertyOrdering: ['word', 'meaningVi', 'partOfSpeechVi', 'exampleMeaning', 'topicSlug', 'level'],
  },
};

const SYSTEM_PROMPT = `Ban la bien tap vien tu dien Anh-Viet cho ung dung hoc tieng Anh.
Voi moi tu duoc dua vao (kem tu loai/dinh nghia/vi du tieng Anh), hay tra ve JSON dung schema:
- meaningVi: nghia tieng Viet ngan gon, tu nhien, dung trong ngu canh do (khong dich may moc tung chu).
- partOfSpeechVi: tu loai dich sang tieng Viet (vd "danh tu", "dong tu", "tinh tu", "trang tu").
- exampleMeaning: dich cau vi du duoc dua vao sang tieng Viet; neu khong co cau vi du thi de chuoi rong.
- topicSlug: chon DUNG MOT slug phu hop nhat trong danh sach chu de duoc cung cap, khong duoc bia slug khac.
- level: do kho cua tu voi nguoi hoc tieng Anh pho thong, chon BEGINNER, INTERMEDIATE hoac ADVANCED.
Tra ve dung so luong phan tu bang so tu dau vao, giu nguyen thu tu va gia tri "word" nhu dau vao.`;

/**
 * Loi 429 (het quota/phut) va 503 (model qua tai tam thoi, "high demand") can cho
 * lau hon han loi thuong truoc khi retry (23/09/2026, phien 13z).
 */
function retryDelayMs(err, attempt) {
  if (err?.status === 429 || err?.status === 503) return config.geminiRateLimitDelayMs * attempt;
  return config.geminiDelayMs * attempt;
}

function cacheFile(runHash, batchIndex) {
  return path.join(config.cacheDir, 'gemini', runHash, `batch-${String(batchIndex).padStart(4, '0')}.json`);
}

export function hashWordList(words) {
  return crypto.createHash('sha1').update(words.join(',')).digest('hex').slice(0, 12);
}

function buildUserText(items) {
  const topicList = TOPICS.map((t) => `${t.slug}: ${t.nameVi} / ${t.nameEn}`).join('\n');
  const wordList = items
    .map(
      (it, i) =>
        `${i + 1}. word="${it.word}" | partOfSpeech="${it.partOfSpeech}" | definition="${it.definition}" | example="${it.example}"`,
    )
    .join('\n');
  return `Danh sach chu de hop le:\n${topicList}\n\nDanh sach tu can xu ly (${items.length} tu):\n${wordList}`;
}

/**
 * Goi thap nhat toi Gemini generateContent, ep JSON dung schema. Dung chung cho
 * moi tac vu sinh noi dung trong tools/crawler (dich tu vung, sinh de Viet/Noi/Nghe).
 */
export async function callGeminiJson(systemPrompt, userText, responseSchema, temperature = 0.5) {
  if (!config.geminiApiKey) {
    throw new Error('Thieu GEMINI_API_KEY trong apps/backend/.env.');
  }
  const url = `${config.geminiBaseUrl}/v1beta/models/${config.geminiModel}:generateContent`;
  const payload = {
    systemInstruction: { parts: [{ text: systemPrompt }] },
    contents: [{ role: 'user', parts: [{ text: userText }] }],
    generationConfig: {
      responseMimeType: 'application/json',
      responseSchema,
      temperature,
      // Tac vu dich/gan nhan don gian, khong can suy luan sau -> tat thinking de giam
      // latency (23/09/2026, phien 13z: gemini-3.5-flash-lite mat ~69s/lo khi bat thinking).
      thinkingConfig: { thinkingBudget: 0 },
    },
  };
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'x-goog-api-key': config.geminiApiKey },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(config.geminiTimeoutMs),
  });
  if (!res.ok) {
    const body = await res.text().catch(() => '');
    const err = new Error(`Gemini HTTP ${res.status}: ${body.slice(0, 300)}`);
    err.status = res.status;
    throw err;
  }
  const json = await res.json();
  const candidate = json.candidates?.[0];
  if (!candidate) throw new Error('Gemini khong tra ung vien nao (co the bi chan noi dung)');
  const text = candidate.content?.parts?.map((p) => p.text || '').join('') || '';
  if (!text) throw new Error('Gemini tra noi dung rong');
  return JSON.parse(text);
}

/**
 * Goi va cache 1 lan, retry theo config.geminiMaxRetries/geminiDelayMs. Dung cho moi generator.
 * In log truoc moi lan goi va khi retry (23/09/2026, phien 13h: cho lai 429/503 im lang toi vai
 * phut khien nguoi dung nghi la script bi treo), de luc nao cung thay dang lam gi.
 */
export async function callGeminiCached(cacheFilePath, systemPrompt, userText, responseSchema, temperature) {
  const cached = await readJsonIfExists(cacheFilePath);
  if (cached) return cached;

  let lastError;
  for (let attempt = 1; attempt <= config.geminiMaxRetries; attempt++) {
    console.log(`[gemini] dang goi (lan ${attempt}/${config.geminiMaxRetries})...`);
    try {
      const result = await callGeminiJson(systemPrompt, userText, responseSchema, temperature);
      await writeJson(cacheFilePath, result);
      await sleep(config.geminiDelayMs);
      return result;
    } catch (err) {
      lastError = err;
      const waitMs = retryDelayMs(err, attempt);
      console.warn(`[gemini] loi (${err.message}), cho ${Math.round(waitMs / 1000)}s roi thu lai...`);
      await sleep(waitMs);
    }
  }
  throw new Error(`Gemini loi lien tiep (${cacheFilePath}): ${lastError?.message}`);
}

async function callOnce(items) {
  const parsed = await callGeminiJson(SYSTEM_PROMPT, buildUserText(items), RESPONSE_SCHEMA, 0.3);
  if (!Array.isArray(parsed) || parsed.length !== items.length) {
    throw new Error(`Gemini tra ${Array.isArray(parsed) ? parsed.length : 'khong phai mang'} phan tu, can ${items.length}`);
  }
  return parsed.map((entry, i) => ({
    word: items[i].word,
    meaningVi: entry.meaningVi || '',
    partOfSpeechVi: entry.partOfSpeechVi || '',
    exampleMeaning: entry.exampleMeaning || '',
    topicSlug: TOPIC_SLUGS.has(entry.topicSlug) ? entry.topicSlug : 'daily-life',
    level: LEVELS.has(entry.level) ? entry.level : 'INTERMEDIATE',
  }));
}

/**
 * Goi 1 lo, retry theo loi tam thoi (429/503). Loi 400 (INVALID_ARGUMENT) co the do noi
 * dung 1 tu (hiem) hoac do loi he thong thoang qua (quota/billing) anh huong ca lo (23/09/2026,
 * phien 13z: 1 lo ~30 tu binh thuong nhu "question", "read" deu bi 400 -> khong phai do tu vung).
 * Tach doi de co lap, nhung gioi han so luong bo qua (config.geminiMaxSkipsPerBatch): vuot nguong
 * la dau hieu loi he thong, dung lai va bao loi ro thay vi am tham dien rong ca chuc tu.
 */
async function translateChunkWithRetry(items, skipTracker) {
  let lastError;
  for (let attempt = 1; attempt <= config.geminiMaxRetries; attempt++) {
    try {
      return await callOnce(items);
    } catch (err) {
      lastError = err;
      // 1 tu ma van 400: co the chi la hen xui tam thoi, cho ngan roi thu lai 1 lan truoc khi bo qua.
      if (err?.status === 400 && items.length > 1) break;
      if (err?.status === 400 && items.length === 1 && attempt >= 2) break;
      await sleep(err?.status === 400 ? config.geminiDelayMs : retryDelayMs(err, attempt));
    }
  }

  if (lastError?.status === 400 && items.length > 1) {
    const mid = Math.ceil(items.length / 2);
    const left = await translateChunkWithRetry(items.slice(0, mid), skipTracker);
    const right = await translateChunkWithRetry(items.slice(mid), skipTracker);
    return [...left, ...right];
  }
  if (lastError?.status === 400 && items.length === 1) {
    skipTracker.count++;
    if (skipTracker.count > config.geminiMaxSkipsPerBatch) {
      throw new Error(
        `Qua ${config.geminiMaxSkipsPerBatch} tu binh thuong lien tiep bi 400 - nghi la loi he thong ` +
          `(quota/billing Gemini) chu khong phai do tu vung, dung lai de kiem tra thay vi bo qua ca lo.`,
      );
    }
    console.warn(`[gemini] Bo qua tu loi 400 (khong dich duoc): "${items[0].word}"`);
    return [
      {
        word: items[0].word,
        meaningVi: '',
        partOfSpeechVi: '',
        exampleMeaning: '',
        topicSlug: 'daily-life',
        level: 'INTERMEDIATE',
      },
    ];
  }
  throw lastError;
}

/**
 * Dich + gan chu de cho mot lo (<= geminiBatchSize) tu vung. Ket qua cache theo
 * runHash (hash toan bo danh sach tu cua lan chay) + chi so lo, cho phep resume:
 * chay lai script se bo qua cac lo da dich neu danh sach tu khong doi.
 */
export async function translateBatch(items, runHash, batchIndex) {
  if (!config.geminiApiKey) {
    throw new Error('Thieu GEMINI_API_KEY trong apps/backend/.env, khong the dich VI/gan chu de.');
  }
  const file = cacheFile(runHash, batchIndex);
  const cached = await readJsonIfExists(file);
  if (cached) return cached;

  try {
    const result = await translateChunkWithRetry(items, { count: 0 });
    await writeJson(file, result);
    await sleep(config.geminiDelayMs);
    return result;
  } catch (err) {
    throw new Error(`Gemini loi lien tiep o lo ${batchIndex}: ${err?.message}`);
  }
}
