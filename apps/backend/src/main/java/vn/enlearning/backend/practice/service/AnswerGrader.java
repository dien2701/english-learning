package vn.enlearning.backend.practice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AnswerSubmission;

/**
 * Chấm bài dùng chung cho Nghe, Đọc và Kiểm tra. Không đụng DB: nhận danh sách câu hỏi của đúng một bài (đã nạp
 * kèm lựa chọn) cùng câu trả lời, trả về kết quả từng câu. Câu bỏ trống tính sai; câu trả lời trỏ tới câu hỏi
 * không thuộc bài, trùng câu, hoặc lựa chọn không thuộc câu đó là lỗi 400.
 */
@Component
public class AnswerGrader {

	static final int MAX_TEXT_LENGTH = 500;
	private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

	public Grading grade(List<Question> questions, List<AnswerSubmission> submitted) {
		Map<UUID, Question> byId = new HashMap<>();
		questions.forEach(q -> byId.put(q.getId(), q));

		Map<UUID, AnswerSubmission> answerByQuestion = new HashMap<>();
		for (AnswerSubmission answer : submitted) {
			if (answer == null || answer.questionId() == null || !byId.containsKey(answer.questionId())
					|| answerByQuestion.put(answer.questionId(), answer) != null) {
				throw invalid();
			}
		}

		List<GradedAnswer> graded = new ArrayList<>(questions.size());
		Map<Skill, int[]> perSkill = new EnumMap<>(Skill.class);
		int correctCount = 0;
		for (Question question : questions) {
			GradedAnswer result = gradeOne(question, answerByQuestion.get(question.getId()));
			graded.add(result);
			int[] tally = perSkill.computeIfAbsent(question.getSkill(), s -> new int[2]);
			tally[1]++;
			if (result.correct()) {
				correctCount++;
				tally[0]++;
			}
		}

		Map<Skill, SkillScore> breakdown = new EnumMap<>(Skill.class);
		perSkill.forEach((skill, t) -> breakdown.put(skill, new SkillScore(t[0], t[1], score(t[0], t[1]))));
		return new Grading(graded, correctCount, questions.size(), score(correctCount, questions.size()), breakdown);
	}

	private GradedAnswer gradeOne(Question question, AnswerSubmission answer) {
		if (question.getKind() == QuestionKind.SINGLE_CHOICE) {
			if (answer == null || answer.optionId() == null) {
				return new GradedAnswer(question, null, null, false);
			}
			QuestionOption chosen = question.getOptions().stream()
					.filter(o -> answer.optionId().equals(o.getId()))
					.findFirst()
					.orElseThrow(AnswerGrader::invalid);
			return new GradedAnswer(question, chosen, null, chosen.isCorrect());
		}

		String text = answer == null ? null : answer.text();
		if (text != null && text.length() > MAX_TEXT_LENGTH) {
			throw invalid();
		}
		if (text == null || text.isBlank()) {
			return new GradedAnswer(question, null, null, false);
		}
		String normalized = normalize(text);
		boolean correct = question.getAcceptedAnswers().stream().anyMatch(a -> normalize(a).equals(normalized));
		return new GradedAnswer(question, null, text.strip(), correct);
	}

	/** Bỏ khoảng trắng thừa và không phân biệt hoa/thường. */
	static String normalize(String value) {
		return value.strip().replaceAll("\s+", " ").toLowerCase(Locale.ROOT);
	}

	/** Thang 10, một chữ số thập phân (cột {@code DECIMAL(3,1)}); bài không có câu hỏi được 0. */
	static BigDecimal score(int correct, int total) {
		if (total <= 0) {
			return BigDecimal.ZERO.setScale(1);
		}
		return MAX_SCORE.multiply(BigDecimal.valueOf(correct)).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
	}

	private static ApiException invalid() {
		return ApiException.field(ErrorCode.VALIDATION, "answers", "errors.invalidAnswers");
	}

	/** Kết quả một câu; {@code selectedOption}/{@code answerText} null nghĩa là bỏ trống. */
	public record GradedAnswer(Question question, QuestionOption selectedOption, String answerText, boolean correct) {
	}

	public record SkillScore(int correctCount, int totalQuestions, BigDecimal score) {
	}

	public record Grading(List<GradedAnswer> answers, int correctCount, int totalQuestions, BigDecimal score,
			Map<Skill, SkillScore> breakdown) {
	}
}
