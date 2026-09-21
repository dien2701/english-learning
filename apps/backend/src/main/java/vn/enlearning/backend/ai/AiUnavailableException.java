package vn.enlearning.backend.ai;

/** Trợ lý AI không trả lời được (lỗi mạng, quá hạn, phản hồi hỏng). Người gọi báo lỗi có thể thử lại. */
public class AiUnavailableException extends RuntimeException {

	public AiUnavailableException(String message) {
		super(message);
	}

	public AiUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
