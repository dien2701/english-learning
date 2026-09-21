package vn.enlearning.backend.writing.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.AiGradingException;
import vn.enlearning.backend.ai.WritingGrader;
import vn.enlearning.backend.config.AsyncConfig;
import vn.enlearning.backend.entity.AiFeedback;
import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.SubmissionStatus;
import vn.enlearning.backend.writing.repository.AiFeedbackRepository;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/**
 * Chấm bài nền. Bài đã lưu (GRADING) trước khi vào đây; lỗi AI thì bài chuyển NEEDS_RETRY, không mất.
 * Không giữ giao dịch trong lúc gọi AI (có thể chậm): đọc, gọi AI, rồi ghi là ba bước riêng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingGradingService {

	private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

	private final WritingSubmissionRepository submissions;
	private final AiFeedbackRepository feedbacks;
	private final WritingGrader grader;
	private final TransactionTemplate tx;
	private final Clock clock;

	@Async(AsyncConfig.AI_TASK_EXECUTOR)
	public void gradeAsync(UUID submissionId) {
		try {
			WritingGrader.Request request = tx.execute(status -> submissions.findById(submissionId)
					.filter(s -> s.getStatus() == SubmissionStatus.GRADING)
					.map(s -> new WritingGrader.Request(s.getPrompt().getInstructions(), s.getPrompt().getMinWords(),
							s.getContent(), s.getWordCount()))
					.orElse(null));
			if (request == null) {
				return;
			}
			WritingGrader.Grade grade = grader.grade(request);
			validate(grade);
			tx.executeWithoutResult(status -> saveGrade(submissionId, grade));
		} catch (RuntimeException e) {
			log.warn("Chấm bài viết {} thất bại, chuyển NEEDS_RETRY: {}", submissionId, e.toString());
			markRetry(submissionId);
		}
	}

	private void saveGrade(UUID submissionId, WritingGrader.Grade grade) {
		WritingSubmission submission = submissions.findById(submissionId)
				.filter(s -> s.getStatus() == SubmissionStatus.GRADING).orElse(null);
		if (submission == null) {
			// Đã quá hạn (NEEDS_RETRY) hoặc luồng khác đã xử lý: bỏ kết quả muộn.
			return;
		}
		AiFeedback feedback = feedbacks.findBySubmissionId(submissionId).orElseGet(AiFeedback::new);
		feedback.setSubmission(submission);
		feedback.setOverallScore(grade.overallScore());
		feedback.setGrammarScore(grade.grammarScore());
		feedback.setVocabularyScore(grade.vocabularyScore());
		feedback.setExpressionScore(grade.expressionScore());
		feedback.setSummary(grade.summary());
		feedback.setModelName(grade.modelName());
		feedback.setIssues(new ArrayList<>(grade.issues()));
		feedbacks.save(feedback);
		submission.setStatus(SubmissionStatus.GRADED);
	}

	private void markRetry(UUID submissionId) {
		try {
			tx.executeWithoutResult(status -> submissions.transition(submissionId, SubmissionStatus.GRADING,
					SubmissionStatus.NEEDS_RETRY, clock.instant()));
		} catch (RuntimeException e) {
			// Bài vẫn GRADING; lần đọc sau khi quá hạn sẽ tự chuyển NEEDS_RETRY.
			log.error("Không đánh dấu NEEDS_RETRY cho bài viết {}", submissionId, e);
		}
	}

	/** Bản OpenAI thật có thể trả sai định dạng; điểm ngoài thang 10 bị CHECK của DB từ chối, nên chặn sớm. */
	private static void validate(WritingGrader.Grade grade) {
		if (grade == null || grade.summary() == null || grade.issues() == null
				|| outOfRange(grade.overallScore()) || outOfRange(grade.grammarScore())
				|| outOfRange(grade.vocabularyScore()) || outOfRange(grade.expressionScore())) {
			throw new AiGradingException("Kết quả chấm không hợp lệ");
		}
	}

	private static boolean outOfRange(BigDecimal score) {
		return score == null || score.signum() < 0 || score.compareTo(MAX_SCORE) > 0;
	}
}
