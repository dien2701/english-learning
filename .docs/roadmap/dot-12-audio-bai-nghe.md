# Đợt 12: Audio bài nghe (OpenAI TTS đa giọng, Cloudinary)

Phụ thuộc: đợt 5 (quản trị nội dung). Độc lập với đợt 11.
Quyết định (21/09/2026): đổi quyết định cũ, cho phép Cloudinary để lưu audio. Bản thu âm của người học KHÔNG lưu.

## Khung chung (12a)
- `.env`: `OPENAI_API_KEY` (đã có), `OPENAI_TTS_MODEL` (mặc định `gpt-4o-mini-tts`), `CLOUDINARY_URL`.
- Cổng `AudioStorage` (`store(bytes, publicId) → url`, `delete`): `CloudinaryAudioStorage` khi có `CLOUDINARY_URL` (resource_type `video`, thư mục `en-learning/listening`); thiếu thì `LocalAudioStorage` ghi vào `uploads/audio` và phục vụ qua `/api/media/audio/**` (permitAll, chỉ đọc).
- Cổng `SpeechSynthesizer` (`synthesize(text, voice) → mp3 bytes`): `OpenAiSpeechSynthesizer` khi có key; thiếu key thì bản giả trả MP3 im lặng ngắn có sẵn trong `resources`, không gọi ra ngoài.
- Migration số kế tiếp: `listening_lessons.audio_source` ENUM(`TTS`,`UPLOAD`) NULL (và `audio_public_id` để xoá file cũ khi thay). Sửa entity cùng lúc.

## 12.1 Sinh MP3 đa giọng từ transcript (12a)
- [x] Tách transcript theo dòng `Tên: câu nói`; mỗi tên người nói gán một giọng cố định trong bảng giọng OpenAI (thứ tự xuất hiện → `alloy`, `verse`/`echo`, `nova`, ...; người kể không có tên dùng giọng mặc định). Gộp các dòng liền nhau cùng người nói, cắt đoạn dài hơn giới hạn ký tự của API.
- [x] Gọi TTS từng đoạn định dạng `mp3`, ghép thành một file (nối khung MP3 cùng bitrate; nếu nghe lỗi thì chuyển sang xin `wav`/`pcm` rồi mã hoá một lần, ghi quyết định vào đây). Không thêm ffmpeg.
- [x] `ListeningAudioService.generate(lessonId)`: sinh, tải lên `AudioStorage`, cập nhật `audio_url`, `audio_source=TTS`, xoá file cũ; cập nhật `duration_seconds` nếu đo được.
- [x] Admin: `POST /admin/listening/:id/audio/generate` và `POST /admin/listening/:id/audio` (multipart, mp3/m4a/wav, ≤ 20MB, `audio_source=UPLOAD`); `DELETE /admin/listening/:id/audio`. Test 401/403/415/413.
- [x] Sửa transcript bài `TTS` trong trang quản trị: không tự sinh lại; hiện gợi ý "Sinh lại audio". (BE xong: PUT không đụng audio; phần gợi ý làm ở 12b.)

## 12.2 FE phát audio (12b)
- [x] Trang nghe luôn phát `audioUrl`. Chỉ khi `audioUrl` rỗng mới dùng TTS trình duyệt, và đọc `transcript` (không đọc description). Sửa fallback của phiên 8f cho khớp.
- [x] Form quản trị bài nghe: hiện nguồn audio (TTS/UPLOAD/chưa có), trình phát thử, nút "Sinh bằng AI", "Tải file lên", "Xoá audio". Chuỗi VI/EN.

## 12.3 Sinh audio cho 12 bài seed (12c)
- [x] Lệnh chạy một lần: `ApplicationRunner` bật bằng cờ `app.audio.generate-seed=true`, duyệt các bài nghe chưa có `audio_url`, sinh và lưu; bỏ qua bài đã có; log tiến độ; xong thì tắt cờ.
- [x] Ghi URL đã sinh ngược vào `seed-demo/listening.json` (và `seed/listening.json` nếu dùng) để lần seed sau không phải sinh lại.

