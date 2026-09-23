import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';

const QUESTIONS_PER_TOPIC = 15;
const OPTIONS_PER_QUESTION = 4;
const MIN_WORDS_PER_TOPIC = 8;

function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function pickLevel(words) {
  const counts = {};
  for (const w of words) counts[w.level] = (counts[w.level] || 0) + 1;
  return Object.entries(counts).sort((a, b) => b[1] - a[1])[0]?.[0] || 'INTERMEDIATE';
}

/** Xay 1 cau hoi trac nghiem: chon nghia VI dung cua `target`, 3 nhieu (distractor)
 *  lay tu nghia VI cua cac tu khac cung chu de (khac roi thi khong lap lai). */
function buildQuestion(target, pool) {
  const distractorPool = shuffle(pool.filter((w) => w.word !== target.word && w.meaningVi !== target.meaningVi));
  const distractors = distractorPool.slice(0, OPTIONS_PER_QUESTION - 1);
  const options = shuffle([
    { content: target.meaningVi, correct: true },
    ...distractors.map((d) => ({ content: d.meaningVi, correct: false })),
  ]);
  return {
    kind: 'SINGLE_CHOICE',
    skill: 'VOCABULARY',
    content: `Từ "${target.word}" (${target.partOfSpeechEn || ''}) có nghĩa là gì?`,
    explanation: `"${target.word}": ${target.meaningEn || target.definition || ''}`.trim(),
    options,
    acceptedAnswers: [],
  };
}

/** Sinh bai tap trac nghiem tu vung theo tung chu de, hoan toan tu du lieu words.json
 *  da co (khong goi Gemini): moi chu de du >= MIN_WORDS_PER_TOPIC tu thi tao 1 de. */
export async function generateExercises() {
  const words = await readJsonIfExists(path.join(config.outputDir, 'words.json'));
  const topics = await readJsonIfExists(path.join(config.outputDir, 'topics.json'));
  if (!words || !topics) {
    throw new Error('Thieu words.json/topics.json trong seed/real - chay tools/crawler/src/index.js (13.2) truoc.');
  }

  const byTopic = new Map(topics.map((t) => [t.slug, []]));
  for (const w of words) {
    if (byTopic.has(w.topicSlug)) byTopic.get(w.topicSlug).push(w);
  }

  const exams = [];
  for (const topic of topics) {
    const pool = byTopic.get(topic.slug) || [];
    if (pool.length < MIN_WORDS_PER_TOPIC) continue;
    const targets = shuffle(pool).slice(0, Math.min(QUESTIONS_PER_TOPIC, pool.length));
    exams.push({
      status: 'ACTIVE',
      titleVi: `Từ vựng: ${topic.nameVi}`,
      titleEn: `Vocabulary: ${topic.nameEn}`,
      descriptionVi: `${targets.length} câu trắc nghiệm chọn nghĩa đúng cho từ vựng chủ đề "${topic.nameVi}".`,
      descriptionEn: `${targets.length} multiple-choice questions to match the right meaning for "${topic.nameEn}" vocabulary.`,
      level: pickLevel(pool),
      timeLimitMinutes: Math.max(10, Math.round(targets.length * 1.5)),
      questions: targets.map((t) => buildQuestion(t, pool)),
    });
  }

  await writeJson(path.join(config.outputDir, 'exercises.json'), exams);
  return exams;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  generateExercises().then((exams) => {
    console.log(`[exercises] da ghi ${exams.length} de, tong ${exams.reduce((s, e) => s + e.questions.length, 0)} cau hoi.`);
  }).catch((err) => {
    console.error('\n[loi]', err.message);
    process.exitCode = 1;
  });
}
