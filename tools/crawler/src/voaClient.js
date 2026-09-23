import crypto from 'node:crypto';
import path from 'node:path';
import fs from 'node:fs/promises';
import { config } from './config.js';
import { readJsonIfExists, writeJson, ensureDir, sleep } from './cache.js';

/**
 * Bai VOA Learning English THAT tren chinh trang hien tai (learningenglish.voanews.com, xem
 * config.js/voaZones). Moi trang bai: <h1 class="title pg-title"> ten bai, khung phat nhac
 * (".c-mmp__fallback-link" la link mp3 du phong tren CDN voa-audio.voanews.eu), phan noi dung
 * chinh nam trong div class "wsw" - bat dau bang chinh khung phat nhac (EMBED/SHARE/dong ho/",
 * "Direct link") roi moi den transcript that, ket bang dai gach duoi "___" (tu vung "Words in
 * This Story") hoac "Share"/"This item is part of" (dieu huong lien quan). Khong dung thu vien
 * parse HTML ngoai (dung fetch + regex/strip tag don gian, giong triet ly cac client khac).
 */

function hash(text) {
  return crypto.createHash('sha1').update(text).digest('hex').slice(0, 16);
}

/** Rut gon ten bai thanh khoa on dinh, dung lam ten cache/Cloudinary public_id. */
export function slugify(text) {
  return String(text)
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60);
}

const NAMED_ENTITIES = {
  amp: '&', lt: '<', gt: '>', quot: '"', apos: "'", nbsp: ' ',
  mdash: '—', ndash: '–', hellip: '…',
  lsquo: '‘', rsquo: '’', ldquo: '“', rdquo: '”',
};

function decodeEntities(text) {
  return text.replace(/&(#x?[0-9a-f]+|[a-z]+);/gi, (m, body) => {
    if (body[0] === '#') {
      const code = body[1].toLowerCase() === 'x' ? parseInt(body.slice(2), 16) : parseInt(body.slice(1), 10);
      if (!Number.isFinite(code) || code > 0x10ffff || (code >= 0xd800 && code <= 0xdfff)) return m;
      return String.fromCodePoint(code);
    }
    const key = body.toLowerCase();
    return NAMED_ENTITIES[key] ?? m;
  });
}

/**
 * Bo ky tu dieu khien (tru \n, \t) va surrogate le (tu entity so hong nhu &#55357; bi cat doi cap,
 * hoac loi encode cua trang goc) truoc khi gui cho Gemini - HTTP 400 "invalid argument" chung khong
 * kem chi tiet field nao sai, nen don dep phong ngua thay vi doan schema (23/09/2026, phien 13h).
 */
function sanitizeText(text) {
  return text
    .replace(/[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]/g, '')
    .replace(/[\ud800-\udbff](?![\udc00-\udfff])/g, '') // high surrogate khong co low di kem
    .replace(/(?<![\ud800-\udbff])[\udc00-\udfff]/g, '') // low surrogate khong co high di truoc
    .normalize('NFC');
}

/** Chuyen 1 doan HTML thanh cac dong text (khong tag), giu ranh gioi dong theo the block. */
function htmlToLines(html) {
  let s = html.replace(/<script[\s\S]*?<\/script>/gi, '\n');
  s = s.replace(/<style[\s\S]*?<\/style>/gi, '\n');
  s = s.replace(/<(br|\/p|\/div|\/li|\/h[1-6]|\/figcaption)\s*\/?>/gi, '\n');
  s = s.replace(/<[^>]+>/g, ' ');
  s = decodeEntities(s);
  return s
    .split('\n')
    .map((line) => line.replace(/\s+/g, ' ').trim())
    .filter(Boolean);
}

/** Cat rieng khoi noi dung chinh (div co class chua tenClass), tranh dinh vao sidebar/footer. */
function extractDivByClass(html, className) {
  const re = new RegExp(`<div[^>]*class="[^"]*\\b${className}\\b[^"]*"[^>]*>`, 'i');
  const startMatch = html.match(re);
  if (!startMatch) return null;
  let depth = 1;
  let pos = startMatch.index + startMatch[0].length;
  const openRe = /<div\b/gi;
  const closeRe = /<\/div>/gi;
  while (depth > 0 && pos < html.length) {
    openRe.lastIndex = pos;
    closeRe.lastIndex = pos;
    const nextOpen = openRe.exec(html);
    const nextClose = closeRe.exec(html);
    if (!nextClose) break;
    if (nextOpen && nextOpen.index < nextClose.index) {
      depth++;
      pos = nextOpen.index + nextOpen[0].length;
    } else {
      depth--;
      pos = nextClose.index + nextClose[0].length;
      if (depth === 0) return html.slice(startMatch.index + startMatch[0].length, nextClose.index);
    }
  }
  return null;
}

const STOP_MARKERS = [/^_{3,}$/, /^Words in This Story$/i, /^Share$/i, /^This item is part of$/i];

