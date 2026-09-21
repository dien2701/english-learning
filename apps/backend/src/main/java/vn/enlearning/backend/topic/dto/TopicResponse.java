package vn.enlearning.backend.topic.dto;

import java.util.UUID;

import vn.enlearning.backend.common.L10n;

/** {@code itemCount}: số bộ thẻ đang hoạt động thuộc chủ đề (các kỹ năng khác sẽ cộng thêm ở đợt sau). */
public record TopicResponse(UUID id, L10n name, long itemCount) {
}
