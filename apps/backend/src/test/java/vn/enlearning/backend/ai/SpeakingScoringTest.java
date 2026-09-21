package vn.enlearning.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpeakingScoringTest {

	private static SpeakingGrader.PromptSummary prompt(String text, String transcript, double score) {
		return new SpeakingGrader.PromptSummary(text, new SpeakingGrader.PromptAssessment(transcript,
				BigDecimal.valueOf(score), List.of(), List.of(), "m"));
	}

	@Test
	@DisplayName("Điểm tổng và phát âm là trung bình điểm các câu, làm tròn 1 chữ số")
	void averagesPromptScores() {
		SpeakingScoring.Scores s = SpeakingScoring.aggregate(List.of(
				prompt("Hello there.", "Hello there.", 7.5), prompt("How are you?", "How are you", 8.0)));

		assertThat(s.overall()).isEqualByComparingTo("7.8");
		assertThat(s.pronunciation()).isEqualByComparingTo("7.8");
		assertThat(s.fluency()).isEqualByComparingTo("7.8");
		assertThat(s.vocabulary()).isEqualByComparingTo("10.0");
	}

	@Test
	@DisplayName("Từ vựng/ngữ pháp/bám đề theo mức khớp transcript với câu mẫu; thiếu từ hoặc im lặng thì thấp")
	void matchRatioDrivesAccuracy() {
		assertThat(SpeakingScoring.matchRatio("I think three is enough.", "I think tree is enough")).isEqualTo(0.8);
		assertThat(SpeakingScoring.matchRatio("Nice to meet you", "")).isZero();
		assertThat(SpeakingScoring.matchRatio("It's fine", "IT'S   fine!")).isEqualTo(1.0);

		SpeakingScoring.Scores s = SpeakingScoring.aggregate(List.of(prompt("a b c d", "a b", 5.0)));
		assertThat(s.grammar()).isEqualByComparingTo("5.0");
		assertThat(s.relevance()).isEqualByComparingTo("5.0");
	}
}
