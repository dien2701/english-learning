package vn.enlearning.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FakeWritingGraderTest {

	private final FakeWritingGrader grader = new FakeWritingGrader(new AiProperties("", Duration.ofSeconds(60), Duration.ZERO));

	private static WritingGrader.Request request(String content, int words, int minWords) {
		return new WritingGrader.Request("Write something.", minWords, content, words);
	}

	@Test
	@DisplayName("Điểm nằm trong thang 10, có đủ 3 nhóm lỗi và tên mô hình")
	void gradeIsWellFormed() {
		WritingGrader.Grade grade = grader.grade(request("I study English. Every day.", 5, 50));

		assertThat(grade.modelName()).isEqualTo("fake-writing-grader");
		assertThat(grade.issues()).hasSize(3);
		assertThat(grade.issues().get(0).excerpt()).isEqualTo("I study English.");
		for (BigDecimal score : new BigDecimal[] { grade.overallScore(), grade.grammarScore(),
				grade.vocabularyScore(), grade.expressionScore() }) {
			assertThat(score).isBetween(BigDecimal.ZERO, BigDecimal.TEN);
		}
	}

	@Test
	@DisplayName("Bài dài hơn mức tối thiểu được điểm cao hơn bài ngắn")
	void longerEssayScoresHigher() {
		BigDecimal shortScore = grader.grade(request("Short.", 5, 50)).overallScore();
		BigDecimal fullScore = grader.grade(request("Long enough.", 50, 50)).overallScore();

		assertThat(fullScore).isGreaterThan(shortScore);
	}

	@Test
	@DisplayName("[fail] lỗi lần đầu rồi thành công khi chấm lại; [fail-always] luôn lỗi")
	void failureModes() {
		WritingGrader.Request once = request("Text [fail] once-" + System.nanoTime(), 4, 1);
		assertThatThrownBy(() -> grader.grade(once)).isInstanceOf(AiGradingException.class);
		assertThat(grader.grade(once).overallScore()).isNotNull();

		WritingGrader.Request always = request("Text [fail-always]", 2, 1);
		assertThatThrownBy(() -> grader.grade(always)).isInstanceOf(AiGradingException.class);
		assertThatThrownBy(() -> grader.grade(always)).isInstanceOf(AiGradingException.class);
	}
}
