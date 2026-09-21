package vn.enlearning.backend.common;

import java.util.Map;

import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Phản hồi lỗi, đúng những trường mà interceptor của frontend ({@code shared/api/client.ts}) đọc:
 * {@code message}, {@code code}, {@code messageKey}, {@code fieldErrorKeys}. Giá trị của
 * {@code fieldErrorKeys} cũng là khoá dịch. {@code message} mang cùng khoá như mock đang làm.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
		boolean success,
		String message,
		String code,
		String messageKey,
		Map<String, String> fieldErrorKeys) {

	public static ApiErrorResponse of(ErrorCode code) {
		return new ApiErrorResponse(false, code.messageKey(), code.name(), code.messageKey(), null);
	}

	public static ApiErrorResponse of(ErrorCode code, String messageKey, Map<String, String> fieldErrorKeys) {
		return new ApiErrorResponse(false, messageKey, code.name(), messageKey, fieldErrorKeys);
	}

	/** Lỗi do Spring MVC ném ra (404, 405, 415...) nên không có mã nghiệp vụ; mã lấy theo trạng thái HTTP. */
	public static ApiErrorResponse ofHttpStatus(HttpStatusCode status) {
		String key = status.is5xxServerError() ? "errors.server"
				: status.value() == 404 ? "errors.notFound"
				: status.value() == 403 ? "errors.forbidden"
				: status.value() == 401 ? "errors.sessionExpired"
				: "errors.badRequest";
		return new ApiErrorResponse(false, key, "HTTP_" + status.value(), key, null);
	}
}
