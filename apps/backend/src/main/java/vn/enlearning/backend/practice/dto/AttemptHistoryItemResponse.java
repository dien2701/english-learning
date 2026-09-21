package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Skill;

/** {@code detailPath} là đường dẫn trang kết quả trên frontend. */
public record AttemptHistoryItemResponse(UUID attemptId, UUID lessonId, L10n lessonTitle, Skill skill,
		BigDecimal score, int correctCount, int totalQuestions, Instant submittedAt, String detailPath) {
}
