import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..'); // tools/crawler
const ENV_PATH = path.resolve(ROOT, '../../apps/backend/.env');

try {
  process.loadEnvFile(ENV_PATH);
} catch {
  // apps/backend/.env chua co (vd chua chay backend lan nao) -> bo qua, script van chay
  // duoc voi ban dich gia neu thieu GEMINI_API_KEY.
}

function argValue(flag, fallback) {
  const prefix = `--${flag}=`;
  const found = process.argv.find((a) => a.startsWith(prefix));
  return found ? found.slice(prefix.length) : fallback;
}

export const config = {
  dataDir: path.resolve(ROOT, 'data'),
  cacheDir: path.resolve(ROOT, '.cache'),
  outputDir: path.resolve(ROOT, '../../apps/backend/src/main/resources/seed/real'),

  oxfordListUrl: 'https://raw.githubusercontent.com/samuraitruong/oxford-3000/master/data/oxford-3000.json',
  oxfordListFile: 'oxford-3000.json',

  // Doi tu api.dictionaryapi.dev (23/09/2026, phien 13z): dich vu do bi treo/khong phan hoi
  // o backend API (trang chu van tai duoc nhung /api/v2/entries/* timeout lien tuc), chuyen
  // sang Datamuse (nguon du lieu tu Wiktionary, on dinh hon, khong can key) — xem dictionaryClient.js.
  dictionaryBaseUrl: 'https://api.datamuse.com/words',
  dictionaryDelayMs: 150,
  dictionaryMaxRetries: 3,
  dictionaryTimeoutMs: 15000,

  geminiApiKey: process.env.GEMINI_API_KEY || '',
  // Cac model "Flash" day du (3.5/3.6/3.7/3.8-flash) mien phi chi ~20 request/NGAY - het rat nhanh
  // khi crawl nhieu bai (23/09/2026, phien 13h: 8 bai da het quota 2 lan lien tiep, ke ca doi acc
  // moi). Doi sang dong "Flash-Lite" (~500 request/ngay, da kiem tra thuc te ho tro responseSchema
  // day du qua debugGemini.js tam - gemini-3.1-flash-lite VA gemini-2.5-flash-lite deu duoc; rieng
  // gemini-3.5-flash-lite KHONG ho tro responseSchema, tra 400 "invalid argument" voi moi schema).
  geminiModel: process.env.GEMINI_MODEL || 'gemini-3.1-flash-lite',
  geminiBaseUrl: process.env.GEMINI_BASE_URL || 'https://generativelanguage.googleapis.com',
  geminiBatchSize: 50,
  geminiDelayMs: 4000,
  // Tang tu 4 len 6 (23/09/2026, phien 13z): loi 429/503 can nhieu luot cho hon moi het.
  geminiMaxRetries: 6,
  // Vuot nguong nay trong 1 lo la dau hieu loi he thong (quota/billing), khong phai do tu vung
  // -> dung lai va bao loi thay vi am tham bo qua ca chuc tu (23/09/2026, phien 13z).
  geminiMaxSkipsPerBatch: 3,
  // Gap HTTP 429 (het quota/phut) can cho lau hon han loi thuong truoc khi retry
  // (23/09/2026, phien 13z: thu lai qua nhanh van bi 429 lien tiep roi het luot retry).
  geminiRateLimitDelayMs: Number(process.env.GEMINI_RATE_LIMIT_DELAY_MS || 30000),
  // Chay song song nhieu lo thay vi tuan tu de tan dung thoi gian cho phan hoi
  // (23/09/2026, phien 13z). Ha tu 4 xuong 2 sau khi 4 lam vuot quota o lo 21/30.
  geminiConcurrency: Number(process.env.GEMINI_CONCURRENCY || 2),
  // 45s ban dau khong du: model nguoi dung cau hinh (vd gemini-3.5-flash-lite qua GEMINI_MODEL)
  // co the mat toi ~70s cho 1 lo 50 tu (23/09/2026, do luong thuc te khi debug loi timeout lien tiep).
  // gemini-3.6-flash: 1 TU DUY NHAT da mat ~58s du bat thinkingBudget=0 (thoughtSignature van co
  // trong phan hoi, co ve model van "nghi" mot it) -> tang len 240s cho lo 50 tu (24/09/2026).
  geminiTimeoutMs: 240000,

  // ~1.500 tu theo quyet dinh dot 13; sua bang --limit=N khi chay thu.
  targetWordCount: Number(argValue('limit', process.env.CRAWLER_MAX_WORDS || 1500)),
  // So tu ghi ra words.json (22/09/2026: giam con 800), lay mau deu A-Z tu cac tu tra duoc
  // trong tu dien; giu targetWordCount = 1.500 de dung lai cache tu dien/Gemini da co.
  finalWordCount: Number(argValue('words', process.env.CRAWLER_FINAL_WORDS || 800)),

  unsplashAccessKey: process.env.UNSPLASH_ACCESS_KEY || '',
  unsplashSearchUrl: 'https://api.unsplash.com/search/photos',
  // App demo Unsplash gioi han 50 req/gio -> gian deu ra ca gio de khong bao gio vuot.
  unsplashDelayMs: Math.ceil((60 * 60 * 1000) / 50),
  unsplashMaxRetries: 3,
  unsplashTimeoutMs: 15000,

  // Anh nhieu nguon (22/09/2026): Pexels 200 req/gio, goi nhanh roi het han muc (429) thi
  // imageClient tu chuyen sang Openverse/Wikimedia (khong can key) trong luc Pexels tam nghi.
  pexelsApiKey: process.env.PEXELS_API_KEY || '',
  pexelsDelayMs: 1000,
  openverseDelayMs: 1500,
  wikimediaDelayMs: 500,
  imageTimeoutMs: 15000,
  imageCooldownMs: 15 * 60 * 1000,

  // Audio nguoi doc thuc (13.6): cau tieng Anh co san audio tren Tatoeba.org (mien phi, CC BY).
  tatoebaSearchUrl: 'https://tatoeba.org/en/api_v0/search',
  tatoebaAudioBaseUrl: 'https://tatoeba.org',
  tatoebaDelayMs: 500,
  tatoebaMaxRetries: 3,
  tatoebaTimeoutMs: 20000,
  audioSentencesPerLesson: 8,
  audioSentenceMinLen: 15,
  audioSentenceMaxLen: 130,

  cloudinaryUrl: process.env.CLOUDINARY_URL || '',
  cloudinaryBaseUrl: 'https://api.cloudinary.com',
  cloudinaryTimeoutMs: 60000,

  // Nghe (13h): 20 bai VOA Learning English THAT tren chinh trang hien tai (learningenglish.voanews.com),
  // KHONG dung mirror tinh manythings.org nua (23/09/2026, phien 13h chay thu: toan bo mp3 cu tren
  // www.voanews.com/MediaAssets2/... da chet het - "fetch failed" ca loat, khong phai roi rac tung bai -
  // ha tang audio cu do VOA da go bo). Trang hien tai co CDN audio rieng (voa-audio.voanews.eu) con song.
  // Moi "zone" (chuong trinh) la 1 zoneId, danh sach bai phan trang qua /z/<zoneId>?p=<trang>. Anh xa
  // zone -> 1 trong 20 topicSlug (src/topics.js) chi la uoc luong hop ly, khong bat buoc deu moi chu de.
  voaBaseUrl: 'https://learningenglish.voanews.com',
  voaZones: [
    { id: 987, topicSlug: 'daily-life' }, // Words and Their Stories
    { id: 1581, topicSlug: 'entertainment' }, // American Stories
    { id: 986, topicSlug: 'entertainment' }, // Arts & Culture
    { id: 3521, topicSlug: 'society-law' }, // As It Is
    { id: 4456, topicSlug: 'academic' }, // Everyday Grammar
    { id: 7468, topicSlug: 'education' }, // Education Tips
    { id: 955, topicSlug: 'health' }, // Health & Lifestyle
    { id: 1579, topicSlug: 'it-tech' }, // Science & Technology
    { id: 5535, topicSlug: 'education' }, // Ask a Teacher
  ],
  voaListPageSize: 12,
  voaMaxListPages: 6,
  voaDelayMs: 800,
  voaMaxRetries: 3,
  voaTimeoutMs: 20000,
  voaAudioCheckTimeoutMs: 15000,
  voaAudioDownloadTimeoutMs: 60000,
  targetListeningCount: Number(argValue('listening', 8)),
};
