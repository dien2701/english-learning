package vn.enlearning.backend.practice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.PracticeAttemptAnswer;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AttemptHistoryItemResponse;
import vn.enlearning.backend.practice.dto.GradedAnswerResponse;
import vn.enlearning.backend.practice.dto.PracticeResultResponse;
import vn.enlearning.backend.practice.dto.SkillBreakdownResponse;

/** Dựng kết quả (có đáp án đúng) từ một lượt đã lưu. Phải gọi trong giao dịch vì đọc quan hệ LAZY. */
@Component
public class AttemptResultMapper {

	public PracticeResultResponse toResult(PracticeAttempt attempt) {
		List<PracticeAttemptAnswer> rows = attempt.getAnswers().stream()
				.sorted((a, b) -> Integer.compare(a.getQuestion().getSortOrder(), b.getQuestion().getSortOrder()))
				.toList();
		List<GradedAnswerResponse> answers = IntStream.range(0, rows.size())
				.mapToObj(i -> toAnswer(i + 1, rows.get(i)))
				.toList();

		int total = attempt.getTotalQuestions() == null ? rows.size() : attempt.getTotalQuestions();
		int correct = attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount();
		int duration = attempt.getDurationSeconds() == null ? 0 : attempt.getDurationSeconds();
		BigDecimal score = attempt.getScore() == null ? AnswerGrader.score(correct, total) : attempt.getScore();

		String transcript = attempt.getListeningLesson() == null ? null : attempt.getListeningLesson().getTranscript();
		List<SkillBreakdownResponse> breakdown = attempt.getExam() == null ? null : breakdown(rows);
		ContentEntity parent = parent(attempt);
		return new PracticeResultResponse(attempt.getId(), parent.getId(), title(parent), skill(attempt), score,
				correct, total - correct, total, duration, attempt.getSubmittedAt(),
				PracticeAttemptService.isTimedOut(timeLimitMinutes(attempt), duration), answers, transcript,
				breakdown);
	}

	public AttemptHistoryItemResponse toHistoryItem(PracticeAttempt attempt) {
		ContentEntity parent = parent(attempt);
		Skill skill = skill(attempt);
		String base = switch (skill) {
			case LISTENING -> "/listening/result/";
			case READING -> "/reading/result/";
			default -> "/exam/result/";
		};
		return new AttemptHistoryItemResponse(attempt.getId(), parent.getId(), title(parent), skill,
				attempt.getScore(), attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount(),
				attempt.getTotalQuestions() == null ? 0 : attempt.getTotalQuestions(), attempt.getSubmittedAt(),
				base + attempt.getId());
	}

	private static GradedAnswerResponse toAnswer(int order, PracticeAttemptAnswer row) {
		Question q = row.getQuestion();
		String userAnswer;
		String correctAnswer;
		if (q.getKind() == QuestionKind.SINGLE_CHOICE) {
			userAnswer = row.getSelectedOption() == null ? null : row.getSelectedOption().getContent();
			correctAnswer = q.getOptions().stream().filter(QuestionOption::isCorrect).map(QuestionOption::getContent)
					.findFirst().orElse("");
		} else {
			userAnswer = row.getAnswerText();
			correctAnswer = String.join(" / ", q.getAcceptedAnswers());
		}
		return new GradedAnswerResponse(q.getId(), order, q.getContent(), userAnswer, correctAnswer,
				Boolean.TRUE.equals(row.getCorrect()), q.getExplanation());
	}

	private static List<SkillBreakdownResponse> breakdown(List<PracticeAttemptAnswer> rows) {
		Map<Skill, int[]> tally = new EnumMap<>(Skill.class);
		for (PracticeAttemptAnswer row : rows) {
			int[] t = tally.computeIfAbsent(row.getQuestion().getSkill(), s -> new int[2]);
			t[1]++;
			if (Boolean.TRUE.equals(row.getCorrect())) {
				t[0]++;
			}
		}
		List<SkillBreakdownResponse> result = new ArrayList<>();
		tally.forEach((skill, t) -> result.add(new SkillBreakdownResponse(skill, t[0], t[1],
				AnswerGrader.score(t[0], t[1]))));
		return result;
	}

	private static ContentEntity parent(PracticeAttempt attempt) {
		if (attempt.getListeningLesson() != null) {
			return attempt.getListeningLesson();
		}
		return attempt.getReadingLesson() != null ? attempt.getReadingLesson() : attempt.getExam();
	}

	private static Skill skill(PracticeAttempt attempt) {
		if (attempt.getListeningLesson() != null) {
			return Skill.LISTENING;
		}
		return attempt.getReadingLesson() != null ? Skill.READING : Skill.EXAM;
	}

	private static int timeLimitMinutes(PracticeAttempt attempt) {
		if (attempt.getReadingLesson() != null) {
			return attempt.getReadingLesson().getTimeLimitMinutes();
		}
		return attempt.getExam() != null ? attempt.getExam().getTimeLimitMinutes() : 0;
	}

	private static L10n title(ContentEntity content) {
		return L10n.of(content.getTitleVi(), content.getTitleEn());
	}
}
