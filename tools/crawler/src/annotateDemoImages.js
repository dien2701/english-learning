import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { searchImageCached } from './imageClient.js';

// seed/ (bai demo dang dung, KHAC seed/real/ cua du lieu that 13.2-13.6).
const demoSeedDir = path.resolve(config.outputDir, '..');

// Tu khoa tong quat du phong (giong generateImages.js) cho de tru tuong khong tim duoc anh dung nghia.
const GENERIC_QUERY = {
  reading: 'reading book library',
  writing: 'writing notebook desk',
  speaking: 'people conversation talking',
};

function demoPath(fileName) {
  return path.join(demoSeedDir, fileName);
}

function applyResult(item, result) {
  item.imageUrl = result?.imageUrl || null;
  item.imageAuthor = result?.imageAuthor || null;
  item.imageAuthorUrl = result?.imageAuthorUrl || null;
}

/**
 * Bai Doc/Viet/Noi demo dang dung o app (seed/reading.json, writing.json, speaking.json)
 * co dang { topic, titleEn, ... }, khong co id rieng -> khoa cache theo topic + vi tri
 * trong topic, giong annotateLessons() trong generateImages.js (13.4) nhung cho file demo.
 */
async function annotateDemoLessons(fileName, kind) {
  const filePath = demoPath(fileName);
  const items = await readJsonIfExists(filePath);
  if (!items) throw new Error(`Thieu ${filePath}`);

  const indexInTopic = new Map();
  for (const item of items) {
    const idx = indexInTopic.get(item.topic) || 0;
    indexInTopic.set(item.topic, idx + 1);
    if (item.imageUrl) continue;

    const queries = [item.titleEn || item.topic, item.topic, GENERIC_QUERY[kind]];
    const result = await searchImageCached(`demo-${kind}`, `${item.topic}-${idx}`, queries);
    if (result === undefined) continue; // loi tam thoi, lan chay sau thu lai
    applyResult(item, result);
  }
  await writeJson(filePath, items);
  console.log(`[demo-${kind}] ${items.filter((i) => i.imageUrl).length}/${items.length} co anh.`);
}

/**
 * 13g: chi tim anh minh hoa cho 3 file bai seed demo dang dung (KHONG sinh bai moi bang Gemini,
 * giu nguyen noi dung). Dung lai chuoi nguon Pexels -> Openverse -> Wikimedia -> Unsplash
 * (imageClient.js, 13.4) va cache .cache/images/.
 */
export async function annotateDemoImages() {
  await annotateDemoLessons('reading.json', 'reading');
  await annotateDemoLessons('writing.json', 'writing');
  await annotateDemoLessons('speaking.json', 'speaking');
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  annotateDemoImages()
    .then(() => console.log('\nXong 13g (anh cho bai Doc/Viet/Noi seed).'))
    .catch((err) => {
      console.error('\n[loi]', err.message);
      process.exitCode = 1;
    });
}
