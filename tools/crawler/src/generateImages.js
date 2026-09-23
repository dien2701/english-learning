import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { searchImageCached } from './imageClient.js';

const SAVE_EVERY = 10; // ghi lai JSON dinh ky de khong mat tien do khi dung giua chung

// Tu khoa tong quat du phong theo loai noi dung, dung khi tu khoa cu the (tieu de bai/tu) khong
// ra anh phu hop (vd de tru tuong) - Openverse/Wikimedia gan nhu luon co anh cho tu khoa chung nay.
const GENERIC_QUERY = {
  topics: 'english vocabulary study',
  words: 'dictionary book',
  listening: 'people listening headphones',
  writing: 'writing notebook desk',
  speaking: 'people conversation talking',
};

function outPath(fileName) {
  return path.join(config.outputDir, fileName);
}

function applyResult(item, result) {
  item.imageUrl = result?.imageUrl || null;
  item.imageAuthor = result?.imageAuthor || null;
  item.imageAuthorUrl = result?.imageAuthorUrl || null;
}

async function annotateTopics() {
  const fileName = 'topics.json';
  const topics = await readJsonIfExists(outPath(fileName));
  if (!topics) throw new Error('Thieu topics.json - chay src/index.js (13.2) truoc.');

  let processed = 0;
  for (const topic of topics) {
    if (topic.imageUrl) continue;
    const result = await searchImageCached('topics', topic.slug, [topic.nameEn, GENERIC_QUERY.topics]);
    if (result === undefined) continue; // loi tam thoi, lan chay sau thu lai
    applyResult(topic, result);
    processed++;
    if (processed % SAVE_EVERY === 0) await writeJson(outPath(fileName), topics);
  }
  await writeJson(outPath(fileName), topics);
  console.log(`[topics] ${topics.filter((t) => t.imageUrl).length}/${topics.length} co anh.`);
}

async function annotateWords() {
  const fileName = 'words.json';
  const words = await readJsonIfExists(outPath(fileName));
  if (!words) throw new Error('Thieu words.json - chay src/index.js (13.2) truoc.');

  let processed = 0;
  for (const word of words) {
    if (word.imageUrl) continue;
    const result = await searchImageCached('words', word.word.toLowerCase(), [word.word, GENERIC_QUERY.words]);
    if (result === undefined) continue; // loi tam thoi, lan chay sau thu lai
    applyResult(word, result);
    processed++;
    if (processed % SAVE_EVERY === 0) {
      await writeJson(outPath(fileName), words);
      console.log(`[words] da xu ly them ${processed} tu trong lan chay nay...`);
    }
  }
  await writeJson(outPath(fileName), words);
  console.log(`[words] ${words.filter((w) => w.imageUrl).length}/${words.length} co anh.`);
}

/** Bai Viet/Noi/Nghe deu co dang { topic, ...noi dung }, khong co id rieng ->
 *  khoa cache theo topic + vi tri trong topic (thu tu sinh tu Gemini on dinh giua cac lan chay). */
async function annotateLessons(fileName, kind) {
  const items = await readJsonIfExists(outPath(fileName));
  if (!items) throw new Error(`Thieu ${fileName} - chay src/generate.js (13.3) truoc.`);

  const indexInTopic = new Map();
  let processed = 0;
  for (const item of items) {
    const idx = indexInTopic.get(item.topic) || 0;
    indexInTopic.set(item.topic, idx + 1);
    if (item.imageUrl) continue;

    const queries = [item.titleEn || item.topic, item.topic, GENERIC_QUERY[kind]];
    const result = await searchImageCached(kind, `${item.topic}-${idx}`, queries);
    if (result === undefined) continue; // loi tam thoi, lan chay sau thu lai
    applyResult(item, result);
    processed++;
    if (processed % SAVE_EVERY === 0) await writeJson(outPath(fileName), items);
  }
  await writeJson(outPath(fileName), items);
  console.log(`[${kind}] ${items.filter((i) => i.imageUrl).length}/${items.length} co anh.`);
}

/**
 * Sinh anh (13.4, nhieu nguon du phong - xem imageClient.js): tim theo tu khoa cho topics/words/listening
 * da co trong seed/real/ (13.2/13.3), ghi imageUrl/imageAuthor/imageAuthorUrl vao dung cac JSON do. Viet/Noi
 * dung bai seed cu (13g, xem annotateDemoImages.js), khong con o day. Chay theo lo 50 req/gio (gioi han
 * Unsplash demo app); dung giua chung roi chay lai lenh cu se resume dung phan con thieu (cache theo
 * kind/key trong .cache/images/, va JSON dau ra cung duoc ghi dinh ky nen item da co imageUrl se duoc bo qua).
 */
export async function generateImages() {
  await annotateTopics();
  await annotateWords();
  await annotateLessons('listening.json', 'listening');
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  generateImages()
    .then(() => console.log('\nXong toan bo 13.4.'))
    .catch((err) => {
      console.error('\n[loi]', err.message);
      process.exitCode = 1;
    });
}