/** Phan tich 1 trang bai VOA hien tai: tra { titleEn, transcript, mp3Url } hoac null neu thieu du lieu. */
export function parseArticle(html) {
  const titleMatch = html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i);
  if (!titleMatch) return null;
  const titleEn = sanitizeText(decodeEntities(titleMatch[1].replace(/<[^>]+>/g, ' '))).replace(/\s+/g, ' ').trim();
  if (!titleEn) return null;

  const mp3Match = html.match(/c-mmp__fallback-link"[^>]*href="([^"]+\.mp3)"/i);
  if (!mp3Match) return null;
  const mp3Url = mp3Match[1];

  const column = extractDivByClass(html, 'wsw');
  if (!column) return null;
  const lines = htmlToLines(column);

  const directLinkIdx = lines.findIndex((line) => /^Direct link$/i.test(line));
  if (directLinkIdx === -1) return null;

  const transcriptLines = [];
  for (const line of lines.slice(directLinkIdx + 1)) {
    if (STOP_MARKERS.some((re) => re.test(line))) break;
    transcriptLines.push(line);
  }
  const transcript = sanitizeText(transcriptLines.join('\n\n')).trim();
  if (!transcript) return null;

  return { titleEn, transcript, mp3Url };
}

async function fetchWithRetry(url, init, maxRetries, delayMs) {
  let lastError;
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await fetch(url, init);
    } catch (err) {
      lastError = err;
      await sleep(delayMs * attempt);
    }
  }
  throw lastError;
}

function listCacheFile(zoneId) {
  return path.join(config.cacheDir, 'voa', 'list', `zone-${zoneId}.json`);
}

/** Danh sach URL bai (tuyet doi) cua 1 zone, phan trang qua /z/<zoneId>?p=<trang>, co cache. */
export async function listZoneArticles(zoneId) {
  const cached = await readJsonIfExists(listCacheFile(zoneId));
  if (cached) return cached;

  const seen = new Set();
  const articles = [];
  for (let page = 1; page <= config.voaMaxListPages; page++) {
    const url = `${config.voaBaseUrl}/z/${zoneId}?p=${page}`;
    const res = await fetchWithRetry(url, { signal: AbortSignal.timeout(config.voaTimeoutMs) }, config.voaMaxRetries, config.voaDelayMs);
    if (!res.ok) break;
    const html = await res.text();

    const hrefRe = /href="(\/a\/[^"]+\.html)"/gi;
    let sizeBefore = seen.size;
    let m;
    while ((m = hrefRe.exec(html))) {
      const href = m[1];
      if (seen.has(href)) continue;
      seen.add(href);
      articles.push({ url: new URL(href, config.voaBaseUrl).toString() });
    }
    await sleep(config.voaDelayMs);
    if (seen.size === sizeBefore) break; // trang khong co bai moi -> het danh sach
  }

  await writeJson(listCacheFile(zoneId), articles);
  return articles;
}

function articleCacheFile(url) {
  return path.join(config.cacheDir, 'voa', 'article', `${hash(url)}.html`);
}

/** HTML tho cua 1 trang bai, cache theo URL de khong tai lai khi resume. */
export async function fetchArticleHtml(url) {
  const file = articleCacheFile(url);
  try {
    return await fs.readFile(file, 'utf8');
  } catch (err) {
    if (err.code !== 'ENOENT') throw err;
  }
  const res = await fetchWithRetry(url, { signal: AbortSignal.timeout(config.voaTimeoutMs) }, config.voaMaxRetries, config.voaDelayMs);
  if (!res.ok) throw new Error(`VOA bai "${url}" HTTP ${res.status}`);
  const html = await res.text();
  await ensureDir(path.dirname(file));
  await fs.writeFile(file, html, 'utf8');
  await sleep(config.voaDelayMs);
  return html;
}

function audioCheckCacheFile(mp3Url) {
  return path.join(config.cacheDir, 'voa', 'audio-check', `${hash(mp3Url)}.json`);
}

async function probe(mp3Url, method) {
  const init = { method, signal: AbortSignal.timeout(config.voaAudioCheckTimeoutMs) };
  if (method === 'GET') init.headers = { Range: 'bytes=0-1023' };
  return fetch(mp3Url, init);
}

/**
 * Kiem tra mp3 con song hay khong: thu HEAD truoc (nhanh), CDN nao khong cho HEAD (405/403 hoac
 * loi mang) thi thu lai bang GET voi Range toi thieu. Chi cache ket qua khi nhan duoc PHAN HOI
 * HTTP thuc su (ke ca 404) tu it nhat 1 trong 2 cach - loi mang/timeout ca 2 cach thi KHONG cache,
 * de lan chay sau thu lai (giong imageClient.js, tranh cache oan 1 loi mang tam thoi thanh "chet").
 */
export async function checkAudioAlive(mp3Url) {
  const file = audioCheckCacheFile(mp3Url);
  const cached = await readJsonIfExists(file);
  if (cached !== undefined) return cached.ok;

  for (const method of ['HEAD', 'GET']) {
    try {
      const res = await probe(mp3Url, method);
      await writeJson(file, { ok: res.ok });
      await sleep(config.voaDelayMs);
      return res.ok;
    } catch {
      // thu cach con lai truoc khi bo cuoc
    }
  }
  console.warn(`[voa] khong kiem tra duoc "${mp3Url}" (loi mang ca HEAD va GET), thu lai lan sau.`);
  return false;
}

function audioCacheFile(mp3Url) {
  return path.join(config.cacheDir, 'voa', 'audio', `${hash(mp3Url)}.mp3`);
}

/** Tai (co cache) noi dung mp3 goc ve Buffer. */
export async function downloadMp3(mp3Url) {
  const file = audioCacheFile(mp3Url);
  try {
    return await fs.readFile(file);
  } catch (err) {
    if (err.code !== 'ENOENT') throw err;
  }
  const res = await fetchWithRetry(
    mp3Url,
    { signal: AbortSignal.timeout(config.voaAudioDownloadTimeoutMs) },
    config.voaMaxRetries,
    config.voaDelayMs,
  );
  if (!res.ok) throw new Error(`Tai mp3 VOA loi HTTP ${res.status}: ${mp3Url}`);
  const data = Buffer.from(await res.arrayBuffer());
  await ensureDir(path.dirname(file));
  await fs.writeFile(file, data);
  return data;
}
