import path from 'node:path';
import { config } from './config.js';
import { readJsonIfExists, writeJson, sleep } from './cache.js';
import { arpabetToIpa } from './arpabetToIpa.js';

function cacheFile(word) {
  return path.join(config.cacheDir, 'dictionary', `${word}.json`);
}

const POS_NAMES = {
  n: 'noun',
  v: 'verb',
  adj: 'adjective',
  adv: 'adverb',
  pron: 'pronoun',
  prep: 'preposition',
  conj: 'conjunction',
  u: 'noun',
};

/** Rut phonetic (ARPAbet tho tu tag "pron:...")/tu loai/dinh nghia dau tien tu mot entry cua Datamuse. */
function pickBestSense(entry) {
  const pronTag = entry.tags?.find((t) => t.startsWith('pron:'));
  const phonetic = pronTag ? arpabetToIpa(`/${pronTag.slice(5).trim()}/`) : '';
  const firstDef = entry.defs?.[0];
  if (!firstDef) return null;
  const tabIndex = firstDef.indexOf('\t');
  const posAbbr = tabIndex >= 0 ? firstDef.slice(0, tabIndex) : '';
  const definition = (tabIndex >= 0 ? firstDef.slice(tabIndex + 1) : firstDef).trim();
  if (!definition) return null;
  return {
    phonetic,
    partOfSpeech: POS_NAMES[posAbbr] || posAbbr,
    definition,
    // Datamuse khong co cau vi du; generate.js/geminiClient.js da xu ly duoc example rong
    // (exampleMeaning de trong khi khong co example tieng Anh dau vao).
    example: '',
  };
}

async function fetchOnce(word) {
  const url = `${config.dictionaryBaseUrl}?sp=${encodeURIComponent(word)}&md=dpr&max=1`;
  const res = await fetch(url, { signal: AbortSignal.timeout(config.dictionaryTimeoutMs) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  const results = await res.json();
  const entry = results.find((r) => r.word?.toLowerCase() === word.toLowerCase()) || results[0];
  if (!entry) return { status: 'not_found' };
  const sense = pickBestSense(entry);
  if (!sense) return { status: 'not_found' };
  return { status: 'ok', data: sense };
}

/**
 * Tra ve { status: 'ok', data } | { status: 'not_found' }. Ket qua dinh doan (ok/not_found)
 * duoc cache vinh vien de resume khong goi lai; loi mang/5xx khong cache, se thu lai lan chay sau.
 */
export async function fetchWord(word) {
  const file = cacheFile(word);
  const cached = await readJsonIfExists(file);
  if (cached) return cached;

  let lastError;
  for (let attempt = 1; attempt <= config.dictionaryMaxRetries; attempt++) {
    try {
      const result = await fetchOnce(word);
      await writeJson(file, result);
      return result;
    } catch (err) {
      lastError = err;
      await sleep(config.dictionaryDelayMs * attempt * 2);
    } finally {
      await sleep(config.dictionaryDelayMs);
    }
  }
  throw new Error(`Khong lay duoc "${word}" tu Dictionary API sau ${config.dictionaryMaxRetries} lan: ${lastError?.message}`);
}
