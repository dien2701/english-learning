package vn.enlearning.backend.avatar;

/**
 * Kết quả lưu một ảnh đại diện.
 *
 * @param url      địa chỉ để hiển thị (URL Cloudinary hoặc {@code /api/media/avatars/...})
 * @param publicId định danh để xoá lại sau này, lưu ở {@code users.avatar_public_id}
 */
public record StoredAvatar(String url, String publicId) {
}
