package vn.enlearning.backend.dashboard.dto;

import java.time.Instant;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.Skill;

/** Khớp {@code ContinueLearning} ở frontend. */
public record ContinueLearningResponse(
		UUID id,
		Skill skill,
		L10n title,
		L10n topic,
		Level level,
		int progress,
		long completedItems,
		long totalItems,
		String coverUrl,
		String resumePath,
		Instant lastStudiedAt) {
}
