import path from 'node:path';
import { config } from './config.js';
import { readJsonIfExists, writeJson } from './cache.js';

/** Tai danh sach Oxford 3000 goc (co cache local trong data/, khong goi lai qua mang). */
async function loadRawList() {
  const localPath = path.join(config.dataDir, config.oxfordListFile);
  const cached = await readJsonIfExists(localPath);
  if (cached) return cached;

  const res = await fetch(config.oxfordListUrl);
  if (!res.ok) throw new Error(`Khong tai duoc danh sach Oxford 3000: HTTP ${res.status}`);
  const list = await res.json();
  await writeJson(localPath, list);
  return list;
}

/**
 * Loc con lai tu don, chu thuong, khong ky tu la (bo cum tu, viet tat, danh tu rieng,
 * so thu tu nghia nhu "close 1"/"close 2"), roi khu trung.
 */
function cleanList(rawList) {
  const seen = new Set();
  const cleaned = [];
  for (const rawEntry of rawList) {
    const entry = String(rawEntry).trim();
    if (/\s/.test(entry)) continue; // cum tu / phrasal verb
    if (!/^[A-Za-z-]+$/.test(entry)) continue; // co dau cham, so...
    if (/^[A-Z]/.test(entry) && entry !== 'I') continue; // danh tu rieng (thang, thu, ten...)
    const word = entry.toLowerCase();
    if (word.length < 2 || seen.has(word)) continue;
    seen.add(word);
    cleaned.push(word);
  }
  return cleaned;
}

/** Lay mau deu theo chi so de khong lech ve dau bang chu cai khi phai cat bot. */
export function sampleEvenly(list, targetCount) {
  if (!targetCount || list.length <= targetCount) return list;
  const result = [];
  for (let i = 0; i < targetCount; i++) {
    result.push(list[Math.floor((i * list.length) / targetCount)]);
  }
  return result;
}

export async function getWordList() {
  const raw = await loadRawList();
  const cleaned = cleanList(raw);
  const sampled = sampleEvenly(cleaned, config.targetWordCount);
  return { raw, cleaned, sampled };
}
