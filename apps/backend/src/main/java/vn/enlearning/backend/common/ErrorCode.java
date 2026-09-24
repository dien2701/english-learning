package vn.enlearning.backend.common;

import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ. {@code messageKey} là khoá dịch trong {@code vi.json}/{@code en.json} của frontend;
 * backend chỉ trả khoá, việc đổi sang VI/EN do giao diện làm.
 */
public enum ErrorCode {

	VALIDATION(HttpStatus.BAD_REQUEST, "errors.checkInfo"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "errors.invalidCredentials"),
	ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "errors.accountLocked"),
	EMAIL_TAKEN(HttpStatus.CONFLICT, "errors.emailTaken"),
	INVALID_CODE(HttpStatus.BAD_REQUEST, "errors.invalidCode"),
	WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "errors.wrongPassword"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "errors.sessionExpired"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "errors.forbidden"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "errors.notFound"),
	INVALID_STATE(HttpStatus.CONFLICT, "errors.invalidState"),
	CONTENT_IN_USE(HttpStatus.CONFLICT, "errors.contentInUse"),
	AUDIO_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "errors.audioTooLarge"),
	AUDIO_UNSUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "errors.audioUnsupported"),
	STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "errors.storageUnavailable"),
	AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "errors.aiUnavailable"),
	EMAIL_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "errors.emailSendFailed"),
	RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "errors.tooManyRequests"),
	CHAT_DAILY_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "errors.chatDailyLimit"),
	SPEAKING_INCOMPLETE(HttpStatus.BAD_REQUEST, "errors.speakingIncomplete"),
	INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "errors.server");

	private final HttpStatus status;
	private final String messageKey;

	ErrorCode(HttpStatus status, String messageKey) {
		this.status = status;
		this.messageKey = messageKey;
	}

	public HttpStatus status() {
		return status;
	}

	public String messageKey() {
		return messageKey;
	}
}
