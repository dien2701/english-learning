package vn.enlearning.backend.speaking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;

/**
 * Kết quả một lượt nói. {@code overallScore} và {@code scores} chỉ có khi GRADED; khi GRADING hoặc FAILED
 * hai danh sách để rỗng. FAILED nghĩa là người học phải ghi âm lại (âm thanh không được lưu nên không chấm lại được).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpeakingResultResponse(UUID attemptId, UUID lessonId, L10n lessonTitle, SpeakingAttemptStatus status,
		BigDecimal overallScore, SpeakingScoresResponse scores, List<String> improvements,
		List<SpeakingPromptFeedbackResponse> promptFeedback, Instant submittedAt) {
}
