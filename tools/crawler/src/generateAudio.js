import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { callGeminiCached } from './geminiClient.js';
import { searchSentencesWithAudio, downloadAudio } from './tatoebaClient.js';
import { uploadAudio } from './cloudinaryClient.js';
import { Mp3 } from './mp3.js';
import { TOPICS } from './topics.js';

const SAVE_EVERY = 1; // moi bai phai goi Cloudinary/Gemini -> ghi lai ngay de resume duoc tot nhat

const QUESTIONS_SCHEMA = {
  type: 'ARRAY',
  items: {
    type: 'OBJECT',
    properties: {
      kind: { type: 'STRING', enum: ['SINGLE_CHOICE', 'FILL_BLANK'] },
      content: { type: 'STRING' },
      explanation: { type: 'STRING' },
      options: {
        type: 'ARRAY',
        items: {
          type: 'OBJECT',
          properties: { content: { type: 'STRING' }, correct: { type: 'BOOLEAN' } },
          required: ['content', 'correct'],
        },
      },
      acceptedAnswers: { type: 'ARRAY', items: { type: 'STRING' } },
    },
    required: ['kind', 'content', 'explanation', 'options', 'acceptedAnswers'],
    propertyOrdering: ['kind', 'content', 'explanation', 'options', 'acceptedAnswers'],
  },
};

const QUESTIONS_SYSTEM_PROMPT = `Ban la giao vien tieng Anh soan cau hoi nghe hieu cho ung dung hoc tieng Anh.
Ban se duoc dua mot TRANSCRIPT co san (ghep tu nhieu cau audio nguoi doc thuc, khong the sua). Hay tao dung 5 cau hoi
kiem tra nghe hieu dua CHINH XAC tren transcript nay, moi cau la SINGLE_CHOICE voi 4 "options" (dung 1 "correct": true)
hoac FILL_BLANK voi "options" rong va "acceptedAnswers" la 1-2 dap an chap nhan duoc (chu thuong); "explanation" trich
cau trong transcript chung minh dap an. Tra ve dung 5 phan tu.`;

function cacheFile(kind, key) {
  return path.join(config.cacheDir, 'gemini-audio-questions', kind, `${key}.json`);
}

function outPath(fileName) {
  return path.join(config.outputDir, fileName);
}

function keywordFor(item) {
  const words = (item.titleEn || item.topic || '').match(/[A-Za-z]+/g) || [];
  return words.find((w) => w.length > 3) || words[0] || '';
}

/** Tim it nhat `need` cau tieng Anh co audio, uu tien theo tu khoa cua bai, du thieu thi lay cau chung. */
async function collectSentences(key, keyword, topicNameEn, need) {
  const seen = new Map();
  const tryQuery = async (query, page, cacheSuffix) => {
    if (seen.size >= need) return;
    const rows = await searchSentencesWithAudio(`${key}-${cacheSuffix}-p${page}`, query, page);
    for (const r of rows) {
      if (!seen.has(r.text)) seen.set(r.text, r);
    }
  };

  if (keyword) {
    await tryQuery(keyword, 1, 'kw');
    await tryQuery(keyword, 2, 'kw');
  }
  if (seen.size < need) {
    await tryQuery(topicNameEn, 1, 'topic');
  }
  for (let page = 1; seen.size < need && page <= 4; page++) {
    await tryQuery('', page, 'any');
  }
  return [...seen.values()].slice(0, need);
}

async function generateQuestions(key, transcript) {
  const items = await callGeminiCached(cacheFile('listening', key), QUESTIONS_SYSTEM_PROMPT, `Transcript:\n${transcript}`, QUESTIONS_SCHEMA, 0.5);
  return items.map((q) => ({
    kind: q.kind === 'FILL_BLANK' ? 'FILL_BLANK' : 'SINGLE_CHOICE',
    skill: 'LISTENING',
    content: q.content,
    explanation: q.explanation,
    options: Array.isArray(q.options) ? q.options : [],
    acceptedAnswers: Array.isArray(q.acceptedAnswers) ? q.acceptedAnswers : [],
  }));
}

/**
 * Sinh audio nguoi doc thuc (13.6): thay TTS bang cau tieng Anh co san audio tu Tatoeba.org (mien phi, CC BY),
 * ghep nhieu cau thanh 1 bai nghe, viet lai transcript (noi dung cau thuc) + cau hoi (Gemini, dua tren transcript
 * moi), roi tai audio da ghep len Cloudinary (giong CloudinaryAudioStorage.java o BE). Chay theo tung bai, ghi lai
 * listening.json ngay sau moi bai de resume duoc khi dung giua chung.
 */
export async function generateAudio() {
  const fileName = 'listening.json';
  const items = await readJsonIfExists(outPath(fileName));
  if (!items) throw new Error('Thieu listening.json - chay src/generateLessons.js (13.3) truoc.');
  const topicNameByslug = new Map(TOPICS.map((t) => [t.slug, t.nameEn]));

  const indexInTopic = new Map();
  let processed = 0;
  for (const item of items) {
    const idx = indexInTopic.get(item.topic) || 0;
    indexInTopic.set(item.topic, idx + 1);
    if (item.audioUrl !== undefined) continue;

    const key = `${item.topic}-${idx}`;
    console.log(`[audio] ${key}: tim cau co audio...`);
    const sentences = await collectSentences(key, keywordFor(item), topicNameByslug.get(item.topic) || item.topic, config.audioSentencesPerLesson);
    if (sentences.length < 3) {
      console.warn(`[audio] ${key}: chi tim duoc ${sentences.length} cau co audio, bo qua lan nay (chay lai sau).`);
      continue;
    }

    console.log(`[audio] ${key}: tai ${sentences.length} file audio...`);
    const buffers = [];
    for (const s of sentences) buffers.push(await downloadAudio(s));
    const joined = Mp3.join(buffers);

    console.log(`[audio] ${key}: tai len Cloudinary...`);
    const uploaded = await uploadAudio(joined.data, key);

    const transcript = sentences.map((s) => s.text).join('\n');
    console.log(`[audio] ${key}: sinh lai cau hoi nghe hieu theo transcript moi...`);
    const questions = await generateQuestions(key, transcript);

    item.transcript = transcript;
    item.questions = questions;
    item.audioUrl = uploaded.url;
    item.audioPublicId = uploaded.publicId;
    item.durationSeconds = Math.max(1, Math.round(joined.seconds));
    item.audioCredit = [...new Set(sentences.map((s) => s.author))].join(', ');

    processed++;
    console.log(`[audio] ${key}: xong (${processed} bai trong lan chay nay).`);
    if (processed % SAVE_EVERY === 0) await writeJson(outPath(fileName), items);
  }
  await writeJson(outPath(fileName), items);
  console.log(`[audio] ${items.filter((i) => i.audioUrl).length}/${items.length} bai co audio thuc.`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  generateAudio()
    .then(() => console.log('\nXong 13.6.'))
    .catch((err) => {
      console.error('\n[loi]', err.message);
      process.exitCode = 1;
    });
}
