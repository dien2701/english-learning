# Đợt 13: Dữ liệu thật

Mục tiêu: thay dữ liệu demo bằng dữ liệu thật (~1.500 từ, 20 chủ đề, 40 bài nghe), có ảnh minh hoạ Unsplash
(DB chỉ lưu URL) và audio bài nghe trên Cloudinary thư mục `en-learning/`.

## Quyết định đã chốt (22/09/2026)
- Nguồn: danh sách Oxford 3000 (CEFR) + Free Dictionary API `dictionaryapi.dev` (CC BY-SA): phiên âm, từ loại, định nghĩa, ví dụ.
  Nghĩa VI và nội dung song ngữ dịch bằng Gemini (gom ~50 từ/lần gọi). Kịch bản bài nghe do Gemini viết.
- Cách nạp: script Node crawl một lần → JSON trong `apps/backend/src/main/resources/seed/real/` → seeder BE.
- Ảnh: Unsplash API lúc crawl (`UNSPLASH_ACCESS_KEY`), lưu `urls.regular` + tên/link tác giả, gọi `download_location`
  theo điều khoản Unsplash. Cần ảnh cho: chủ đề, bộ từ/bài tập, từng từ, bài nghe, đề Viết/Nói. Từ trừu tượng không
  tìm được ảnh phù hợp thì để trống.
- Audio: OpenAI TTS (luồng đợt 12), upload Cloudinary `en-learning/listening/<id>`.
- Dashboard thời gian học: người dùng đã kiểm tra lại, hiển thị đúng, không sửa.

## Quyết định đổi (23/09/2026, người dùng tự chạy sau phiên 13z)
- `api.dictionaryapi.dev` (nguồn định nghĩa tiếng Anh ở 13.2) bị treo ở bước `/api/v2/entries/*` (trang chủ vẫn
  tải được nhưng API timeout liên tục, có thể do sự cố phía nhà cung cấp). Đổi sang Datamuse (`api.datamuse.com`,
  không cần key, dữ liệu gốc từ Wiktionary) trong `dictionaryClient.js`. Không còn câu ví dụ tiếng Anh cho mỗi từ
  (Datamuse không cung cấp) — `example`/`exampleMeaning` sẽ để trống, không ảnh hưởng các bước sau.

## Quyết định đổi (23/09/2026, người dùng tự chạy sau phiên 13z, tiếp theo)
- Lô Gemini bị timeout liên tục (3 lần chạy lại đều dừng đúng cùng 1 lô): đo trực tiếp thấy model
  người dùng cấu hình qua `GEMINI_MODEL` (`gemini-3.5-flash-lite`) trả lời đúng (status 200) nhưng
  mất ~69s cho 1 lô 50 từ, vượt `geminiTimeoutMs` cũ (45s). Tăng lên 120s trong `config.js`.

## Quyết định đổi (22/09/2026, phiên 13f)
- Audio 40 bài nghe seed KHÔNG dùng OpenAI TTS nữa: `tools/crawler/src/generateAudio.js` lấy audio người
  đọc thật (câu tiếng Anh có audio, nguồn Tatoeba.org, miễn phí, CC BY), ghép nhiều câu/bài, viết LẠI
  `transcript` + `questions` (Gemini, dựa đúng transcript mới) rồi tự tải lên Cloudinary, ghi thẳng
  `audioUrl`/`audioPublicId`/`durationSeconds` vào `listening.json` — giống cách `generateImages.js` ghi
  `imageUrl`. Seeder đọc các trường này, lưu `audio_source = REAL` (thêm vào enum, `V6` migration).
  `AUDIO_GENERATE_SEED`/`OpenAiSpeechSynthesizer` giữ lại nguyên cho tính năng Admin "Sinh lại audio" ở
  giao diện (không đụng), chỉ không cần chạy cho seed thật nữa vì JSON đã có `audioUrl` sẵn.

## Quyết định đổi (22/09/2026, tiếp tục crawl dở)
- Giảm còn 800 từ trải đều A–Z (`--words`, mặc định 800), dùng lại 1.000 bản dịch đã cache; 20 bài nghe
  (1 Viết, 1 Nói, 1 Nghe mỗi chủ đề).
- Ảnh nhiều nguồn dự phòng (`imageClient.js`): Pexels → Openverse → Wikimedia Commons → Unsplash. Không dùng
  Pixabay (điều khoản cấm hotlink lâu dài).

