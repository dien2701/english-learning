import { config } from './config.js';

async function searchOnce(query) {
  const url = `${config.unsplashSearchUrl}?query=${encodeURIComponent(query)}&per_page=1&orientation=landscape`;
  const res = await fetch(url, {
    headers: { Authorization: `Client-ID ${config.unsplashAccessKey}` },
    signal: AbortSignal.timeout(config.unsplashTimeoutMs),
  });
  if (res.status === 403 || res.status === 429) {
    throw Object.assign(new Error(`Unsplash HTTP ${res.status} (het han muc 50 req/gio)`), { rateLimited: true });
  }
  if (!res.ok) throw new Error(`Unsplash HTTP ${res.status}`);
  const json = await res.json();
  const photo = json.results?.[0];
  if (!photo) return null;
  return {
    imageUrl: photo.urls?.regular || '',
    imageAuthor: photo.user?.name || '',
    imageAuthorUrl: photo.user?.links?.html || '',
    downloadLocation: photo.links?.download_location || '',
  };
}

/** Bao hieu da tai anh theo dieu khoan Unsplash (khong chan pipeline neu loi). */
async function triggerDownload(downloadLocation) {
  if (!downloadLocation) return;
  try {
    const sep = downloadLocation.includes('?') ? '&' : '?';
    await fetch(`${downloadLocation}${sep}client_id=${config.unsplashAccessKey}`, {
      signal: AbortSignal.timeout(config.unsplashTimeoutMs),
    });
  } catch {
    // Khong quan trong bang viec co URL anh; bo qua loi trigger download.
  }
}

/** Nguon du phong cuoi trong imageClient.js: tim 1 anh, bao download theo dieu khoan Unsplash. */
export async function searchUnsplash(query) {
  const found = await searchOnce(query);
  if (!found) return null;
  await triggerDownload(found.downloadLocation);
  return { imageUrl: found.imageUrl, imageAuthor: found.imageAuthor, imageAuthorUrl: found.imageAuthorUrl };
}
