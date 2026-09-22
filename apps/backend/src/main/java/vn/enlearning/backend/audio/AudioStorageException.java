package vn.enlearning.backend.audio;

/** Không lưu được file audio (mạng, quyền ghi đĩa, Cloudinary từ chối). */
public class AudioStorageException extends RuntimeException {

	public AudioStorageException(String message) {
		super(message);
	}

	public AudioStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