## Quyết định đổi (23/09/2026, phiên 13h, chạy thử) — ưu tiên hơn mục "Nghe" dưới đây
- `voaClient.js`/`generateListening.js` bản đầu (13h) cào mirror tĩnh `manythings.org/voa/...`
  (VOA Special English cũ) — chạy thử thì **toàn bộ** mp3 gốc trên `www.voanews.com/MediaAssets2/...`
  báo "fetch failed" (lỗi mạng, không phải 404 từng bài) → hạ tầng audio đó đã bị VOA gỡ hẳn, mirror
  không tự lưu bản mp3 nào nên không dùng được. Đổi sang cào CHÍNH trang hiện tại
  `learningenglish.voanews.com` (CDN audio riêng `voa-audio.voanews.eu`, còn sống); danh sách bài
  theo "zone" (chương trình, `/z/<zoneId>?p=<trang>`) thay cho category cũ. Chi tiết: README `tools/crawler`.

## Quyết định đổi (23/09/2026, giảm số bài nghe) — ưu tiên hơn mục "Nghe" dưới đây
- Giảm còn **8 bài nghe** (từ 20): mỗi bài mất nhiều lượt gọi Gemini (dịch tên + mô tả + 5 câu hỏi,
  đã gộp lại 1 lần gọi/bài ở phiên 13h) và free tier Gemini hết quota ngày rất nhanh (mới 2/20 bài đã
  hết). `targetListeningCount` mặc định đổi 20 → 8 trong `config.js`.

## Quyết định đổi (22/09/2026, thu gọn crawl) — ưu tiên hơn các mục trên
- Từ vựng: KHÔNG dịch thêm. Chỉ lấy từ đã có bản dịch trong `.cache/gemini`, cắt còn 500 từ trải đều A–Z
  (`--words 500`), không gọi Gemini cho từ vựng nữa.
- Đọc/Viết/Nói: KHÔNG để Gemini sinh bài mới. Giữ đúng bài seed đang có (`seed/reading.json` 4, `writing.json` 5,
  `speaking.json` 3), script chỉ tìm ảnh minh hoạ (chuỗi `imageClient.js`) và ghi `imageUrl/imageAuthor/imageAuthorUrl`.
- Nghe: bỏ Tatoeba ghép câu. Cào 8 bài thật từ VOA Learning English (public domain, giảm từ 20 — xem quyết
  định đổi 23/09/2026 phía trên): mp3 gốc + transcript,
  bắt buộc có audio, bài nào thiếu mp3 thì bỏ và lấy bài khác. Tải mp3 lên Cloudinary `Home/En-Learning`
  (public_id `En-Learning/<id>`), ghi `audioUrl/audioPublicId/durationSeconds`, `audio_source = REAL`, ghi công
  nguồn VOA. Câu hỏi trắc nghiệm: Gemini sinh từ transcript thật, 1 lần gọi/bài, cache lại. Kèm ảnh minh hoạ.

## 13.7 Crawl thu gọn
- [x] 13g: 500 từ từ cache dịch sẵn + ảnh cho bài Đọc/Viết/Nói seed có sẵn (không gọi Gemini).
- [x] 13h: `voaClient.js` + `generateListening.js`: 8 bài VOA thật (mp3 → Cloudinary, transcript, câu hỏi Gemini, ảnh) → `listening.json`.
      Cào chính `learningenglish.voanews.com` (không dùng mirror `manythings.org` — mp3 cũ trên đó đã chết hẳn,
      xem quyết định đổi 23/09/2026 phía trên). Người dùng đã tự chạy thật `cd tools/crawler && npm run generate-listening`
      qua VPN (voanews.com bị chặn mạng ở VN) — **đã có đủ 8/8 bài** trong `seed/real/listening.json` (cả 8 đều
      thuộc zone 987 "Words and Their Stories", topic `daily-life`, 2/8 có ảnh). Model Gemini đổi sang
      `gemini-3.1-flash-lite` (mặc định mới trong `config.js` — dòng "Flash" đầy đủ chỉ ~20 request/ngày miễn phí,
      hết quota liên tục; Flash-Lite ~500/ngày và vẫn hỗ trợ `responseSchema`). Còn thiếu: chạy seeder ở 13i.
