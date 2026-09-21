package vn.enlearning.backend.speaking.dto;

import java.util.UUID;

/** {@code order} tính từ 1; {@code phonetic} có thể null; {@code meaning} là nghĩa tiếng Việt. */
public record SpeakingPromptResponse(UUID id, int order, String text, String phonetic, String meaning) {
}
