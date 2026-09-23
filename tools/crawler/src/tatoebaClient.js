import path from 'node:path';
import fs from 'node:fs/promises';
import { config } from './config.js';
import { readJsonIfExists, writeJson, ensureDir, sleep } from './cache.js';

function searchCacheFile(key) {
  return path.join(config.cacheDir, 'tatoeba', 'search', `${key}.json`);
}

function audioCacheFile(audioId) {
  return path.join(config.cacheDir, 'tatoeba', 'audio', `${audioId}.mp3`);
}

async function searchOnce(query, page) {
  const params = new URLSearchParams({ from: 'eng', has_audio: 'yes', perPage: '50', page: String(page) });
  if (query) params.set('query', query);
  const url = `${config.tatoebaSearchUrl}?${params.toString()}`;
  const res = await fetch(url, { signal: AbortSignal.timeout(config.tatoebaTimeoutMs) });
  if (!res.ok) throw new Error(`Tatoeba HTTP ${res.status}`);
  const json = await res.json();
  return Array.isArray(json.results) ? json.results : [];
}

/**
 * Tim cau tieng Anh co audio nguoi doc thuc theo tu khoa (cache theo key on dinh de resume duoc).
 * Tra ve mang { text, audioId, downloadUrl, author }, chi giu cau dai 15-130 ky tu (loai cau qua ngan/dai).
 */
export async function searchSentencesWithAudio(key, query, page = 1) {
  const file = searchCacheFile(key);
  const cached = await readJsonIfExists(file);
  if (cached) return cached;

  let lastError;
  for (let attempt = 1; attempt <= config.tatoebaMaxRetries; attempt++) {
    try {
      const rows = await searchOnce(query, page);
      const result = rows
        .filter((r) => r.lang === 'eng' && Array.isArray(r.audios) && r.audios.length > 0)
        .filter((r) => r.text.length >= config.audioSentenceMinLen && r.text.length <= config.audioSentenceMaxLen)
        .map((r) => ({
          text: r.text,
          audioId: r.audios[0].id,
          downloadUrl: `${config.tatoebaAudioBaseUrl}${r.audios[0].download_url}`,
          author: r.audios[0].author || 'anonymous',
        }));
      await writeJson(file, result);
      await sleep(config.tatoebaDelayMs);
      return result;
    } catch (err) {
      lastError = err;
      await sleep(config.tatoebaDelayMs * attempt);
    }
  }
  throw new Error(`Tatoeba loi lien tiep (${key}): ${lastError?.message}`);
}

/** Tai (co cache) mot file audio ve Buffer mp3. */
export async function downloadAudio(sentence) {
  const file = audioCacheFile(sentence.audioId);
  try {
    return await fs.readFile(file);
  } catch (err) {
    if (err.code !== 'ENOENT') throw err;
  }
  let lastError;
  for (let attempt = 1; attempt <= config.tatoebaMaxRetries; attempt++) {
    try {
      const res = await fetch(sentence.downloadUrl, { signal: AbortSignal.timeout(config.tatoebaTimeoutMs) });
      if (!res.ok) throw new Error(`Tatoeba audio HTTP ${res.status}`);
      const data = Buffer.from(await res.arrayBuffer());
      await ensureDir(path.dirname(file));
      await fs.writeFile(file, data);
      await sleep(config.tatoebaDelayMs);
      return data;
    } catch (err) {
      lastError = err;
      await sleep(config.tatoebaDelayMs * attempt);
    }
  }
  throw new Error(`Tai audio Tatoeba loi (id ${sentence.audioId}): ${lastError?.message}`);
}
