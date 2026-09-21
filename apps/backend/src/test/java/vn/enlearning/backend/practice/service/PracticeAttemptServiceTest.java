package vn.enlearning.backend.practice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AnswerSubmission;
import vn.enlearning.backend.practice.dto.SubmitRequest;
import vn.enlearning.backend.practice.service.PracticeAttemptService.Submission;

@ExtendWith(MockitoExtension.class)
class PracticeAttemptServiceTest {

	private static final Instant NOW = Instant.parse("2026-09-21T10:00:00Z");

	@Mock
	private PracticeAttemptRepository attempts;
	@Mock
	private QuestionRepository questions;
	@Mock
	private ListeningLessonRepository listeningLessons;
	@Mock
	private ReadingLessonRepository readingLessons;
	@Mock
	private ExamRepository exams;
	@Mock
	private UserRepository users;

	private PracticeAttemptService service;
	private final UUID userId = UUID.randomUUID();
	private final User user = new User();

	@BeforeEach
	void setUp() {
		service = new PracticeAttemptService(attempts, questions, listeningLessons, readingLessons, exams, users,
				new AnswerGrader(), Clock.fixed(NOW, ZoneOffset.UTC));
		user.setId(userId);
	}

	private static Question choiceQuestion(Skill skill, QuestionOption right, QuestionOption wrong) {
		Question q = new Question();
		q.setId(UUID.randomUUID());
		q.setSkill(skill);
		q.setKind(QuestionKind.SINGLE_CHOICE);
		q.setContent("?");
		right.setId(UUID.randomUUID());
		right.setCorrect(true);
		wrong.setId(UUID.randomUUID());
		q.getOptions().addAll(List.of(right, wrong));
		return q;
	}

	private static Question fillQuestion(Skill skill, String accepted) {
		Question q = new Question();
		q.setId(UUID.randomUUID());
		q.setSkill(skill);
		q.setKind(QuestionKind.FILL_BLANK);
		q.setContent("?");
		q.setAcceptedAnswers(List.of(accepted));
		return q;
	}

	private PracticeAttempt savedAttempt() {
		ArgumentCaptor<PracticeAttempt> captor = ArgumentCaptor.forClass(PracticeAttempt.class);
		verify(attempts).save(captor.capture());
		return captor.getValue();
	}

