package vn.enlearning.backend.flashcard.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import vn.enlearning.backend.entity.enums.RecallLevel;

public record RecallRequest(
		@NotNull(message = "errors.missingCardInfo") UUID flashcardId,
		@NotNull(message = "errors.missingCardInfo") RecallLevel recallLevel) {
}
