package vn.enlearning.backend.ai;

/**
 * Lời gọi Gemini thất bại (mạng, quá hạn, 429/5xx, bị chặn, JSON sai khuôn). Thông báo không chứa key hay nội dung bài;
 * các cổng AI bọc lại thành {@link AiGradingException} hoặc {@link AiUnavailableException}.
 */
public class GeminiException extends RuntimeException {

	public GeminiException(String message) {
		super(message);
	}

	public GeminiException(String message, Throwable cause) {
		super(message, cause);
	}
}
