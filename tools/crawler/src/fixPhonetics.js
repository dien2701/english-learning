import fs from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';
import { arpabetToIpa } from './arpabetToIpa.js';

/**
 * Sua loi phien am ARPAbet ("/AE1 K T IH0 V/") do dictionaryClient.js tra ve truoc khi doi
 * sang IPA (23/09/2026, xem CLAUDE.md). Chi doi dinh dang tren du lieu DA CO san trong
 * .cache/dictionary/ va words.json - khong goi lai Datamuse/Gemini/Unsplash.
 */
async function fixCache() {
  const dir = path.join(config.cacheDir, 'dictionary');
  const files = await fs.readdir(dir).catch(() => []);
  let changed = 0;
  for (const file of files) {
    if (!file.endsWith('.json')) continue;
    const filePath = path.join(dir, file);
    const entry = await readJsonIfExists(filePath);
    const phonetic = entry?.data?.phonetic;
    if (!phonetic) continue;
    const fixed = arpabetToIpa(phonetic);
    if (fixed !== phonetic) {
      entry.data.phonetic = fixed;
      await writeJson(filePath, entry);
      changed++;
    }
  }
  console.log(`[cache] da sua phien am cho ${changed}/${files.length} file.`);
}

async function fixWordsJson() {
  const filePath = path.join(config.outputDir, 'words.json');
  const words = await readJsonIfExists(filePath);
  if (!words) {
    console.log('[words.json] khong tim thay, bo qua.');
    return;
  }
  let changed = 0;
  for (const word of words) {
    if (!word.phonetic) continue;
    const fixed = arpabetToIpa(word.phonetic);
    if (fixed !== word.phonetic) {
      word.phonetic = fixed;
      changed++;
    }
  }
  await writeJson(filePath, words);
  console.log(`[words.json] da sua phien am cho ${changed}/${words.length} tu.`);
}

export async function fixPhonetics() {
  await fixCache();
  await fixWordsJson();
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  fixPhonetics()
    .then(() => console.log('\nXong sua phien am.'))
    .catch((err) => {
      console.error('\n[loi]', err.message);
      process.exitCode = 1;
    });
}
