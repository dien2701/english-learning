package vn.enlearning.backend.avatar;

/** Không lưu được ảnh đại diện (mạng, quyền ghi đĩa, Cloudinary từ chối). */
public class AvatarStorageException extends RuntimeException {

	public AvatarStorageException(String message) {
		super(message);
	}

	public AvatarStorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
