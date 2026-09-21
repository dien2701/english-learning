package vn.enlearning.backend.flashcard.dto;

import java.time.Instant;

public record ProgressResponse(Instant savedAt, long learnedCards, int progressPercent) {
}
