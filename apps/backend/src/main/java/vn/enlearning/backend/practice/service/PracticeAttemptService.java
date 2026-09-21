package vn.enlearning.backend.practice.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.PracticeAttemptAnswer;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.practice.dto.SubmitRequest;
import vn.enlearning.backend.practice.service.AnswerGrader.Grading;

/**
 * Nộp và lưu một lượt làm bài Nghe, Đọc hoặc Kiểm tra. Mỗi lần nộp tạo một {@link PracticeAttempt} COMPLETED mới
 * (làm lại được nhiều lần; không có bước "bắt đầu làm" riêng). Người học chỉ nộp được bài ACTIVE. Nộp trễ vẫn
 * được nhận và chấm, chỉ đánh dấu {@code timedOut} (thời gian client báo vượt giới hạn quá dung sai).
 */
@Service
@RequiredArgsConstructor
public class PracticeAttemptService {

	/** Dung sai độ trễ mạng khi so thời gian làm bài với giới hạn. */
	static final int LATE_TOLERANCE_SECONDS = 10;

	private final PracticeAttemptRepository attempts;
	private final QuestionRepository questions;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final ExamRepository exams;
	private final UserRepository users;
	private final AnswerGrader grader;
	private final Clock clock;

	@Transactional
	public Submission submitListening(UUID userId, UUID lessonId, SubmitRequest request) {
		var lesson = listeningLessons.findById(lessonId).filter(PracticeAttemptService::isActive)
				.orElseThrow(PracticeAttemptService::notFound);
		return submit(userId, request, questions.findByListeningLessonIdOrderBySortOrder(lessonId), 0,
				a -> a.setListeningLesson(lesson));
	}

	@Transactional
	public Submission submitReading(UUID userId, UUID lessonId, SubmitRequest request) {
		var lesson = readingLessons.findById(lessonId).filter(PracticeAttemptService::isActive)
				.orElseThrow(PracticeAttemptService::notFound);
		return submit(userId, request, questions.findByReadingLessonIdOrderBySortOrder(lessonId),
				lesson.getTimeLimitMinutes(), a -> a.setReadingLesson(lesson));
	}

	@Transactional
	public Submission submitExam(UUID userId, UUID examId, SubmitRequest request) {
		var exam = exams.findById(examId).filter(PracticeAttemptService::isActive)
				.orElseThrow(PracticeAttemptService::notFound);
		return submit(userId, request, questions.findByExamIdOrderBySortOrder(examId), exam.getTimeLimitMinutes(),
				a -> a.setExam(exam));
	}

	private Submission submit(UUID userId, SubmitRequest request, List<Question> lessonQuestions,
			int timeLimitMinutes, Consumer<PracticeAttempt> setParent) {
		Grading grading = grader.grade(lessonQuestions, request.answers());
		int duration = request.durationSeconds() == null ? 0 : request.durationSeconds();
		Instant now = clock.instant();

		PracticeAttempt attempt = new PracticeAttempt();
		attempt.setUser(users.getReferenceById(userId));
		setParent.accept(attempt);
		attempt.setStatus(AttemptStatus.COMPLETED);
		attempt.setStartedAt(now.minusSeconds(duration));
		attempt.setSubmittedAt(now);
		attempt.setDurationSeconds(duration);
		attempt.setCorrectCount(grading.correctCount());
		attempt.setTotalQuestions(grading.totalQuestions());
		attempt.setScore(grading.score());
		for (AnswerGrader.GradedAnswer graded : grading.answers()) {
			PracticeAttemptAnswer row = new PracticeAttemptAnswer();
			row.setAttempt(attempt);
			row.setQuestion(graded.question());
			row.setSelectedOption(graded.selectedOption());
			row.setAnswerText(graded.answerText());
			row.setCorrect(graded.correct());
			attempt.getAnswers().add(row);
		}
		attempts.save(attempt);

		return new Submission(attempt, grading, isTimedOut(timeLimitMinutes, duration));
	}

	/** 0 phút nghĩa là không giới hạn thời gian. */
	static boolean isTimedOut(int timeLimitMinutes, int durationSeconds) {
		return timeLimitMinutes > 0 && durationSeconds > timeLimitMinutes * 60L + LATE_TOLERANCE_SECONDS;
	}

	private static boolean isActive(ContentEntity content) {
		return content.getStatus() == ContentStatus.ACTIVE;
	}

	private static ApiException notFound() {
		return new ApiException(ErrorCode.NOT_FOUND);
	}

	/** Lượt vừa lưu cùng kết quả chấm từng câu, để controller dựng phản hồi có đáp án đúng. */
	public record Submission(PracticeAttempt attempt, Grading grading, boolean timedOut) {
	}
}
