package vn.enlearning.backend.common;

import java.util.List;

/** Trang dữ liệu, khớp {@code Page<T>} của frontend ({@code shared/api/types.ts}). Trang đánh số từ 1. */
public record PageResponse<T>(List<T> items, int page, int pageSize, long total, int totalPages) {

	public static <T> PageResponse<T> of(List<T> all, int page, int pageSize) {
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);
		int from = (int) Math.min((long) (current - 1) * size, all.size());
		int to = Math.min(from + size, all.size());
		int totalPages = (all.size() + size - 1) / size;
		return new PageResponse<>(all.subList(from, to), current, size, all.size(), totalPages);
	}
}
