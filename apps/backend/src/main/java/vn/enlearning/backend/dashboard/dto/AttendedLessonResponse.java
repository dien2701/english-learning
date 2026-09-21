package vn.enlearning.backend.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.Skill;

/** Khớp {@code AttendedLesson} ở frontend; {@code score} vắng mặt với bộ thẻ. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttendedLessonResponse(
		UUID id,
		Skill skill,
		L10n title,
		Level level,
		LessonStatus status,
		int progress,
		BigDecimal score,
		Instant lastActivityAt,
		String detailPath) {

	public enum LessonStatus { IN_PROGRESS, COMPLETED }
}
