import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { callGeminiCached } from './geminiClient.js';
import { searchImageCached } from './imageClient.js';
import { uploadAudio } from './cloudinaryClient.js';
import { Mp3 } from './mp3.js';
import { listZoneArticles, fetchArticleHtml, parseArticle, checkAudioAlive, downloadMp3, slugify } from './voaClient.js';

// 1 lan goi Gemini/bai (dung quyet dinh dot 13) - phien dau tach lam 2 lan vi nghi schema long qua
// sau, nhung nguyen nhan thuc su la model gemini-3.5-flash-lite (cu trong .env) khong ho tro
// responseSchema; da doi sang gemini-3.6-flash (ho tro day du) nen gop lai 1 lan goi de bot quota
// (23/09/2026, phien 13h - free tier het quota ngay chi sau vai bai).
const LESSON_SCHEMA = {
  type: 'OBJECT',
  properties: {
    titleVi: { type: 'STRING' },
    descriptionVi: { type: 'STRING' },
    descriptionEn: { type: 'STRING' },
    questions: {
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
    },
  },
  required: ['titleVi', 'descriptionVi', 'descriptionEn', 'questions'],
  propertyOrdering: ['titleVi', 'descriptionVi', 'descriptionEn', 'questions'],
};

const SYSTEM_PROMPT = `Ban la bien tap vien noi dung hoc tieng Anh, dang chuyen 1 bai VOA Learning English co san
thanh bai luyen nghe cho ung dung. Ban duoc dua TRANSCRIPT GOC (khong duoc sua noi dung, day la ban ghi am thuc)
va TEN BAI tieng Anh goc. Hay tra ve JSON dung schema:
- titleVi: dich ten bai sang tieng Viet, tu nhien, ngan gon.
- descriptionVi/descriptionEn: 1-2 cau MOI mo ta noi dung bai (dung hien thi trong danh sach bai nghe), khong
  chep lai nguyen van cau nao trong transcript.
- questions: dung 5 cau hoi kiem tra nghe hieu dua CHINH XAC tren transcript, moi cau la SINGLE_CHOICE voi 4
  "options" (dung 1 "correct": true) hoac FILL_BLANK voi "options" rong va "acceptedAnswers" la 1-2 dap an chap
  nhan duoc (chu thuong); "explanation" trich cau trong transcript chung minh dap an.`;

function outPath(fileName) {
  return path.join(config.outputDir, fileName);
}

function geminiCacheFile(key) {
  return path.join(config.cacheDir, 'gemini-listening-voa', `${key}.json`);
}

async function buildLesson(article, topicSlug) {
  const key = slugify(article.titleEn);
  const userText = `Ten bai: ${article.titleEn}\n\nTranscript:\n${article.transcript}`;

  const gemini = await callGeminiCached(geminiCacheFile(key), SYSTEM_PROMPT, userText, LESSON_SCHEMA, 0.5);

  console.log(`[voa] ${key}: tai mp3 goc...`);
  const audioBuffer = await downloadMp3(article.mp3Url);
  const { seconds } = Mp3.parse(audioBuffer);

  console.log(`[voa] ${key}: tai len Cloudinary...`);
  const uploaded = await uploadAudio(audioBuffer, `voa-${key}`);

  console.log(`[voa] ${key}: tim anh minh hoa...`);
  const image = await searchImageCached('listening-voa', key, article.titleEn);

  return {
    topic: topicSlug,
    level: 'INTERMEDIATE',
    titleVi: gemini.titleVi,
    titleEn: article.titleEn,
    descriptionVi: gemini.descriptionVi,
    descriptionEn: gemini.descriptionEn,
    transcript: article.transcript,
    questions: gemini.questions.map((q) => ({
      kind: q.kind === 'FILL_BLANK' ? 'FILL_BLANK' : 'SINGLE_CHOICE',
      skill: 'LISTENING',
      content: q.content,
      explanation: q.explanation,
      options: Array.isArray(q.options) ? q.options : [],
      acceptedAnswers: Array.isArray(q.acceptedAnswers) ? q.acceptedAnswers : [],
    })),
    imageUrl: image?.imageUrl || null,
    imageAuthor: image?.imageAuthor || null,
    imageAuthorUrl: image?.imageAuthorUrl || null,
    audioUrl: uploaded.url,
    audioPublicId: uploaded.publicId,
    durationSeconds: Math.max(1, Math.round(seconds)),
    audioCredit: 'VOA Learning English (Voice of America) - public domain',
  };
}

/**
 * Cao 20 bai nghe thuc tu VOA Learning English (13h): duyet tung zone trong config.voaZones
 * (chuong trinh tren chinh trang hien tai learningenglish.voanews.com - xem voaClient.js), bo qua
 * bai thieu transcript/mp3 hoac mp3 da chet (HEAD/GET qua voaClient.checkAudioAlive), nhan bai
 * dau tien con hop le trong moi zone. Tieu de/mo ta tieng Viet va 5 cau hoi trac nghiem sinh bang
 * 1 lan goi Gemini/bai (co cache, xem ghi chu o LESSON_SCHEMA).
 * Dung lai khi du targetListeningCount bai; ghi listening.json
 * ngay sau moi bai de resume duoc khi dung giua chung.
 */
export async function generateListening() {
  const fileName = 'listening.json';
  const items = (await readJsonIfExists(outPath(fileName))) || [];
  const doneTitles = new Set(items.map((i) => i.titleEn));

  for (const { id: zoneId, topicSlug } of config.voaZones) {
    if (items.length >= config.targetListeningCount) break;
    console.log(`[voa] zone ${zoneId}: lay danh sach bai...`);
    const articles = await listZoneArticles(zoneId);

    for (const { url } of articles) {
      if (items.length >= config.targetListeningCount) break;

      let html;
      try {
        html = await fetchArticleHtml(url);
      } catch (err) {
        console.warn(`[voa] "${url}": loi tai trang (${err.message}), bo qua.`);
        continue;
      }

      const parsed = parseArticle(html);
      if (!parsed || parsed.transcript.length < 200) continue;
      if (doneTitles.has(parsed.titleEn)) continue;

      const alive = await checkAudioAlive(parsed.mp3Url);
      if (!alive) {
        console.warn(`[voa] "${parsed.titleEn}": mp3 da chet, bo qua.`);
        continue;
      }

      console.log(`[voa] nhan bai "${parsed.titleEn}" (zone ${zoneId})...`);
      try {
        const lesson = await buildLesson(parsed, topicSlug);
        items.push(lesson);
        doneTitles.add(parsed.titleEn);
        await writeJson(outPath(fileName), items);
        console.log(`[voa] ${items.length}/${config.targetListeningCount} bai.`);
      } catch (err) {
        console.warn(`[voa] "${parsed.titleEn}": loi khi xu ly (${err.message}), bo qua.`);
      }
    }
  }

  await writeJson(outPath(fileName), items);
  if (items.length < config.targetListeningCount) {
    console.warn(`[voa] chi thu thap duoc ${items.length}/${config.targetListeningCount} bai, chay lai lenh de tiep tuc (het zone thi them zoneId moi vao config.voaZones).`);
  } else {
    console.log(`[voa] du ${items.length} bai.`);
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  generateListening()
    .then(() => console.log('\nXong 13h.'))
    .catch((err) => {
      console.error('\n[loi]', err.message);
      process.exitCode = 1;
    });
}
