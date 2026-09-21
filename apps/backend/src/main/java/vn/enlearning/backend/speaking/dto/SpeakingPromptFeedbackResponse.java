package vn.enlearning.backend.speaking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** {@code text} là câu đề bài; {@code transcript} là văn bản AI nghe được (âm thanh gốc không được lưu). */
public record SpeakingPromptFeedbackResponse(UUID promptId, String text, String transcript, BigDecimal score,
		List<String> mispronounced, String comment) {
}
