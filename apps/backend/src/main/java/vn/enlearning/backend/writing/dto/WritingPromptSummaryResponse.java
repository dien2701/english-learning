package vn.enlearning.backend.writing.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

/** {@code status}: NOT_STARTED, hoặc trạng thái bài nộp gần nhất của người dùng (GRADING, GRADED, NEEDS_RETRY). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WritingPromptSummaryResponse(UUID id, L10n title, UUID topicId, L10n topicName, Level level,
		int suggestedMinutes, int minWords, String status, BigDecimal lastScore, String imageUrl, String imageAuthor,
		String imageAuthorUrl) {
}
