-- Đợt 12 (12a): nguồn và định danh file audio của bài nghe.
-- audio_source: TTS (sinh từ transcript) hoặc UPLOAD (Admin tải lên); NULL = chưa có audio, hoặc audio_url là link ngoài.
-- audio_public_id: public_id trên Cloudinary hoặc tên file trong uploads/audio, để xoá file cũ khi thay/xoá audio.
ALTER TABLE listening_lessons
    ADD COLUMN audio_source    ENUM('TTS', 'UPLOAD') NULL COMMENT 'Nguồn file audio đang dùng' AFTER audio_url,
    ADD COLUMN audio_public_id VARCHAR(300)          NULL COMMENT 'Định danh file ở AudioStorage (Cloudinary public_id hoặc tên file cục bộ)' AFTER audio_source;
