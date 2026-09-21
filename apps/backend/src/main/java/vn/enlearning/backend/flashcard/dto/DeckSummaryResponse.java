package vn.enlearning.backend.flashcard.dto;

import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

/** Trạng thái suy ra từ tiến độ của người gọi: NOT_STARTED, IN_PROGRESS hoặc COMPLETED. */
public record DeckSummaryResponse(
		UUID id,
		L10n title,
		L10n description,
		String coverImageUrl,
		UUID topicId,
		L10n topicName,
		Level level,
		long totalCards,
		long learnedCards,
		int progressPercent,
		String status) {
}
