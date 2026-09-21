package vn.enlearning.backend.common;

import java.util.Map;

/** Lỗi nghiệp vụ có chủ đích; {@link GlobalExceptionHandler} đổi nó thành phản hồi lỗi chuẩn. */
public class ApiException extends RuntimeException {

	private final ErrorCode code;
	private final Map<String, String> fieldErrorKeys;

	public ApiException(ErrorCode code) {
		this(code, null);
	}

	public ApiException(ErrorCode code, Map<String, String> fieldErrorKeys) {
		super(code.name());
		this.code = code;
		this.fieldErrorKeys = fieldErrorKeys;
	}

	/** Không kèm stack trace: đây là luồng điều khiển bình thường, không phải sự cố. */
	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public ErrorCode getCode() {
		return code;
	}

	public Map<String, String> getFieldErrorKeys() {
		return fieldErrorKeys;
	}

	/** Lỗi ở một ô nhập cụ thể, ví dụ {@code email → errors.emailTaken}. */
	public static ApiException field(ErrorCode code, String field, String messageKey) {
		return new ApiException(code, Map.of(field, messageKey));
	}
}
