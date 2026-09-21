package vn.enlearning.backend.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/** Bộ lọc danh sách của Admin: thấy cả nội dung INACTIVE, tìm theo tiêu đề. */
final class AdminContentSpecs {

	private AdminContentSpecs() {
	}

	static <T extends ContentEntity> Specification<T> filter(String search, ContentStatus status, Level level,
			boolean withTopic) {
		return (root, query, cb) -> {
			if (withTopic) {
				root.fetch("topic", JoinType.INNER);
			}
			List<Predicate> where = new ArrayList<>();
			if (status != null) {
				where.add(cb.equal(root.get("status"), status));
			}
			if (level != null) {
				where.add(cb.equal(root.get("level"), level));
			}
			if (search != null && !search.isBlank()) {
				String like = "%" + search.trim().toLowerCase(Locale.ROOT)
						.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
				where.add(cb.or(
						cb.like(cb.lower(root.get("titleVi")), like, '\\'),
						cb.like(cb.lower(root.get("titleEn")), like, '\\')));
			}
			return cb.and(where.toArray(Predicate[]::new));
		};
	}
}
