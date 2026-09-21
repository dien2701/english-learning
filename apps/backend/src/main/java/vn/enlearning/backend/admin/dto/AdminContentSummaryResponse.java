package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/**
 * Một dòng trong bảng quản lý nội dung. {@code topicName} null với đề kiểm tra; {@code inUse} là true khi nội dung
 * đã có trong lịch sử học nên chỉ được ngừng hoạt động, không được xoá.
 */
public record AdminContentSummaryResponse(UUID id, ContentType skill, L10n title, L10n topicName, Level level,
		ContentStatus status, int itemCount, Instant updatedAt, boolean inUse) {
}
