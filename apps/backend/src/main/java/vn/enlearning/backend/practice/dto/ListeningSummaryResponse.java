package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ListeningSummaryResponse(UUID id, L10n title, L10n description, UUID topicId, L10n topicName,
		Level level, int durationSeconds, long questionCount, boolean isCompleted, BigDecimal lastScore) {
}
