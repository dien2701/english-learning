import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { writeJson } from './cache.js';
import { callGeminiCached } from './geminiClient.js';
import { TOPICS } from './topics.js';

const LEVELS = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
const WRITING_PER_TOPIC = 1;
const SPEAKING_PER_TOPIC = 1;
const LISTENING_PER_TOPIC = 1; // 20 chu de x 1 = 20 bai nghe (giam tu 40, 22/09/2026)

const LEVEL_PROP = { type: 'STRING', enum: LEVELS };

function cacheFile(kind, topicSlug) {
  return path.join(config.cacheDir, 'gemini-lessons', kind, `${topicSlug}.json`);
}

// ---------------------------------------------------------------------------
// Viet (writing.json)
// ---------------------------------------------------------------------------

const WRITING_SCHEMA = {
  type: 'ARRAY',
  items: {
    type: 'OBJECT',
    properties: {
      level: LEVEL_PROP,
      titleVi: { type: 'STRING' },
      titleEn: { type: 'STRING' },
      instructions: { type: 'STRING' },
      suggestedMinutes: { type: 'INTEGER' },
      minWords: { type: 'INTEGER' },
      hints: { type: 'ARRAY', items: { type: 'STRING' } },
    },
    required: ['level', 'titleVi', 'titleEn', 'instructions', 'suggestedMinutes', 'minWords', 'hints'],
    propertyOrdering: ['level', 'titleVi', 'titleEn', 'instructions', 'suggestedMinutes', 'minWords', 'hints'],
  },
};

const WRITING_SYSTEM_PROMPT = `Ban la giao vien tieng Anh soan de bai Luyen viet cho ung dung hoc tieng Anh.
Voi chu de duoc cung cap, hay tao ${WRITING_PER_TOPIC} de bai khac nhau, do kho tang dan (uu tien BEGINNER va INTERMEDIATE,
chi dung ADVANCED neu chu de phu hop). Moi de gom:
- titleVi/titleEn: tieu de ngan gon.
- instructions: yeu cau day du BANG TIENG ANH (giong de thi that), neu ro do dai toi thieu.
- suggestedMinutes: thoi gian goi y (15-40).
- minWords: so tu toi thieu (40-250 tuy do kho).
- hints: 3 gach dau dong goi y bang TIENG ANH giup nguoi hoc trien khai bai viet.
Tra ve dung ${WRITING_PER_TOPIC} phan tu.`;

async function generateWriting() {
  const results = [];
  for (const topic of TOPICS) {
    const userText = `Chu de: ${topic.nameVi} / ${topic.nameEn} (slug: ${topic.slug}).`;
    const items = await callGeminiCached(
      cacheFile('writing', topic.slug),
      WRITING_SYSTEM_PROMPT,
      userText,
      WRITING_SCHEMA,
      0.6,
    );
    for (const item of items) {
      results.push({
        status: 'ACTIVE',
        topic: topic.slug,
        level: LEVELS.includes(item.level) ? item.level : 'INTERMEDIATE',
        suggestedMinutes: item.suggestedMinutes || 20,
        minWords: item.minWords || 60,
        titleVi: item.titleVi,
        titleEn: item.titleEn,
        instructions: item.instructions,
        hints: Array.isArray(item.hints) ? item.hints : [],
      });
    }
    console.log(`[writing] ${topic.slug} xong (${results.length} de bai tich luy).`);
  }
  return results;
}

// ---------------------------------------------------------------------------
// Noi (speaking.json)
// ---------------------------------------------------------------------------

const SPEAKING_SCHEMA = {
  type: 'ARRAY',
  items: {
    type: 'OBJECT',
    properties: {
      level: LEVEL_PROP,
      titleVi: { type: 'STRING' },
      titleEn: { type: 'STRING' },
      descriptionVi: { type: 'STRING' },
      descriptionEn: { type: 'STRING' },
      prompts: {
        type: 'ARRAY',
        items: {
          type: 'OBJECT',
          properties: {
            text: { type: 'STRING' },
            phonetic: { type: 'STRING' },
            meaningVi: { type: 'STRING' },
          },
          required: ['text', 'meaningVi'],
        },
      },
    },
    required: ['level', 'titleVi', 'titleEn', 'descriptionVi', 'descriptionEn', 'prompts'],
    propertyOrdering: ['level', 'titleVi', 'titleEn', 'descriptionVi', 'descriptionEn', 'prompts'],
  },
};

const SPEAKING_SYSTEM_PROMPT = `Ban la giao vien tieng Anh soan bai Luyen noi cho ung dung hoc tieng Anh.
Voi chu de duoc cung cap, hay tao ${SPEAKING_PER_TOPIC} bai, moi bai gom dung 5 cau (prompts) de nguoi hoc doc to,
do kho tu nhien theo chu de (BEGINNER neu la cau chao hoi/gioi thieu don gian, cao hon neu cau phuc tap hon). Moi bai gom:
- titleVi/titleEn, descriptionVi/descriptionEn: ngan gon.
- prompts: 5 cau, "text" la cau tieng Anh tu nhien lien quan chu de, "phonetic" de trong neu khong chac (chuoi rong),
  "meaningVi" la nghia tieng Viet cua ca cau.
Tra ve dung ${SPEAKING_PER_TOPIC} phan tu.`;

