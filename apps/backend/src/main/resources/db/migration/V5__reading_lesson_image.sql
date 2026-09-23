-- =====================================================================
-- V5 · Ảnh minh hoạ + ghi công Unsplash cho bài Luyện đọc (bỏ sót ở V4)
-- =====================================================================

ALTER TABLE reading_lessons
    ADD COLUMN image_url        VARCHAR(500) NULL AFTER description_en,
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;