## Ghi chú 12a (đã làm)
- Quyết định ghép MP3: giữ `mp3` từ OpenAI, đọc khung (`audio/Mp3`), bỏ ID3/Xing rồi nối khung, cùng tần số/kênh (khác thì lỗi); chưa nghe thử thật nên nếu nối bị lỗi tiếng thì chuyển sang xin `wav`/`pcm` rồi mã hoá một lần. Cloudinary gọi REST có ký SHA-1, không thêm SDK.
- Giọng: người nói theo thứ tự xuất hiện → alloy, echo, nova, onyx, shimmer, fable, coral, ash (vòng lại); người kể/dòng đầu không tên → sage; dòng không tên sau đó đọc tiếp người nói trước. Đoạn tối đa 1500 ký tự.
- Migration `V3__listening_audio_source.sql` (audio_source, audio_public_id). PUT `/admin/content/{id}` KHÔNG đè audioUrl khi bài có audio_source (chỉ dùng 3 endpoint audio); GET chi tiết trả thêm `audioSource`.
- Endpoint trả `{id, audioUrl, audioSource, durationSeconds}`. Lỗi mới: 503 `AI_UNAVAILABLE` (TTS), 503 `STORAGE_UNAVAILABLE` (khoá i18n `errors.storageUnavailable`), 400 `fieldErrorKeys.file = errors.field.fileRequired`; 413/415 dùng `AUDIO_TOO_LARGE`/`AUDIO_UNSUPPORTED` sẵn có. FE (12b) cần thêm 2 khoá i18n mới này.
- Multipart toàn cục nâng lên 20MB/21MB; luyện nói vẫn giới hạn 5MB ở service. Cục bộ: `uploads/audio` (đã .gitignore), phát ở `/api/media/audio/**`.
- Test: `Mp3Test`, `TranscriptVoicePlannerTest`, `AudioServicesHttpTest` (đơn vị, không DB) và `ListeningAudioApiTests` (MockMvc).

## Ghi chú 12b (đã làm)
- Người học không nhận transcript trước khi nộp, nên `GET /listening/:id` thêm `speechText` (= transcript) CHỈ khi `audioUrl` trống; trình phát đọc trường này.
- Admin: `ListeningAudioPanel` (nguồn, trình phát thử, sinh/tải/xoá). Sinh/tải/xoá chỉ dùng được khi bài đã lưu (có id). `getContent` trả thêm `audioSource`. Khoá i18n mới: `errors.storageUnavailable`, `errors.field.fileRequired`, `contentForm.audio*`.

## Ghi chú 12c (đã làm)
- `audio/SeedAudioRunner` (`@ConditionalOnProperty app.audio.generate-seed=true`, `@Order(100)`): duyệt `findAllWithoutAudio()`, gọi `ListeningAudioService.generate`, lỗi một bài chỉ log rồi đi tiếp. Ghi ngược URL vào `src/main/resources/seed-demo/listening.json` (theo `titleEn`, đường dẫn tương đối nên chạy từ `apps/backend`) CHỈ với URL http(s) (Cloudinary); audio cục bộ không ghi ngược. `seed/listening.json` không dùng nên bỏ qua. Không tự tắt cờ được (là env): log nhắc bỏ cờ.

## Tiêu chí xong
- [x] Thiếu key/Cloudinary: mọi thứ chạy bằng bản giả + lưu cục bộ, test xanh.
- [x] `./mvnw -q compile`, test module liên quan, `npm run lint`, `npm run build` sạch.
- [x] Cập nhật `CLAUDE.md` (Quyết định đã chốt) và `ARCHITECTURE.md` mục hạ tầng: Cloudinary, OpenAI TTS.

## Kiểm tra hoàn thành
Folder Postman "Dot 12 - Audio bai nghe" (biến `listeningId12` tự lấy từ request 01). Request 04-06 cần tự chọn file trong tab Body (mp3 nhỏ / `.txt` / file > 20MB); bỏ `CLOUDINARY_URL` và `OPENAI_API_KEY` thì `audioUrl` là `/api/media/audio/...` (bản giả im lặng).
| # | Request / thao tác | Kỳ vọng |
|---|---|---|
| 1 | `GET /admin/content?skill=LISTENING&pageSize=1` (adminToken) | 200, lưu `items[0].id` |
| 2 | `POST /admin/listening/{id}/audio/generate` | 200, `{id, audioUrl, audioSource: TTS, durationSeconds}`; mở URL nghe đúng transcript, mỗi người một giọng |
| 3 | `GET /admin/content/{id}` | `audioSource` = TTS, `payload.audioUrl` có giá trị |
| 4 | `POST /admin/listening/{id}/audio` (multipart `file` mp3) | 200, `audioSource` = UPLOAD, file cũ bị xoá |
| 5 | Cùng endpoint với file `.txt` / file 30MB | 415 `AUDIO_UNSUPPORTED` / 413 `AUDIO_TOO_LARGE` |
| 6 | Gọi bằng token USER | 403 |
| 7 | `GET /listening/{id}` (token USER) | có `audioUrl`, không có `speechText` |
| 8 | `DELETE /admin/listening/{id}/audio` rồi `GET /listening/{id}` | `audioUrl`/`audioSource` null; `speechText` = transcript, giao diện đọc transcript |
| 9 | Chạy BE với `AUDIO_GENERATE_SEED=true` | 12 bài có `audio_url`; SQL: `SELECT title_en, audio_source, audio_url FROM listening_lessons` |