	@Test
	@DisplayName("Nộp bài nghe: lưu lượt COMPLETED gắn đúng bài nghe, mỗi câu một dòng đã chấm")
	void submitListeningPersistsAttemptAndAnswers() {
		ListeningLesson lesson = new ListeningLesson();
		UUID lessonId = UUID.randomUUID();
		QuestionOption right = new QuestionOption();
		Question q1 = choiceQuestion(Skill.LISTENING, right, new QuestionOption());
		Question q2 = fillQuestion(Skill.LISTENING, "apple");
		when(listeningLessons.findById(lessonId)).thenReturn(Optional.of(lesson));
		when(questions.findByListeningLessonIdOrderBySortOrder(lessonId)).thenReturn(List.of(q1, q2));
		when(users.getReferenceById(userId)).thenReturn(user);

		Submission result = service.submitListening(userId, lessonId, new SubmitRequest(
				List.of(new AnswerSubmission(q1.getId(), right.getId(), null),
						new AnswerSubmission(q2.getId(), null, "APPLE")),
				120));

		PracticeAttempt attempt = savedAttempt();
		assertThat(attempt).isSameAs(result.attempt());
		assertThat(attempt.getUser()).isSameAs(user);
		assertThat(attempt.getListeningLesson()).isSameAs(lesson);
		assertThat(attempt.getReadingLesson()).isNull();
		assertThat(attempt.getExam()).isNull();
		assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.COMPLETED);
		assertThat(attempt.getSubmittedAt()).isEqualTo(NOW);
		assertThat(attempt.getStartedAt()).isEqualTo(NOW.minusSeconds(120));
		assertThat(attempt.getDurationSeconds()).isEqualTo(120);
		assertThat(attempt.getCorrectCount()).isEqualTo(2);
		assertThat(attempt.getTotalQuestions()).isEqualTo(2);
		assertThat(attempt.getScore()).isEqualByComparingTo("10.0");
		assertThat(attempt.getAnswers()).hasSize(2).allSatisfy(a -> {
			assertThat(a.getAttempt()).isSameAs(attempt);
			assertThat(a.getCorrect()).isTrue();
		});
		assertThat(attempt.getAnswers().get(0).getSelectedOption()).isSameAs(right);
		assertThat(attempt.getAnswers().get(1).getAnswerText()).isEqualTo("APPLE");
		assertThat(result.timedOut()).isFalse();
	}

	@Test
	@DisplayName("Bài đọc nộp trễ quá dung sai: vẫn lưu và chấm nhưng timedOut = true; trong dung sai thì không")
	void lateReadingSubmissionIsFlagged() {
		ReadingLesson lesson = new ReadingLesson();
		lesson.setTimeLimitMinutes(10);
		UUID lessonId = UUID.randomUUID();
		when(readingLessons.findById(lessonId)).thenReturn(Optional.of(lesson));
		when(questions.findByReadingLessonIdOrderBySortOrder(lessonId)).thenReturn(List.of());
		when(users.getReferenceById(userId)).thenReturn(user);

		Submission onTime = service.submitReading(userId, lessonId, new SubmitRequest(List.of(), 600 + 10));
		Submission late = service.submitReading(userId, lessonId, new SubmitRequest(List.of(), 600 + 11));

		assertThat(onTime.timedOut()).isFalse();
		assertThat(late.timedOut()).isTrue();
		assertThat(late.attempt().getReadingLesson()).isSameAs(lesson);
	}

	@Test
	@DisplayName("Không giới hạn thời gian (0 phút) thì không bao giờ timedOut; durationSeconds thiếu tính là 0")
	void unlimitedTimeNeverTimesOut() {
		Exam exam = new Exam();
		exam.setTimeLimitMinutes(0);
		UUID examId = UUID.randomUUID();
		Question q = fillQuestion(Skill.READING, "a");
		when(exams.findById(examId)).thenReturn(Optional.of(exam));
		when(questions.findByExamIdOrderBySortOrder(examId)).thenReturn(List.of(q));
		when(users.getReferenceById(userId)).thenReturn(user);

		Submission result = service.submitExam(userId, examId, new SubmitRequest(List.of(), null));

		assertThat(result.timedOut()).isFalse();
		assertThat(result.attempt().getExam()).isSameAs(exam);
		assertThat(result.attempt().getDurationSeconds()).isZero();
		assertThat(result.grading().breakdown()).containsOnlyKeys(Skill.READING);
	}

	@Test
	@DisplayName("Bài không tồn tại hoặc INACTIVE: 404, không lưu gì")
	void unknownOrInactiveContentIsNotFound() {
		UUID missing = UUID.randomUUID();
		ReadingLesson inactive = new ReadingLesson();
		inactive.setStatus(ContentStatus.INACTIVE);
		UUID inactiveId = UUID.randomUUID();
		when(listeningLessons.findById(missing)).thenReturn(Optional.empty());
		when(readingLessons.findById(inactiveId)).thenReturn(Optional.of(inactive));
		when(exams.findById(missing)).thenReturn(Optional.empty());
		SubmitRequest request = new SubmitRequest(List.of(), 0);

		for (Throwable thrown : List.of(
				catchThrowable(() -> service.submitListening(userId, missing, request)),
				catchThrowable(() -> service.submitReading(userId, inactiveId, request)),
				catchThrowable(() -> service.submitExam(userId, missing, request)))) {
			assertThat(thrown).isInstanceOfSatisfying(ApiException.class,
					e -> assertThat(e.getCode()).isEqualTo(ErrorCode.NOT_FOUND));
		}
		verify(attempts, never()).save(any());
	}

	@Test
	@DisplayName("Câu trả lời sai định dạng: 400 và không lưu lượt nào")
	void invalidAnswersSaveNothing() {
		ListeningLesson lesson = new ListeningLesson();
		UUID lessonId = UUID.randomUUID();
		Question q = fillQuestion(Skill.LISTENING, "a");
		when(listeningLessons.findById(lessonId)).thenReturn(Optional.of(lesson));
		when(questions.findByListeningLessonIdOrderBySortOrder(lessonId)).thenReturn(List.of(q));

		Throwable thrown = catchThrowable(() -> service.submitListening(userId, lessonId,
				new SubmitRequest(List.of(new AnswerSubmission(UUID.randomUUID(), null, "a")), 5)));

		assertThat(thrown).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.getCode()).isEqualTo(ErrorCode.VALIDATION));
		verify(attempts, never()).save(any());
	}
}
