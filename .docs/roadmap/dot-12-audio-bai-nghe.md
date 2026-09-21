# Đợt 12: Audio bài nghe (OpenAI TTS đa giọng, Cloudinary)

Phụ thuộc: đợt 5 (quản trị nội dung). Độc lập với đợt 11.
Quyết định (21/09/2026): đổi quyết định cũ, cho phép Cloudinary để lưu audio. Bản thu âm của người học KHÔNG lưu.

## Khung chung (12a)
- `.env`: `OPENAI_API_KEY` (đã có), `OPENAI_TTS_MODEL` (mặc định `gpt-4o-mini-tts`), `CLOUDINARY_URL`.
- Cổng `AudioStorage` (`store(bytes, publicId) → url`, `delete`): `CloudinaryAudioStorage` khi có `CLOUDINARY_URL` (resource_type `video`, thư mục `en-learning/listening`); thiếu thì `LocalAudioStorage` ghi vào `uploads/audio` và phục vụ qua `/api/media/audio/**` (permitAll, chỉ đọc).
- Cổng `SpeechSynthesizer` (`synthesize(text, voice) → mp3 bytes`): `OpenAiSpeechSynthesizer` khi có key; thiếu key thì bản giả trả MP3 im lặng ngắn có sẵn trong `resources`, không gọi ra ngoài.
- Migration số kế tiếp: `listening_lessons.audio_source` ENUM(`TTS`,`UPLOAD`) NULL (và `audio_public_id` để xoá file cũ khi thay). Sửa entity cùng lúc.

## 12.1 Sinh MP3 đa giọng từ transcript (12a)
- [ ] Tách transcript theo dòng `Tên: câu nói`; mỗi tên người nói gán một giọng cố định trong bảng giọng OpenAI (thứ tự xuất hiện → `alloy`, `verse`/`echo`, `nova`, ...; người kể không có tên dùng giọng mặc định). Gộp các dòng liền nhau cùng người nói, cắt đoạn dài hơn giới hạn ký tự của API.
- [ ] Gọi TTS từng đoạn định dạng `mp3`, ghép thành một file (nối khung MP3 cùng bitrate; nếu nghe lỗi thì chuyển sang xin `wav`/`pcm` rồi mã hoá một lần, ghi quyết định vào đây). Không thêm ffmpeg.
- [ ] `ListeningAudioService.generate(lessonId)`: sinh, tải lên `AudioStorage`, cập nhật `audio_url`, `audio_source=TTS`, xoá file cũ; cập nhật `duration_seconds` nếu đo được.
- [ ] Admin: `POST /admin/listening/:id/audio/generate` và `POST /admin/listening/:id/audio` (multipart, mp3/m4a/wav, ≤ 20MB, `audio_source=UPLOAD`); `DELETE /admin/listening/:id/audio`. Test 401/403/415/413.
- [ ] Sửa transcript bài `TTS` trong trang quản trị: không tự sinh lại; hiện gợi ý "Sinh lại audio".

## 12.2 FE phát audio (12b)
- [ ] Trang nghe luôn phát `audioUrl`. Chỉ khi `audioUrl` rỗng mới dùng TTS trình duyệt, và đọc `transcript` (không đọc description). Sửa fallback của phiên 8f cho khớp.
- [ ] Form quản trị bài nghe: hiện nguồn audio (TTS/UPLOAD/chưa có), trình phát thử, nút "Sinh bằng AI", "Tải file lên", "Xoá audio". Chuỗi VI/EN.

## 12.3 Sinh audio cho 12 bài seed (12c)
- [ ] Lệnh chạy một lần: `ApplicationRunner` bật bằng cờ `app.audio.generate-seed=true`, duyệt các bài nghe chưa có `audio_url`, sinh và lưu; bỏ qua bài đã có; log tiến độ; xong thì tắt cờ.
- [ ] Ghi URL đã sinh ngược vào `seed-demo/listening.json` (và `seed/listening.json` nếu dùng) để lần seed sau không phải sinh lại.

## Tiêu chí xong
- [ ] Thiếu key/Cloudinary: mọi thứ chạy bằng bản giả + lưu cục bộ, test xanh.
- [ ] `./mvnw -q compile`, test module liên quan, `npm run lint`, `npm run build` sạch.
- [ ] Cập nhật `CLAUDE.md` (Quyết định đã chốt) và `ARCHITECTURE.md` mục hạ tầng: Cloudinary, OpenAI TTS.

## Kiểm tra hoàn thành (dự kiến)
| # | Request / thao tác | Kỳ vọng |
|---|---|---|
| 1 | `POST /admin/listening/{{id}}/audio/generate` (adminToken) | 200, `audioUrl` là URL Cloudinary, `audioSource` = TTS |
| 2 | Mở URL đó | Nghe đúng transcript, mỗi người nói một giọng |
| 3 | `POST /admin/listening/{{id}}/audio` với file mp3 | `audioSource` = UPLOAD, file cũ bị xoá trên Cloudinary |
| 4 | Gọi bằng token USER | 403 |
| 5 | Tải file `.txt` / file 30MB | 415 / 413 |
| 6 | Xoá audio rồi mở bài trên giao diện | Trình duyệt đọc transcript (không đọc mô tả) |
| 7 | Chạy BE với `app.audio.generate-seed=true` | 12 bài nghe có `audio_url`; SQL: `SELECT title_en, audio_source, audio_url FROM listening_lessons` |
| 8 | Bỏ `CLOUDINARY_URL` và `OPENAI_API_KEY`, sinh audio | URL `/api/media/audio/...` phát được đoạn im lặng, không gọi ra ngoài |
