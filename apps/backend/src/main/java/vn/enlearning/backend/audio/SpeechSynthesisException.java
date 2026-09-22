package vn.enlearning.backend.audio;

/** TTS không trả được audio hợp lệ (lỗi mạng, quá hạn, phản hồi hỏng). Người gọi báo lỗi có thể thử lại. */
public class SpeechSynthesisException extends RuntimeException {

	public SpeechSynthesisException(String message) {
		super(message);
	}

	public SpeechSynthesisException(String message, Throwable cause) {
		super(message, cause);
	}
}
