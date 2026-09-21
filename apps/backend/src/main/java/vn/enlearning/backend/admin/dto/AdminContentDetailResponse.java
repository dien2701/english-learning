package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/** Các trường của dòng danh sách, kèm {@code payload} cùng dạng body của PUT (có id từng thẻ/câu hỏi/câu nói). */
public record AdminContentDetailResponse(UUID id, ContentType skill, L10n title, L10n topicName, Level level,
		ContentStatus status, int itemCount, Instant updatedAt, boolean inUse, AdminContentRequest payload) {

	public static AdminContentDetailResponse of(AdminContentSummaryResponse s, AdminContentRequest payload) {
		return new AdminContentDetailResponse(s.id(), s.skill(), s.title(), s.topicName(), s.level(), s.status(),
				s.itemCount(), s.updatedAt(), s.inUse(), payload);
	}
}
