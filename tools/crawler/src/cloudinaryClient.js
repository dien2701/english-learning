import crypto from 'node:crypto';
import { config } from './config.js';

const FOLDER = 'En-Learning'; // giong CloudinaryAudioStorage.java (Media Library Home/En-Learning)
const CLOUDINARY_URL_RE = /^cloudinary:\/\/([^:@/\s]+):([^@/\s]+)@([^/?#\s]+)/;

function parseCloudinaryUrl() {
  const m = CLOUDINARY_URL_RE.exec((config.cloudinaryUrl || '').trim());
  if (!m) {
    throw new Error('CLOUDINARY_URL phai co dang cloudinary://<api_key>:<api_secret>@<cloud_name>');
  }
  return { apiKey: m[1], apiSecret: m[2], cloudName: m[3] };
}

function sign(params, apiSecret) {
  const toSign =
    Object.keys(params)
      .sort()
      .map((k) => `${k}=${params[k]}`)
      .join('&') + apiSecret;
  return crypto.createHash('sha1').update(toSign, 'utf8').digest('hex');
}

/** Tai file mp3 len Cloudinary (resource_type video, giong BE) theo REST co ky, khong can SDK. */
export async function uploadAudio(data, name) {
  const { apiKey, apiSecret, cloudName } = parseCloudinaryUrl();
  const publicId = `${FOLDER}/${name}`;
  const timestamp = String(Math.floor(Date.now() / 1000));
  const signed = { public_id: publicId, asset_folder: FOLDER, timestamp };

  const form = new FormData();
  form.set('file', new Blob([data], { type: 'audio/mpeg' }), `${name}.mp3`);
  for (const [k, v] of Object.entries(signed)) form.set(k, v);
  form.set('api_key', apiKey);
  form.set('signature', sign(signed, apiSecret));

  const res = await fetch(`${config.cloudinaryBaseUrl}/v1_1/${cloudName}/video/upload`, {
    method: 'POST',
    body: form,
    signal: AbortSignal.timeout(config.cloudinaryTimeoutMs),
  });
  const json = await res.json().catch(() => ({}));
  if (!res.ok || !json.secure_url) {
    throw new Error(`Cloudinary upload loi HTTP ${res.status}: ${json.error?.message || 'khong ro'}`);
  }
  return { url: json.secure_url, publicId: json.public_id || publicId };
}
