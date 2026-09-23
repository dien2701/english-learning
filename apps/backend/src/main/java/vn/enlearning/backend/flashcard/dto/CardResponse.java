package vn.enlearning.backend.flashcard.dto;

import java.util.UUID;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.RecallLevel;

public record CardResponse(
		UUID id,
		String word,
		String phonetic,
		L10n meaning,
		L10n partOfSpeech,
		String example,
		String exampleMeaning,
		String imageUrl,
		String imageAuthor,
		String imageAuthorUrl,
		String audioUrl,
		RecallLevel recallLevel) {
}
