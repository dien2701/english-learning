package vn.enlearning.backend.speaking.dto;

import java.math.BigDecimal;

/** Điểm từng mặt, thang 10; {@code relevance} là mức bám sát chủ đề. */
public record SpeakingScoresResponse(BigDecimal pronunciation, BigDecimal vocabulary, BigDecimal grammar,
		BigDecimal fluency, BigDecimal relevance) {
}
