package vn.enlearning.backend.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Đổi mọi ngoại lệ thành {@link ApiErrorResponse}. Kế thừa {@link ResponseEntityExceptionHandler}
 * để các lỗi chuẩn của Spring MVC (404, 405, 415, JSON hỏng...) cũng ra đúng vỏ này thay vì ProblemDetail.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ResponseEntity<Object> handleApi(ApiException ex) {
		ErrorCode code = ex.getCode();
		ApiErrorResponse body = ex.getFieldErrorKeys() == null
				? ApiErrorResponse.of(code)
				: ApiErrorResponse.of(code, code.messageKey(), ex.getFieldErrorKeys());
		return ResponseEntity.status(code.status()).body(body);
	}

	/** Ném từ method security; các bộ lọc đã tự xử lý phần còn lại. */
	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<Object> handleAccessDenied() {
		return ResponseEntity.status(ErrorCode.FORBIDDEN.status()).body(ApiErrorResponse.of(ErrorCode.FORBIDDEN));
	}

	@ExceptionHandler(AuthenticationException.class)
	ResponseEntity<Object> handleAuthentication() {
		return ResponseEntity.status(ErrorCode.UNAUTHORIZED.status()).body(ApiErrorResponse.of(ErrorCode.UNAUTHORIZED));
	}

	/** Lưới an toàn cuối: ghi log đầy đủ ở server, không lộ chi tiết ra ngoài. */
	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> handleUnexpected(Exception ex) {
		log.error("Lỗi không lường trước", ex);
		return ResponseEntity.status(ErrorCode.INTERNAL.status()).body(ApiErrorResponse.of(ErrorCode.INTERNAL));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, String> fieldErrorKeys = new LinkedHashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			String key = error.getDefaultMessage() != null ? error.getDefaultMessage() : "errors.badRequest";
			fieldErrorKeys.merge(error.getField(), key, GlobalExceptionHandler::preferRequired);
		}

		ValidationMessageKey annotation = ex.getBindingResult().getTarget() == null ? null
				: AnnotationUtils.findAnnotation(ex.getBindingResult().getTarget().getClass(), ValidationMessageKey.class);
		String messageKey = annotation != null ? annotation.value() : ErrorCode.VALIDATION.messageKey();

		return ResponseEntity.status(ErrorCode.VALIDATION.status())
				.body(ApiErrorResponse.of(ErrorCode.VALIDATION, messageKey, fieldErrorKeys));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (statusCode.is5xxServerError()) {
			log.error("Lỗi máy chủ trong Spring MVC", ex);
		}
		return ResponseEntity.status(statusCode).headers(headers).body(ApiErrorResponse.ofHttpStatus(statusCode));
	}

	/**
	 * Một ô có thể vi phạm nhiều ràng buộc cùng lúc (để trống vừa là thiếu vừa sai định dạng);
	 * giữ thông báo "bắt buộc" cho khớp cách mock trả lời.
	 */
	private static String preferRequired(String current, String incoming) {
		if (current.endsWith("Required")) {
			return current;
		}
		return incoming.endsWith("Required") ? incoming : current;
	}
}
