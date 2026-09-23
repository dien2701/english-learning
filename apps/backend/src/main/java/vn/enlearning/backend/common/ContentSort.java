package vn.enlearning.backend.common;

import org.springframework.data.domain.Sort;

/**
 * Sắp xếp dùng chung cho các trang danh sách (Flashcard, Nghe, Đọc, Viết, Nói): mọi
 * {@link vn.enlearning.backend.entity.ContentEntity} đều có {@code titleVi}. Giá trị lạ/rỗng coi như "az".
 */
public final class ContentSort {

	private ContentSort() {
	}

	public static Sort resolve(String sort) {
		if ("za".equalsIgnoreCase(sort)) {
			return Sort.by(Sort.Direction.DESC, "titleVi").and(Sort.by("id"));
		}
		if ("newest".equalsIgnoreCase(sort)) {
			return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
		}
		return Sort.by(Sort.Direction.ASC, "titleVi").and(Sort.by("id"));
	}
}
