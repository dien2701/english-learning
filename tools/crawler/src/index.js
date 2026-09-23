import { config } from './config.js';
import { ensureDir, writeJson } from './cache.js';
import { getWordList, sampleEvenly } from './wordSource.js';
import { fetchWord } from './dictionaryClient.js';
import { translateBatch, hashWordList } from './geminiClient.js';
import { TOPICS } from './topics.js';
import path from 'node:path';
import { readdir } from 'node:fs/promises';
import { readJsonIfExists } from './cache.js';

function chunk(arr, size) {
  const out = [];
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size));
  return out;
}

/**
 * Gom moi ban dich da cache (moi runHash cu) thanh map theo tu, de doi so tu
 * (vd 1.500 -> 800) khong phai dich lai tu da co (22/09/2026).
 */
async function loadCachedTranslations() {
  const root = path.join(config.cacheDir, 'gemini');
  const byWord = new Map();
  const runs = await readdir(root).catch(() => []);
  for (const run of runs) {
    const files = await readdir(path.join(root, run)).catch(() => []);
    for (const file of files.filter((f) => f.startsWith('batch-'))) {
      const entries = (await readJsonIfExists(path.join(root, run, file))) || [];
      for (const e of entries) if (e?.word && e.meaningVi) byWord.set(e.word, e);
    }
  }
  return byWord;
}

/** Chay worker cho tung phan tu voi toi da `limit` tac vu song song, giu ket qua dung thu tu. */
async function mapWithConcurrency(items, limit, worker) {
  const results = new Array(items.length);
  let next = 0;
  async function run() {
    while (next < items.length) {
      const current = next++;
      results[current] = await worker(items[current], current);
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, run));
  return results;
}

async function main() {
  const startedAt = Date.now();
  await ensureDir(config.outputDir);

  await writeJson(path.join(config.outputDir, 'topics.json'), TOPICS);
  console.log(`[topics] da ghi ${TOPICS.length} chu de.`);

  const { raw, cleaned, sampled } = await getWordList();
  console.log(`[words] Oxford goc: ${raw.length}, sau loc: ${cleaned.length}, lay mau: ${sampled.length}`);

  const skippedPath = path.join(config.cacheDir, 'skipped-words.json');
  const skipped = [];
  const dictionaryResults = [];
  for (let i = 0; i < sampled.length; i++) {
    const word = sampled[i];
    const result = await fetchWord(word);
    if (result.status === 'ok') {
      dictionaryResults.push({ word, ...result.data });
    } else {
      skipped.push(word);
    }
    if ((i + 1) % 100 === 0 || i === sampled.length - 1) {
      console.log(`[dictionary] ${i + 1}/${sampled.length} (tim thay ${dictionaryResults.length}, bo qua ${skipped.length})`);
    }
  }
  if (skipped.length) await writeJson(skippedPath, skipped);

  // Lay mau deu A-Z tu cac tu co trong tu dien, roi chi dich phan chua co ban dich cache.
  const selected = sampleEvenly(dictionaryResults, config.finalWordCount);
  const cachedTranslations = await loadCachedTranslations();
  const missing = selected.filter((w) => !cachedTranslations.has(w.word));
  console.log(`[words] chon ${selected.length} tu, da co ban dich ${selected.length - missing.length}, can dich them ${missing.length}.`);

  const runHash = hashWordList(missing.map((w) => w.word));
  const batches = chunk(missing, config.geminiBatchSize);
  let doneBatches = 0;
  const translatedBatches = await mapWithConcurrency(batches, config.geminiConcurrency, async (batch, b) => {
    const translated = await translateBatch(batch, runHash, b);
    doneBatches++;
    console.log(`[gemini] ${doneBatches}/${batches.length} lo xong.`);
    return translated;
  });
  for (const tr of translatedBatches.flat()) cachedTranslations.set(tr.word, tr);

  const words = [];
  for (const src of selected) {
    const tr = cachedTranslations.get(src.word);
    if (!tr) throw new Error(`Thieu ban dich cho "${src.word}"`);
    words.push({
      word: src.word,
      phonetic: src.phonetic,
      partOfSpeechEn: src.partOfSpeech,
      partOfSpeechVi: tr.partOfSpeechVi,
      meaningEn: src.definition,
      meaningVi: tr.meaningVi,
      example: src.example,
      exampleMeaning: tr.exampleMeaning,
      topicSlug: tr.topicSlug,
      level: tr.level,
    });
  }

  await writeJson(path.join(config.outputDir, 'words.json'), words);

  const elapsedSec = Math.round((Date.now() - startedAt) / 1000);
  console.log(
    `\nXong trong ${elapsedSec}s. Ghi ${words.length} tu vao ${path.join(config.outputDir, 'words.json')}.` +
      (skipped.length ? ` Bo qua ${skipped.length} tu khong co trong Dictionary API (xem .cache/skipped-words.json).` : ''),
  );
}

main().catch((err) => {
  console.error('\n[loi]', err.message);
  process.exitCode = 1;
});