- [x] 13i: `RealDataSeedService` thêm `seedReading` (mới) và trỏ lại `seedWriting`/`seedSpeaking` sang
      `resources/seed/{reading,writing,speaking}.json` (13g gắn ảnh vào đúng 3 file này, không phải
      `seed/real/`, và không phải `seed-demo/` mà `SeedService` đang dùng cho baseline — hai nguồn cộng dồn
      theo `topicId+titleVi`, không đụng nhau). Bỏ `seedExams` (đọc `seed/real/exercises.json` không tồn
      tại, ngoài phạm vi bản thu gọn — xem "Quyết định đổi 22/09/2026, thu gọn crawl"). Thêm
      `ReadingLessonRepository.existsByTopicIdAndTitleVi`, `RealSeedFiles.RealReading`. `./mvnw -q compile`
      và `-q test-compile` sạch; `./mvnw -q test -Dtest=RealDataSeedServiceTests` cần người dùng tự chạy
      (DB thật) để xác nhận 20 chủ đề + Đọc 4/Viết 5/Nói 3 (đều có ảnh) + 8 bài nghe REAL nạp đúng.
      Người dùng chạy thử `REAL_DATA_SEED=true` gặp `IllegalStateException` vì `reading.json` dùng slug
      chủ đề cũ `life` (không có trong 20 chủ đề thật) — thêm `LEGACY_TOPIC_ALIASES`/`legacyTopic()`: alias
      đã biết (`life`→`daily-life`) thì dùng, slug lạ khác thì bỏ qua bản ghi (log cảnh báo) thay vì chặn
      cả server. Người dùng cần chạy lại để xác nhận không còn lỗi và xem còn cảnh báo "Bỏ qua ..." nào
      không (nếu còn, cho biết slug để bổ sung alias).

## 13.1 Schema ảnh
- [x] Migration `V4__content_images.sql`: `image_url`, `image_author`, `image_author_url` cho topics, listening_lessons, writing_prompts, speaking_lessons; chỉ thêm `*_author`, `*_author_url` cho flashcard_decks (`cover_image_*`) và flashcards (đã có `image_url`).
- [x] Migration `V5__reading_lesson_image.sql`: bổ sung `image_url/image_author/image_author_url` cho reading_lessons (bị bỏ sót ở V4).
- [x] Entity, DTO, kiểu FE; FE hiện ảnh + dòng ghi công (`common.photoBy`) đè lên ảnh bìa (DeckCard, danh sách Nghe/Đọc/Viết/Nói), `WordImage` cho từng từ; `SafeImage`/`WordImage` đã có sẵn fallback khi thiếu ảnh.

## 13.2 Crawl từ vựng
- [x] `tools/crawler/` (Node, không nằm trong app), đọc key từ `.env`, cache từng request vào `tools/crawler/.cache/` (không commit).
- [x] Oxford 3000 → Dictionary API (giới hạn tốc độ, retry) → Gemini dịch VI + gán 20 chủ đề → `words.json`, `topics.json`.

## 13.3 Sinh đề
- [x] Bài tập từ vựng, đề Viết/Nói, 40 bài nghe (kịch bản + câu hỏi, đa cấp độ) → `exercises.json`, `writing.json`, `speaking.json`, `listening.json`.

## 13.4 Ảnh Unsplash
- [x] Tìm theo từ khoá, ghi URL/tác giả vào các JSON; chạy theo lô 50 req/giờ, resume từ cache.

## 13.5 Seeder
- [x] Cờ `REAL_DATA_SEED=true`, nạp idempotent (khoá slug/từ), bỏ qua bản ghi đã có, log số lượng.

## 13.6 Audio
- [x] Upload vào Media Library `Home/En-Learning` (`asset_folder`, public_id `En-Learning/<id>`), không thư mục con.
- [x] `tools/crawler/src/generateAudio.js`: audio người đọc thật (Tatoeba) cho 40 bài, viết lại
      transcript/questions khớp, tự tải lên Cloudinary, ghi `audioUrl` vào `listening.json`.
      **Người dùng tự chạy**: `cd tools/crawler && npm run generate-audio` (cần `GEMINI_API_KEY`,
      `CLOUDINARY_URL` trong `apps/backend/.env`), rồi chạy seeder `REAL_DATA_SEED=true` như cũ.

## Chuẩn bị `.env`
`UNSPLASH_ACCESS_KEY`, `GEMINI_API_KEY`, `CLOUDINARY_URL` (OPENAI_API_KEY không còn cần cho seed audio).

## Kiểm tra hoàn thành
| Kiểm tra | Kết quả mong đợi |
|---|---|
| `SELECT COUNT(*)` bảng từ / chủ đề / bài nghe | 500 / 20 / 8 (Đọc 4, Viết 5, Nói 3 đều có ảnh) |
| API danh sách chủ đề, bài nghe | có `imageUrl` + tác giả |
| Cloudinary | file nằm trong `Home/En-Learning/`, `audio_source` bài nghe = `REAL` |
| Chạy seeder lần 2 | không tạo bản ghi trùng |
