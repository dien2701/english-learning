package vn.enlearning.backend.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;
import vn.enlearning.backend.common.ApiErrorResponse;
import vn.enlearning.backend.common.ErrorCode;

/** Ghi phản hồi lỗi JSON từ tầng filter, nơi {@code GlobalExceptionHandler} không với tới. */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

	private final JsonMapper jsonMapper;

	public void write(HttpServletResponse response, ErrorCode code) throws IOException {
		response.setStatus(code.status().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		jsonMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(code));
	}
}
