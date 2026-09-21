package vn.enlearning.backend.speaking.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.AiGradingException;
import vn.enlearning.backend.ai.SpeakingGrader;
import vn.enlearning.backend.config.AsyncConfig;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;

/**
 * Chuyển văn bản và chấm bài nói ở luồng nền. Âm thanh chỉ nằm trong {@code request} (bộ nhớ) và bị bỏ khi hàm
 * kết thúc. Không chấm lại được: lỗi AI thì lượt là FAILED và người học ghi âm lại. Không giữ giao dịch trong
 * lúc gọi AI (có thể chậm).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakingGradingService {

	private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

	private final SpeakingAttemptRepository attempts;
	private final SpeakingGrader grader;
	private final TransactionTemplate tx;

	@Async(AsyncConfig.AI_TASK_EXECUTOR)
	public void gradeAsync(UUID attemptId, SpeakingGrader.Request request) {
		try {
			SpeakingGrader.Grade grade = grader.grade(request);
			validate(grade);
			tx.executeWithoutResult(status -> saveGrade(attemptId, grade));
		} catch (RuntimeException e) {
			log.warn("Chấm bài nói {} thất bại, chuyển FAILED: {}", attemptId, e.toString());
			markFailed(attemptId);
		}
	}

	private void saveGrade(UUID attemptId, SpeakingGrader.Grade grade) {
		SpeakingAttempt attempt = attempts.findById(attemptId)
				.filter(a -> a.getStatus() == SpeakingAttemptStatus.GRADING).orElse(null);
		if (attempt == null) {
			// Đã quá hạn (FAILED): bỏ kết quả muộn.
			return;
		}
		attempt.setOverallScore(grade.overallScore());
		attempt.setPronunciationScore(grade.pronunciationScore());
		attempt.setVocabularyScore(grade.vocabularyScore());
		attempt.setGrammarScore(grade.grammarScore());
		attempt.setFluencyScore(grade.fluencyScore());
		attempt.setRelevanceScore(grade.relevanceScore());
		attempt.setImprovements(new ArrayList<>(grade.improvements()));
		attempt.setPromptFeedback(new ArrayList<>(grade.promptFeedback()));
		attempt.setModelName(grade.modelName());
		attempt.setStatus(SpeakingAttemptStatus.GRADED);
	}

	private void markFailed(UUID attemptId) {
		try {
			tx.executeWithoutResult(status -> attempts.findById(attemptId)
					.filter(a -> a.getStatus() == SpeakingAttemptStatus.GRADING)
					.ifPresent(a -> a.setStatus(SpeakingAttemptStatus.FAILED)));
		} catch (RuntimeException e) {
			// Lượt vẫn GRADING; lần đọc sau khi quá hạn sẽ tự chuyển FAILED.
			log.error("Không đánh dấu FAILED cho bài nói {}", attemptId, e);
		}
	}

	/** Điểm ngoài thang 10 bị CHECK của DB từ chối, nên chặn sớm; điểm từng câu cũng phải hợp lệ. */
	private static void validate(SpeakingGrader.Grade grade) {
		if (grade == null || grade.improvements() == null || grade.promptFeedback() == null
				|| outOfRange(grade.overallScore()) || outOfRange(grade.pronunciationScore())
				|| outOfRange(grade.vocabularyScore()) || outOfRange(grade.grammarScore())
				|| outOfRange(grade.fluencyScore()) || outOfRange(grade.relevanceScore())
				|| grade.promptFeedback().stream().anyMatch(f -> f == null || outOfRange(f.score()))) {
			throw new AiGradingException("Kết quả chấm không hợp lệ");
		}
	}

	private static boolean outOfRange(BigDecimal score) {
		return score == null || score.signum() < 0 || score.compareTo(MAX_SCORE) > 0;
	}
}
