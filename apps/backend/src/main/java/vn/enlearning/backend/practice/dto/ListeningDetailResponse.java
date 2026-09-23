package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

/**
 * Không có transcript: chỉ trả sau khi nộp bài, trong kết quả. Ngoại lệ: bài chưa có audio thì
 * {@code speechText} mang transcript để trình duyệt đọc thay.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ListeningDetailResponse(UUID id, L10n title, L10n description, UUID topicId, L10n topicName,
		Level level, int durationSeconds, long questionCount, boolean isCompleted, BigDecimal lastScore,
		String imageUrl, String imageAuthor, String imageAuthorUrl, String audioUrl, String speechText,
		List<QuestionResponse> questions) {
}
