package vn.enlearning.backend.study.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import vn.enlearning.backend.entity.enums.StudySkill;

/** {@code refId}: bộ thẻ, bài học hoặc đề đang học; có thể trống (ví dụ CHAT chưa có hội thoại). */
public record HeartbeatRequest(
		@NotNull(message = "errors.badRequest") StudySkill skill,
		UUID refId) {
}
