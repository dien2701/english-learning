package vn.enlearning.backend.flashcard.dto;

import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;

public record DeckDetailResponse(
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
		String status,
		List<CardResponse> cards) {

	public static DeckDetailResponse of(DeckSummaryResponse s, List<CardResponse> cards) {
		return new DeckDetailResponse(s.id(), s.title(), s.description(), s.coverImageUrl(), s.topicId(),
				s.topicName(), s.level(), s.totalCards(), s.learnedCards(), s.progressPercent(), s.status(), cards);
	}
}
