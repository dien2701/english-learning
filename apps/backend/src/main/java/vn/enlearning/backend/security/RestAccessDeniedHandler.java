package vn.enlearning.backend.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ErrorCode;

/** Đã đăng nhập nhưng thiếu quyền (ví dụ USER vào {@code /admin/**}) → 403 kèm JSON. */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ErrorResponseWriter writer;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		writer.write(response, ErrorCode.FORBIDDEN);
	}
}
