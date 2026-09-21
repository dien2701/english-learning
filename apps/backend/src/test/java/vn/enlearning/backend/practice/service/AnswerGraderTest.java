package vn.enlearning.backend.practice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AnswerSubmission;
import vn.enlearning.backend.practice.service.AnswerGrader.Grading;

class AnswerGraderTest {

	private final AnswerGrader grader = new AnswerGrader();

	private static Question choice(Skill skill, QuestionOption... options) {
		Question q = question(skill, QuestionKind.SINGLE_CHOICE);
		int order = 0;
		for (QuestionOption o : options) {
			o.setQuestion(q);
			o.setSortOrder(order++);
			q.getOptions().add(o);
		}
		return q;
	}

	private static Question fill(Skill skill, String... accepted) {
		Question q = question(skill, QuestionKind.FILL_BLANK);
		q.setAcceptedAnswers(List.of(accepted));
		return q;
	}

	private static Question question(Skill skill, QuestionKind kind) {
		Question q = new Question();
		q.setId(UUID.randomUUID());
		q.setSkill(skill);
		q.setKind(kind);
		q.setContent("?");
		return q;
	}

	private static QuestionOption option(boolean correct) {
		QuestionOption o = new QuestionOption();
		o.setId(UUID.randomUUID());
		o.setContent("o");
		o.setCorrect(correct);
		return o;
	}

	private static AnswerSubmission pick(Question q, QuestionOption o) {
		return new AnswerSubmission(q.getId(), o.getId(), null);
	}

	private static AnswerSubmission type(Question q, String text) {
		return new AnswerSubmission(q.getId(), null, text);
	}

	private static void assertInvalid(Throwable thrown) {
		assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> {
			assertThat(e.getCode()).isEqualTo(ErrorCode.VALIDATION);
			assertThat(e.getFieldErrorKeys()).containsEntry("answers", "errors.invalidAnswers");
		});
	}

	@Test
	@DisplayName("Trắc nghiệm: chọn đúng/sai theo QuestionOption")
	void gradesSingleChoice() {
		QuestionOption right = option(true);
		QuestionOption wrong = option(false);
		Question q1 = choice(Skill.READING, right, wrong);
		QuestionOption right2 = option(true);
		QuestionOption wrong2 = option(false);
		Question q2 = choice(Skill.READING, right2, wrong2);

		Grading result = grader.grade(List.of(q1, q2), List.of(pick(q1, right), pick(q2, wrong2)));

		assertThat(result.correctCount()).isEqualTo(1);
		assertThat(result.totalQuestions()).isEqualTo(2);
		assertThat(result.score()).isEqualByComparingTo("5.0");
		assertThat(result.answers().get(0).correct()).isTrue();
		assertThat(result.answers().get(0).selectedOption()).isSameAs(right);
		assertThat(result.answers().get(1).correct()).isFalse();
	}

	@Test
	@DisplayName("Điền từ: không phân biệt hoa/thường, bỏ khoảng trắng thừa, khớp một trong nhiều đáp án")
	void fillBlankIsCaseAndSpaceInsensitive() {
		Question q1 = fill(Skill.READING, "apple");
		Question q2 = fill(Skill.READING, "Ice cream", "gelato");
		Question q3 = fill(Skill.READING, "dog");

		Grading result = grader.grade(List.of(q1, q2, q3),
				List.of(type(q1, "Apple"), type(q2, "  ice   CREAM "), type(q3, "cat")));

		assertThat(result.answers()).extracting(AnswerGrader.GradedAnswer::correct).containsExactly(true, true, false);
		assertThat(result.answers().get(1).answerText()).isEqualTo("ice   CREAM");
	}

	@Test
	@DisplayName("Câu bỏ trống hoặc không có trong danh sách nộp tính sai, vẫn có một dòng kết quả")
	void unansweredCountsAsWrong() {
		QuestionOption right = option(true);
		Question q1 = choice(Skill.LISTENING, right, option(false));
		Question q2 = fill(Skill.LISTENING, "x");
		Question q3 = fill(Skill.LISTENING, "y");

		Grading result = grader.grade(List.of(q1, q2, q3),
				List.of(new AnswerSubmission(q1.getId(), null, null), type(q2, "   ")));

		assertThat(result.answers()).hasSize(3);
		assertThat(result.answers()).allSatisfy(a -> {
			assertThat(a.correct()).isFalse();
			assertThat(a.selectedOption()).isNull();
			assertThat(a.answerText()).isNull();
		});
		assertThat(result.score()).isEqualByComparingTo("0.0");
	}

	@Test
	@DisplayName("Điểm thang 10 làm tròn một chữ số; đúng hết là 10.0, bài rỗng là 0.0")
	void scoreScale() {
		assertThat(AnswerGrader.score(1, 3)).isEqualByComparingTo(new BigDecimal("3.3"));
		assertThat(AnswerGrader.score(2, 3)).isEqualByComparingTo(new BigDecimal("6.7"));
		assertThat(AnswerGrader.score(3, 3)).isEqualByComparingTo(new BigDecimal("10.0"));
		assertThat(AnswerGrader.score(0, 0)).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	@DisplayName("Đề kiểm tra: tách điểm theo kỹ năng của từng câu")
	void breakdownBySkill() {
		QuestionOption r1 = option(true);
		Question listening = choice(Skill.LISTENING, r1, option(false));
		Question reading1 = fill(Skill.READING, "a");
		Question reading2 = fill(Skill.READING, "b");

		Grading result = grader.grade(List.of(listening, reading1, reading2),
				List.of(pick(listening, r1), type(reading1, "a"), type(reading2, "zzz")));

		assertThat(result.breakdown().get(Skill.LISTENING).score()).isEqualByComparingTo("10.0");
		assertThat(result.breakdown().get(Skill.READING).correctCount()).isEqualTo(1);
		assertThat(result.breakdown().get(Skill.READING).totalQuestions()).isEqualTo(2);
		assertThat(result.breakdown().get(Skill.READING).score()).isEqualByComparingTo("5.0");
		assertThat(result.breakdown()).doesNotContainKey(Skill.WRITING);
	}

	@Test
	@DisplayName("questionId của bài khác, câu trùng, lựa chọn không thuộc câu, text quá dài: 400 VALIDATION")
	void rejectsMalformedSubmissions() {
		QuestionOption own = option(true);
		Question q1 = choice(Skill.READING, own, option(false));
		Question q2 = fill(Skill.READING, "a");
		Question foreign = fill(Skill.READING, "a");
		QuestionOption foreignOption = option(true);
		choice(Skill.READING, foreignOption);
		List<Question> lesson = List.of(q1, q2);

		assertInvalid(catchOf(() -> grader.grade(lesson, List.of(type(foreign, "a")))));
		assertInvalid(catchOf(() -> grader.grade(lesson, List.of(type(q2, "a"), type(q2, "a")))));
		assertInvalid(catchOf(() -> grader.grade(lesson, List.of(pick(q1, foreignOption)))));
		assertInvalid(catchOf(() -> grader.grade(lesson, List.of(type(q2, "x".repeat(501))))));
		assertInvalid(catchOf(() -> grader.grade(lesson, List.of(new AnswerSubmission(null, null, null)))));
	}

	private static Throwable catchOf(Runnable action) {
		return catchThrowable(action::run);
	}

	@Test
	@DisplayName("Hợp lệ khi nộp danh sách rỗng: mọi câu tính sai, không lỗi")
	void emptySubmissionIsAllowed() {
		Question q = fill(Skill.READING, "a");
		assertThat(grader.grade(List.of(q), List.of()).correctCount()).isZero();
	}
}
