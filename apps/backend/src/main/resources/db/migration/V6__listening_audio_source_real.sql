-- Đợt 13 (13f): thêm REAL vào audio_source cho audio người đọc thật (Tatoeba, seed thật) khác TTS/UPLOAD.
ALTER TABLE listening_lessons
    MODIFY COLUMN audio_source ENUM('TTS', 'UPLOAD', 'REAL') NULL COMMENT 'Nguồn file audio đang dùng';
