import path from 'node:path';
import { config } from './config.js';
import { readJsonIfExists, writeJson, sleep } from './cache.js';
import { searchUnsplash } from './unsplashClient.js';

/**
 * Tim anh qua nhieu nguon du phong (22/09/2026): Pexels -> Openverse -> Wikimedia Commons ->
 * Unsplash. Nguon nao thieu key thi bo qua; nguon het han muc (429) thi tam nghi toi
 * cooldownUntil va chuyen nguon ke. Tat ca nguon deu cho dung URL truc tiep + ghi cong tac gia.
 */

const USER_AGENT = 'EN-Learning-Crawler/1.0 (educational seed data)';

function stripHtml(html) {
  return String(html || '').replace(/<[^>]*>/g, '').trim();
}

function rateLimited(name, status) {
  return Object.assign(new Error(`${name} HTTP ${status} (het han muc)`), { rateLimited: true });
}

async function getJson(name, url, headers = {}) {
  const res = await fetch(url, {
    headers: { 'User-Agent': USER_AGENT, ...headers },
    signal: AbortSignal.timeout(config.imageTimeoutMs),
  });
  if (res.status === 429 || res.status === 403) throw rateLimited(name, res.status);
  if (!res.ok) throw new Error(`${name} HTTP ${res.status}`);
  return res.json();
}

const PROVIDERS = [
  {
    name: 'pexels',
    enabled: () => Boolean(config.pexelsApiKey),
    delayMs: config.pexelsDelayMs,
    async search(query) {
      const url = `https://api.pexels.com/v1/search?query=${encodeURIComponent(query)}&per_page=1&orientation=landscape`;
      const json = await getJson('Pexels', url, { Authorization: config.pexelsApiKey });
      const photo = json.photos?.[0];
      if (!photo) return null;
      return { imageUrl: photo.src?.large || photo.src?.original, imageAuthor: photo.photographer, imageAuthorUrl: photo.photographer_url };
    },
  },
  {
    name: 'openverse',
    enabled: () => true,
    delayMs: config.openverseDelayMs,
    async search(query) {
      const url = `https://api.openverse.org/v1/images/?q=${encodeURIComponent(query)}&page_size=1&license_type=commercial&mature=false`;
      const json = await getJson('Openverse', url);
      const photo = json.results?.[0];
      if (!photo?.url) return null;
      return { imageUrl: photo.url, imageAuthor: photo.creator || photo.source, imageAuthorUrl: photo.creator_url || photo.foreign_landing_url };
    },
  },
  {
    name: 'wikimedia',
    enabled: () => true,
    delayMs: config.wikimediaDelayMs,
    async search(query) {
      const params = new URLSearchParams({
        action: 'query',
        format: 'json',
        generator: 'search',
        gsrsearch: `${query} filetype:bitmap`,
        gsrnamespace: '6',
        gsrlimit: '1',
        prop: 'imageinfo',
        iiprop: 'url|extmetadata',
        iiurlwidth: '1080',
      });
      const json = await getJson('Wikimedia', `https://commons.wikimedia.org/w/api.php?${params}`);
      const page = Object.values(json.query?.pages || {})[0];
      const info = page?.imageinfo?.[0];
      if (!info) return null;
      return {
        imageUrl: info.thumburl || info.url,
        imageAuthor: stripHtml(info.extmetadata?.Artist?.value) || 'Wikimedia Commons',
        imageAuthorUrl: info.descriptionurl,
      };
    },
  },
  {
    name: 'unsplash',
    enabled: () => Boolean(config.unsplashAccessKey),
    delayMs: config.unsplashDelayMs,
    search: searchUnsplash,
  },
];

const cooldownUntil = new Map();

function cacheFile(kind, key) {
  return path.join(config.cacheDir, 'images', kind, `${key}.json`);
}

/**
 * Tim + cache 1 anh theo tu khoa. `queries` la 1 chuoi hoac mang nhieu tu khoa uu tien:
 * thu tu khoa cu the truoc (vd tieu de bai), het nguon van khong ra thi thu tu khoa tong quat
 * hon (vd ten chu de) - dung cho noi dung tru tuong kho tim anh dung nghia. Tra
 * { imageUrl, imageAuthor, imageAuthorUrl, imageSource } hoac null khi MOI tu khoa + MOI nguon
 * deu tra loi "khong co ket qua". Neu co nguon loi/het han muc ma khong nguon nao tim thay thi
 * KHONG cache null, de lan chay sau thu lai.
 */
export async function searchImageCached(kind, key, queries) {
  const file = cacheFile(kind, key);
  const cached = await readJsonIfExists(file);
  if (cached !== undefined) return cached;

  const queryList = (Array.isArray(queries) ? queries : [queries]).filter(Boolean);
  const active = PROVIDERS.filter((p) => p.enabled());

  for (const query of queryList) {
    for (;;) {
      let hadError = false;
      for (const provider of active) {
        if ((cooldownUntil.get(provider.name) || 0) > Date.now()) {
          hadError = true;
          continue;
        }
        try {
          const found = await provider.search(query);
          await sleep(provider.delayMs);
          if (found?.imageUrl) {
            const result = { ...found, imageSource: provider.name };
            await writeJson(file, result);
            return result;
          }
        } catch (err) {
          hadError = true;
          if (err.rateLimited) {
            console.log(`[images] ${provider.name} het han muc, tam nghi ${config.imageCooldownMs / 60000} phut.`);
            cooldownUntil.set(provider.name, Date.now() + config.imageCooldownMs);
          } else {
            console.warn(`[images] ${provider.name} loi voi "${query}": ${err.message}`);
          }
        }
      }
      if (!hadError) break; // tu khoa nay het nguon, chuyen sang tu khoa tong quat hon (neu con)
      const allCooling = active.every((p) => (cooldownUntil.get(p.name) || 0) > Date.now());
      if (!allCooling) {
        // Loi thuong (khong phai het han muc): bo qua muc nay, lan chay sau thu lai.
        return undefined;
      }
      const waitMs = Math.min(...active.map((p) => cooldownUntil.get(p.name))) - Date.now();
      console.log(`[images] moi nguon deu het han muc, cho ${Math.ceil(waitMs / 60000)} phut...`);
      await sleep(Math.max(waitMs, 1000));
    }
  }
  await writeJson(file, null);
  return null;
}
