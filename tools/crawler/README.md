# tools/crawler

Script Node chạy một lần (không nằm trong `apps/`) để tạo dữ liệu từ vựng thật
cho seeder backend (đợt 13). Không phụ thuộc package ngoài — dùng `fetch` và
`process.loadEnvFile` sẵn có của Node 20+.

## Chạy

```bash
cd tools/crawler
node src/index.js            # 13.2: tra tu dien 1.500 tu (cache), ghi 800 tu trai deu A-Z (--words=N)
node src/index.js --limit=20 # chay thu nhanh voi 20 tu de kiem tra pipeline
node src/generate.js         # 13.3: sinh bai tap tu vung + de Viet/Noi/Nghe (can words.json/topics.json co san)
node src/generateImages.js   # 13.4: anh Pexels -> Openverse -> Wikimedia -> Unsplash, resume
node src/generateAudio.js    # 13.6: audio nguoi doc thuc (Tatoeba) cho 20 bai nghe, resume theo bai
node src/selectWords.js --words=500   # 13g: chon 500 tu tu cache dich san, KHONG goi Gemini/Dictionary
node src/annotateDemoImages.js        # 13g: anh cho seed/{reading,writing,speaking}.json dang dung (khac seed/real/)
node src/generateListening.js         # 13h: 20 bai nghe thuc tu VOA Learning English (mp3 -> Cloudinary, cau hoi Gemini)
```

Đọc `GEMINI_API_KEY` (bắt buộc cho `generate.js`/`generateAudio.js`), `GEMINI_MODEL`,
`GEMINI_BASE_URL`, `UNSPLASH_ACCESS_KEY` (bắt buộc cho `generateImages.js`), `CLOUDINARY_URL`
(bắt buộc cho `generateAudio.js`) từ `apps/backend/.env`. Thiếu key thì script dừng ngay khi
tới bước cần key đó (bước tải từ điển ở `index.js` vẫn chạy được để kiểm tra riêng).

## Luồng

**`src/index.js` (13.2 — crawl từ vựng):**
1. Tải danh sách Oxford 3000 gốc (`data/oxford-3000.json`, đã vendor sẵn để khỏi
   phụ thuộc mạng khi chạy lại), lọc còn từ đơn/chữ thường (bỏ cụm từ, viết tắt,
   danh từ riêng), lấy mẫu đều tới `targetWordCount`.
2. Gọi Free Dictionary API (`api.dictionaryapi.dev`) lấy phiên âm, từ loại, định
   nghĩa, ví dụ tiếng Anh. Từ không có trong từ điển bị bỏ qua, ghi vào
   `.cache/skipped-words.json`.
3. Gom 50 từ/lần, gọi Gemini dịch nghĩa/từ loại/ví dụ sang tiếng Việt và gán 1
   trong 20 chủ đề cố định (`src/topics.js`) + mức độ (`BEGINNER/INTERMEDIATE/ADVANCED`).
4. Ghi `topics.json` và `words.json` vào `apps/backend/src/main/resources/seed/real/`.

**`src/generate.js` (13.3 — sinh đề), chạy sau khi có `words.json`/`topics.json`:**
1. `generateExercises.js`: tự sinh bài tập trắc nghiệm từ vựng **không gọi AI** — mỗi
   chủ đề đủ từ (≥ 8) tạo 1 đề 15 câu, đáp án đúng lấy từ `meaningVi` thật, 3 nhiễu lấy
   ngẫu nhiên từ nghĩa của các từ khác cùng chủ đề. Ghi `exercises.json`.
2. `generateLessons.js`: với mỗi chủ đề trong 20 chủ đề, gọi Gemini 3 lần để sinh
   2 đề Viết, 1 bài Nói (5 câu đọc to), 2 bài Nghe (transcript + 5 câu hỏi) — tổng
   40 bài nghe đúng như quyết định đợt 13. Ghi `writing.json`, `speaking.json`,
   `listening.json`.

**`src/generateImages.js` (13.4 — ảnh Unsplash), chạy sau khi có đủ 13.2/13.3:**
1. Với mỗi chủ đề, từ, bài Nghe/Viết/Nói, gọi Unsplash Search Photos (tên tiếng Anh
   làm từ khoá) lấy 1 ảnh phù hợp nhất, ghi `imageUrl` (`urls.regular`), `imageAuthor`,
   `imageAuthorUrl` thẳng vào `topics.json`, `words.json`, `listening.json`, `writing.json`,
   `speaking.json`. Không có kết quả (từ quá trừu tượng) thì để `null`, không lặp lại.
2. Có gọi `download_location` sau mỗi kết quả để tuân thủ điều khoản ghi nhận lượt tải
   của Unsplash.
3. App demo Unsplash giới hạn 50 request/giờ: mỗi lần gọi cách nhau ~72s; gặp lỗi 403/429
   (hết hạn mức) thì tự chờ 1 giờ rồi chạy tiếp — script chạy rất lâu (~1.600 mục), cứ để
   chạy nền, dừng giữa chừng (Ctrl+C) rồi chạy lại lệnh cũ là resume đúng phần còn thiếu.

**`src/generateAudio.js` (13.6 — audio người đọc thật), chạy sau khi có `listening.json`:**
1. Với mỗi bài nghe, tìm trên Tatoeba.org (miễn phí, câu có audio, CC BY) tối đa 8 câu tiếng
   Anh có audio khớp từ khoá tiêu đề bài (rơi về từ khoá chủ đề, rồi câu bất kỳ nếu vẫn thiếu).
   **Không dùng OpenAI TTS** cho dữ liệu seed thật nữa — thay bằng audio người đọc thật kéo về.
