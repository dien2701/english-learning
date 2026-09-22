package vn.enlearning.backend.audio;

/**
 * Cổng lưu file audio bài nghe. {@link CloudinaryAudioStorage} khi có {@code app.audio.cloudinary-url},
 * ngược lại {@link LocalAudioStorage}.
 */
public interface AudioStorage {

	/**
	 * @param name      tên gốc không có đuôi, chỉ gồm chữ, số, {@code -} và {@code _}
	 * @param extension đuôi không có dấu chấm: {@code mp3}, {@code m4a} hoặc {@code wav}
	 * @throws AudioStorageException khi không lưu được
	 */
	StoredAudio store(byte[] data, String name, String extension);

	/** Xoá file theo {@link StoredAudio#publicId()}. Chỉ ghi log khi lỗi, không ném ngoại lệ (file mồ côi không nghiêm trọng). */
	void delete(String publicId);
}
