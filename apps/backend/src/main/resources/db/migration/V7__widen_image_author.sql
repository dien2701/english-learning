-- =====================================================================
-- V7 · Nới cột image_author (200 -> 500): ghi công Wikimedia/Openverse dài
-- hơn Unsplash, seed từ vựng thật (đợt 13.7) gặp lỗi "Data too long".
-- =====================================================================

ALTER TABLE topics MODIFY COLUMN image_author VARCHAR(500) NULL;
ALTER TABLE flashcard_decks MODIFY COLUMN cover_image_author VARCHAR(500) NULL;
ALTER TABLE flashcards MODIFY COLUMN image_author VARCHAR(500) NULL;
ALTER TABLE listening_lessons MODIFY COLUMN image_author VARCHAR(500) NULL;
ALTER TABLE writing_prompts MODIFY COLUMN image_author VARCHAR(500) NULL;
ALTER TABLE speaking_lessons MODIFY COLUMN image_author VARCHAR(500) NULL;
ALTER TABLE reading_lessons MODIFY COLUMN image_author VARCHAR(500) NULL;
