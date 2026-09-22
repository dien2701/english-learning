package vn.enlearning.backend.audio;

/**
 * Kết quả lưu một file audio.
 *
 * @param url      địa chỉ để trình duyệt phát (URL Cloudinary hoặc {@code /api/media/audio/...})
 * @param publicId định danh để xoá lại sau này, lưu ở {@code listening_lessons.audio_public_id}
 */
public record StoredAudio(String url, String publicId) {
}
