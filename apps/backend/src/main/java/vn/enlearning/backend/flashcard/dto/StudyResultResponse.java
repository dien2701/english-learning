package vn.enlearning.backend.flashcard.dto;

import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.RecallLevel;

public record StudyResultResponse(
		UUID deckId,
		L10n deckTitle,
		int studiedCards,
		long totalCards,
		int completionPercent,
		int remembered,
		int almostRemembered,
		int notRemembered,
		List<ReviewWord> wordsToReview) {

	public record ReviewWord(UUID id, String word, String phonetic, L10n meaning, String imageUrl,
			RecallLevel recallLevel) {
	}
}
