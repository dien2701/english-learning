-- =====================================================================
-- V4 · Ảnh minh hoạ + ghi công Unsplash cho nội dung học (đợt 13)
-- flashcard_decks và flashcards đã có cột ảnh, chỉ thêm ghi công.
-- =====================================================================

ALTER TABLE topics
    ADD COLUMN image_url        VARCHAR(500) NULL AFTER name_en,
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;

ALTER TABLE flashcard_decks
    ADD COLUMN cover_image_author     VARCHAR(200) NULL AFTER cover_image_url,
    ADD COLUMN cover_image_author_url VARCHAR(500) NULL AFTER cover_image_author;

ALTER TABLE flashcards
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;

ALTER TABLE listening_lessons
    ADD COLUMN image_url        VARCHAR(500) NULL AFTER description_en,
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;

ALTER TABLE writing_prompts
    ADD COLUMN image_url        VARCHAR(500) NULL AFTER title_en,
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;

ALTER TABLE speaking_lessons
    ADD COLUMN image_url        VARCHAR(500) NULL AFTER description_en,
    ADD COLUMN image_author     VARCHAR(200) NULL AFTER image_url,
    ADD COLUMN image_author_url VARCHAR(500) NULL AFTER image_author;
