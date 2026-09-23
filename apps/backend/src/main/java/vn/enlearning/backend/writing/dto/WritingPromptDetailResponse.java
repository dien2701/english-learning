package vn.enlearning.backend.writing.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

/** Như bản tóm tắt, thêm yêu cầu đầy đủ ({@code prompt}) và các gợi ý triển khai. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WritingPromptDetailResponse(UUID id, L10n title, UUID topicId, L10n topicName, Level level,
		int suggestedMinutes, int minWords, String status, BigDecimal lastScore, String imageUrl, String imageAuthor,
		String imageAuthorUrl, String prompt, List<String> hints) {
}
