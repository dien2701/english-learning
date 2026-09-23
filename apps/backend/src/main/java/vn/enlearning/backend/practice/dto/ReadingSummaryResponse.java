package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReadingSummaryResponse(UUID id, L10n title, L10n description, UUID topicId, L10n topicName,
		Level level, int wordCount, long questionCount, int timeLimitMinutes, boolean isCompleted,
		BigDecimal lastScore, String imageUrl, String imageAuthor, String imageAuthorUrl) {
}
