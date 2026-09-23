package vn.enlearning.backend.speaking.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

/** {@code isCompleted}/{@code lastScore} chỉ tính lượt đã chấm xong (GRADED) của chính người dùng. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpeakingSummaryResponse(UUID id, L10n title, L10n description, UUID topicId, L10n topicName,
		Level level, int promptCount, boolean isCompleted, BigDecimal lastScore, String imageUrl, String imageAuthor,
		String imageAuthorUrl) {
}
