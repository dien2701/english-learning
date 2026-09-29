package vn.enlearning.backend.avatar;

/**
 * Cổng lưu ảnh đại diện. {@link CloudinaryAvatarStorage} khi có {@code app.avatar.cloudinary-url},
 * ngược lại {@link LocalAvatarStorage}.
 */
public interface AvatarStorage {

	/**
	 * @param name      tên gốc không có đuôi, chỉ gồm chữ, số, {@code -} và {@code _}
	 * @param extension đuôi không có dấu chấm: {@code jpg}, {@code png} hoặc {@code webp}
	 * @throws AvatarStorageException khi không lưu được
	 */
	StoredAvatar store(byte[] data, String name, String extension);

	/** Xoá file theo {@link StoredAvatar#publicId()}. Chỉ ghi log khi lỗi, không ném ngoại lệ (file mồ côi không nghiêm trọng). */
	void delete(String publicId);
}
