package vn.enlearning.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Vỏ bọc mọi phản hồi thành công; khớp {@code ApiResponse<T>} ở frontend ({@code shared/api/types.ts}). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String message) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, data, null);
	}
}