2. Tải các file mp3 đó, ghép thành 1 bài nghe (`src/mp3.js`, đọc khung MP3 để nối liền, không
   cần ffmpeg), viết LẠI `transcript` (nối nguyên văn các câu thật vừa chọn, không phải kịch bản
   Gemini sinh ở 13.3 nữa) và `questions` (gọi lại Gemini dựa đúng trên transcript mới này, vì
   audio thật không khớp nội dung transcript/câu hỏi cũ).
3. Tải file mp3 đã ghép lên Cloudinary (`CLOUDINARY_URL`, cùng thư mục `Home/En-Learning` mà BE
   dùng), ghi `audioUrl`/`audioPublicId`/`durationSeconds` thẳng vào `listening.json` — seeder
   (`RealDataSeedService`) đọc các trường này để lưu `audioSource=REAL`, **không cần** chạy
   `AUDIO_GENERATE_SEED=true` (TTS) cho các bài này nữa.
4. `audioCredit` ghi lại tên tác giả Tatoeba đã đọc từng câu (CC BY yêu cầu ghi nhận tác giả);
   trường này chỉ để đối chiếu, seeder đọc nhưng không lưu vào DB (app hiện chưa có chỗ hiển thị
   ghi công cho audio, khác với ảnh `imageAuthor`).
5. Chạy chậm, có thể dừng giữa chừng (Ctrl+C): bài đã có `audioUrl` được bỏ qua ngay ở lần chạy
   sau; câu Tatoeba đã tải/tìm cũng cache trong `.cache/tatoeba/`.

**`src/generateListening.js` (13h — 20 bai nghe VOA thuc), thay huong Tatoeba cua 13.6:**
1. `voaClient.js` doc danh sach bai tren CHINH trang hien tai `learningenglish.voanews.com`
   (**khong con dung mirror tinh `manythings.org`** - phien 13h chay thu thay toan bo mp3 cu tren
   `www.voanews.com/MediaAssets2/...` da chet het, ha tang audio do VOA go bo, khong phai roi rac
   tung bai). Danh sach bai phan trang qua `/z/<zoneId>?p=<trang>`, theo tung zone (chuong trinh)
   trong `config.voaZones` (9 zone, moi zone anh xa toi 1 trong 20 `topicSlug`).
2. Voi moi bai: doc `<h1>` lam tieu de, lay link mp3 du phong (`.c-mmp__fallback-link`, CDN
   `voa-audio.voanews.eu` con song), cat transcript trong khoi `div.wsw` (bo qua phan khung phat
   nhac dau trang bang moc "Direct link", cat truoc phan tu vung/dieu huong cuoi trang). Bai thieu
   1 trong 2 (transcript/mp3) thi bo qua ngay.
3. Kiem tra URL mp3 con song (`checkAudioAlive`, thu HEAD roi GET Range neu CDN khong cho HEAD):
   bai nao chet thi bo va lay bai ke tiep trong zone/danh sach.
4. Bai con song: goi Gemini 1 lan/bai (dua transcript that) sinh `titleVi`, `descriptionVi/En` va
   5 cau hoi nghe hieu; tai mp3 goc, tai len Cloudinary `Home/En-Learning` (giong 13.6); tim anh
   minh hoa qua `imageClient.js` (chuoi Pexels/Openverse/Wikimedia/Unsplash co san). Ghi thang vao
   `listening.json`, dung lai khi du `targetListeningCount` (20) bai.
5. Nhu cac script khac: dung giua chung (Ctrl+C) roi chay lai se resume (danh sach zone/trang bai/
   ket qua kiem tra mp3/mp3 da tai deu cache trong `.cache/voa/`).

## Thay doi 22/09/2026
- `index.js`: van tra tu dien 1.500 tu (dung cache), roi lay mau deu 800 tu; ban dich Gemini
  duoc gom theo tung tu tu moi lan chay cu trong `.cache/gemini/`, chi dich them tu chua co.
- `generateLessons.js`: moi chu de 1 Viet, 1 Noi, 1 Nghe (20 bai nghe).
- Anh: `imageClient.js` thu lan luot Pexels (`PEXELS_API_KEY`), Openverse, Wikimedia Commons,
  Unsplash; het han muc thi tam nghi nguon do 15 phut va dung nguon ke. Cache o `.cache/images/`.

## Cache & resume

Mỗi lần gọi Dictionary API, Gemini hoặc Unsplash đều cache ra file trong `.cache/`
(không commit). Dừng giữa chừng rồi chạy lại cùng lệnh sẽ đọc cache thay vì gọi
lại — chỉ tiếp tục phần còn thiếu. `index.js --limit` khác nhau sẽ đổi tập từ
lấy mẫu nên cache dịch theo lô tính lại (khoá theo hash danh sách từ); cache của
`generate.js` khoá theo `topicSlug` nên chạy lại luôn resume đúng theo chủ đề. Cache
của `generateImages.js` khoá theo `kind/key` (slug chủ đề, từ, hoặc `topic-viTri`) và
JSON đầu ra cũng được ghi định kỳ, nên mục đã có `imageUrl` (kể cả `null`) được bỏ qua
ngay ở lần chạy sau.