async function generateSpeaking() {
  const results = [];
  for (const topic of TOPICS) {
    const userText = `Chu de: ${topic.nameVi} / ${topic.nameEn} (slug: ${topic.slug}).`;
    const items = await callGeminiCached(
      cacheFile('speaking', topic.slug),
      SPEAKING_SYSTEM_PROMPT,
      userText,
      SPEAKING_SCHEMA,
      0.6,
    );
    for (const item of items) {
      results.push({
        status: 'ACTIVE',
        topic: topic.slug,
        level: LEVELS.includes(item.level) ? item.level : 'BEGINNER',
        titleVi: item.titleVi,
        titleEn: item.titleEn,
        descriptionVi: item.descriptionVi,
        descriptionEn: item.descriptionEn,
        prompts: (item.prompts || []).map((p) => ({
          text: p.text,
          phonetic: p.phonetic || null,
          meaningVi: p.meaningVi,
        })),
      });
    }
    console.log(`[speaking] ${topic.slug} xong (${results.length} bai tich luy).`);
  }
  return results;
}

// ---------------------------------------------------------------------------
// Nghe (listening.json)
// ---------------------------------------------------------------------------

const LISTENING_SCHEMA = {
  type: 'ARRAY',
  items: {
    type: 'OBJECT',
    properties: {
      level: LEVEL_PROP,
      titleVi: { type: 'STRING' },
      titleEn: { type: 'STRING' },
      descriptionVi: { type: 'STRING' },
      descriptionEn: { type: 'STRING' },
      transcript: { type: 'STRING' },
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
        },
      },
    },
    required: ['level', 'titleVi', 'titleEn', 'descriptionVi', 'descriptionEn', 'transcript', 'questions'],
    propertyOrdering: ['level', 'titleVi', 'titleEn', 'descriptionVi', 'descriptionEn', 'transcript', 'questions'],
  },
};

const LISTENING_SYSTEM_PROMPT = `Ban la giao vien tieng Anh soan bai Luyen nghe cho ung dung hoc tieng Anh.
Voi chu de duoc cung cap, hay tao ${LISTENING_PER_TOPIC} bai nghe khac nhau (mot bai muc BEGINNER/INTERMEDIATE de hon,
mot bai muc INTERMEDIATE/ADVANCED kho hon). Moi bai gom:
- titleVi/titleEn, descriptionVi/descriptionEn: ngan gon.
- transcript: doan hoi thoai hoac doc hieu BANG TIENG ANH, tu nhien, 6-14 luot noi hoac 80-160 tu, dung boi canh
  doi thoai ro nguoi noi (vd "Agent:", "Minh:") khi la hoi thoai.
- questions: 5 cau hoi kiem tra nghe hieu dua CHINH XAC tren transcript, moi cau la SINGLE_CHOICE voi 4 "options"
  (dung 1 "correct": true) hoac FILL_BLANK voi "options" rong va "acceptedAnswers" la 1-2 dap an chap nhan duoc
  (chu thuong); "explanation" trich cau trong transcript chung minh dap an.
Tra ve dung ${LISTENING_PER_TOPIC} phan tu.`;

async function generateListening() {
  const results = [];
  for (const topic of TOPICS) {
    const userText = `Chu de: ${topic.nameVi} / ${topic.nameEn} (slug: ${topic.slug}).`;
    const items = await callGeminiCached(
      cacheFile('listening', topic.slug),
      LISTENING_SYSTEM_PROMPT,
      userText,
      LISTENING_SCHEMA,
      0.6,
    );
    for (const item of items) {
      results.push({
        status: 'ACTIVE',
        topic: topic.slug,
        level: LEVELS.includes(item.level) ? item.level : 'INTERMEDIATE',
        titleVi: item.titleVi,
        titleEn: item.titleEn,
        descriptionVi: item.descriptionVi,
        descriptionEn: item.descriptionEn,
        transcript: item.transcript,
        questions: (item.questions || []).map((q) => ({
          kind: q.kind === 'FILL_BLANK' ? 'FILL_BLANK' : 'SINGLE_CHOICE',
          skill: 'LISTENING',
          content: q.content,
          explanation: q.explanation,
          options: Array.isArray(q.options) ? q.options : [],
          acceptedAnswers: Array.isArray(q.acceptedAnswers) ? q.acceptedAnswers : [],
        })),
      });
    }
    console.log(`[listening] ${topic.slug} xong (${results.length} bai tich luy).`);
  }
  return results;
}

// ---------------------------------------------------------------------------

export async function generateLessons() {
  const writing = await generateWriting();
  await writeJson(path.join(config.outputDir, 'writing.json'), writing);

  const speaking = await generateSpeaking();
  await writeJson(path.join(config.outputDir, 'speaking.json'), speaking);

  const listening = await generateListening();
  await writeJson(path.join(config.outputDir, 'listening.json'), listening);

  return { writing, speaking, listening };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  generateLessons().then(({ writing, speaking, listening }) => {
    console.log(`\nXong. writing=${writing.length}, speaking=${speaking.length}, listening=${listening.length}.`);
  }).catch((err) => {
    console.error('\n[loi]', err.message);
    process.exitCode = 1;
  });
}
