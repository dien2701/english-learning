import path from 'node:path';
import { readdir } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { sampleEvenly } from './wordSource.js';
import { fetchWord } from './dictionaryClient.js';

/**
 * Gom moi ban dich Gemini da cache (cac lan chay truoc) thanh map theo tu.
 * Giong loadCachedTranslations() trong index.js, tach rieng de selectWords.js
 * khong phai import ca pipeline dich/tu dien moi.
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

/**
 * 13g: chon lai tu vung tu cache da co (dich Gemini + tu dien tu cac lan chay 13.2 truoc),
 * KHONG goi Gemini hay Dictionary API qua mang. Cat con 500 tu (--words=N) trai deu A-Z
 * tu tap da co ban dich san.
 */
async function main() {
  const cachedTranslations = await loadCachedTranslations();
  const candidates = [...cachedTranslations.keys()].sort();
  console.log(`[words] co ${candidates.length} tu da dich san trong cache.`);
  if (!candidates.length) {
    throw new Error('Chua co ban dich nao trong .cache/gemini/ - khong the chon tu ma khong goi Gemini.');
  }

  const selected = sampleEvenly(candidates, config.finalWordCount);
  console.log(`[words] lay mau ${selected.length} tu (muc tieu ${config.finalWordCount}).`);

  const words = [];
  const missingDictionary = [];
  for (const word of selected) {
    const tr = cachedTranslations.get(word);
    const dict = await fetchWord(word);
    if (dict.status !== 'ok') {
      missingDictionary.push(word);
      continue;
    }
    words.push({
      word,
      phonetic: dict.data.phonetic,
      partOfSpeechEn: dict.data.partOfSpeech,
      partOfSpeechVi: tr.partOfSpeechVi,
      meaningEn: dict.data.definition,
      meaningVi: tr.meaningVi,
      example: dict.data.example,
      exampleMeaning: tr.exampleMeaning,
      topicSlug: tr.topicSlug,
      level: tr.level,
    });
  }
  if (missingDictionary.length) {
    console.warn(`[words] ${missingDictionary.length} tu thieu cache tu dien, bo qua: ${missingDictionary.join(', ')}`);
  }

  await writeJson(path.join(config.outputDir, 'words.json'), words);
  console.log(`\nXong. Ghi ${words.length} tu vao ${path.join(config.outputDir, 'words.json')}.`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main().catch((err) => {
    console.error('\n[loi]', err.message);
    process.exitCode = 1;
  });
}
