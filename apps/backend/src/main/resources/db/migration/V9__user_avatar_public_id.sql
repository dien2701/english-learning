-- =====================================================================
-- V9 · Định danh Cloudinary của ảnh đại diện, để xoá ảnh cũ khi thay ảnh mới
-- =====================================================================

ALTER TABLE users
    ADD COLUMN avatar_public_id VARCHAR(300) NULL AFTER avatar_url;
