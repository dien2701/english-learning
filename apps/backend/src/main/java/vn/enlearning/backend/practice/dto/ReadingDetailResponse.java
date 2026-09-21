package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReadingDetailResponse(UUID id, L10n title, L10n description, UUID topicId, L10n topicName,
		Level level, int wordCount, long questionCount, int timeLimitMinutes, boolean isCompleted,
		BigDecimal lastScore, List<String> passage, List<QuestionResponse> questions) {
}
