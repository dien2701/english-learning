package vn.enlearning.backend.ai;

/** AI không trả được kết quả (lỗi mạng, quá hạn, phản hồi hỏng). Người gọi chuyển bài sang trạng thái chấm lại. */
public class AiGradingException extends RuntimeException {

	public AiGradingException(String message) {
		super(message);
	}

	public AiGradingException(String message, Throwable cause) {
		super(message, cause);
	}
}
