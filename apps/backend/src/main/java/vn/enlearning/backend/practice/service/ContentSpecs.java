package vn.enlearning.backend.practice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/** Bộ lọc danh sách cho nội dung có tiêu đề/mô tả song ngữ; người học chỉ thấy nội dung ACTIVE. */
final class ContentSpecs {

	private ContentSpecs() {
	}

	/** {@code withTopic}: nội dung có chủ đề (Nghe, Đọc); đề kiểm tra thì không. */
	static <T extends ContentEntity> Specification<T> learnerFilter(String search, UUID topicId, Level level,
			boolean withTopic) {
		return (root, query, cb) -> {
			if (withTopic) {
				root.fetch("topic", JoinType.INNER);
			}
			List<Predicate> where = new ArrayList<>();
			where.add(cb.equal(root.get("status"), ContentStatus.ACTIVE));
			if (withTopic && topicId != null) {
				where.add(cb.equal(root.get("topic").get("id"), topicId));
			}
			if (level != null) {
				where.add(cb.equal(root.get("level"), level));
			}
			if (search != null && !search.isBlank()) {
				String like = "%" + search.trim().toLowerCase(Locale.ROOT)
						.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
				where.add(cb.or(
						cb.like(cb.lower(root.get("titleVi")), like, '\\'),
						cb.like(cb.lower(root.get("titleEn")), like, '\\'),
						cb.like(cb.lower(root.get("descriptionVi")), like, '\\'),
						cb.like(cb.lower(root.get("descriptionEn")), like, '\\')));
			}
			return cb.and(where.toArray(Predicate[]::new));
		};
	}
}
