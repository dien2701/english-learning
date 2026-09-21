package vn.enlearning.backend.writing.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.SubmissionStatus;

/** {@code feedback} chỉ có khi {@code status} là GRADED. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WritingSubmissionResponse(UUID id, UUID promptId, L10n promptTitle, String content, int wordCount,
		SubmissionStatus status, Instant submittedAt, WritingFeedbackResponse feedback) {
}
