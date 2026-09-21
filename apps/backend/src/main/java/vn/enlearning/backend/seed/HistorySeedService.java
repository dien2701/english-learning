package vn.enlearning.backend.seed;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.dashboard.service.StudyTimeService;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.PracticeAttemptAnswer;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserFlashcardProgress;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.practice.dto.AnswerSubmission;
import vn.enlearning.backend.practice.service.AnswerGrader;
import vn.enlearning.backend.practice.service.AnswerGrader.Grading;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Nạp lịch sử học mẫu (phiên học, tiến độ thẻ, lượt làm bài) để Dashboard và Thống kê có dữ liệu khi chạy dev.
 * Mọi mốc thời gian tính lùi từ giờ hiện tại theo múi giờ của người dùng, nên biểu đồ tuần/tháng luôn có dữ liệu.
 * Chỉ nạp cho người dùng chưa có phiên học nào, nên chạy lại không nhân đôi.
 *
 * <p>Có hai phiên nằm sát nửa đêm giờ địa phương (23:30 và 00:10) để kiểm tra việc gom ngày theo múi giờ.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class HistorySeedService {

	static final String DEMO_EMAIL = "hocvien@enlearning.vn";
	private static final int DAYS = 60;
	private static final StudySkill[] SKILLS = { StudySkill.VOCABULARY, StudySkill.LISTENING, StudySkill.READING,
			StudySkill.EXAM };

	private final UserRepository users;
	private final StudyTimeService studyTime;
	private final StudySessionRepository sessions;
	private final UserFlashcardProgressRepository progress;
	private final PracticeAttemptRepository attempts;
	private final FlashcardDeckRepository decks;
	private final FlashcardRepository cards;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final ExamRepository exams;
	private final QuestionRepository questions;
	private final AnswerGrader grader;
	private final Clock clock;

	/** Số dòng đã nạp; test đối chiếu với dữ liệu đọc lại. */
	public record Report(int sessions, int ratedCards, int attempts) {
	}

	/** Nạp cho tài khoản học viên mẫu; rỗng nếu tài khoản không có hoặc đã có lịch sử. */
	@Transactional
	public Optional<Report> seedIfEmpty() {
		return users.findByEmail(DEMO_EMAIL).flatMap(this::seedFor);
	}

	@Transactional
	public Optional<Report> seedFor(User user) {
		if (sessions.existsByUserId(user.getId())) {
			log.info("{} đã có lịch sử học, bỏ qua nạp lịch sử mẫu.", user.getEmail());
			return Optional.empty();
		}
		ZoneId zone = studyTime.zoneOf(user.getId());
		Instant now = clock.instant();
		LocalDate today = LocalDate.now(clock.withZone(zone));

		int sessionCount = seedSessions(user, zone, today, now);
		int rated = seedFlashcards(user, now);
		int attemptCount = seedAttempts(user, zone, today);
		Report report = new Report(sessionCount, rated, attemptCount);
		log.info("Đã nạp lịch sử học mẫu cho {}: {}", user.getEmail(), report);
		return Optional.of(report);
	}

	// --- Phiên học ----------------------------------------------------------------------------------

	private int seedSessions(User user, ZoneId zone, LocalDate today, Instant now) {
		UUID[] refs = {
				decks.findAll().stream().findFirst().map(FlashcardDeck::getId).orElse(null),
				listeningLessons.findAll().stream().findFirst().map(ListeningLesson::getId).orElse(null),
				readingLessons.findAll().stream().findFirst().map(ReadingLesson::getId).orElse(null),
				exams.findAll().stream().findFirst().map(Exam::getId).orElse(null) };

		List<StudySession> rows = new ArrayList<>();
		for (int d = 0; d < DAYS; d++) {
			LocalDate day = today.minusDays(d);
			// Nghỉ vài ngày để biểu đồ có cột trống, riêng hôm nay luôn có.
			if (d != 0 && d % 6 == 4) {
				continue;
			}
			int skill = d % SKILLS.length;
			int seconds = (12 + (d * 7) % 38) * 60 + (d * 13) % 60;
			rows.add(session(user, SKILLS[skill], refs[skill], at(day, LocalTime.of(19, 30), zone, now), seconds,
					now));
			if (d > 0 && d % 3 == 0) {
				rows.add(session(user, StudySkill.VOCABULARY, refs[0], at(day, LocalTime.of(7, 0), zone, now), 600,
						now));
			}
		}
		// Sát nửa đêm giờ địa phương: khác ngày nếu (sai) gom theo UTC.
		rows.add(session(user, StudySkill.READING, refs[2], at(today.minusDays(1), LocalTime.of(23, 30), zone, now),
				25 * 60, now));
		rows.add(session(user, StudySkill.LISTENING, refs[1], at(today.minusDays(2), LocalTime.of(0, 10), zone, now),
				20 * 60, now));
		sessions.saveAll(rows);
		return rows.size();
	}

	/** Không cho phiên rơi vào tương lai (hôm nay, giờ hiện tại còn sớm hơn giờ mẫu). */
	private static Instant at(LocalDate day, LocalTime time, ZoneId zone, Instant now) {
		Instant start = day.atTime(time).atZone(zone).toInstant();
		return start.isAfter(now.minus(Duration.ofHours(1))) ? now.minus(Duration.ofHours(1)) : start;
	}

	private static StudySession session(User user, StudySkill skill, UUID refId, Instant start, int seconds,
			Instant now) {
		StudySession s = new StudySession();
		s.setUser(user);
		s.setSkill(skill);
		s.setRefId(refId);
		s.setStartedAt(start);
		s.setActiveSeconds(seconds);
		Instant end = start.plusSeconds(seconds);
		s.setLastHeartbeatAt(end.isAfter(now) ? now : end);
		return s;
	}

	// --- Tiến độ thẻ ---------------------------------------------------------------------------------

	/** Bộ đầu tiên học dở (khoảng 60% thẻ, một phần thuộc), bộ thứ hai học xong. */
	private int seedFlashcards(User user, Instant now) {
		List<FlashcardDeck> all = decks.findAll();
		int rated = 0;
		if (!all.isEmpty()) {
			List<Flashcard> deckCards = cards.findByDeckIdOrderBySortOrderAscIdAsc(all.get(0).getId());
			int count = (int) Math.ceil(deckCards.size() * 0.6);
			RecallLevel[] cycle = { RecallLevel.REMEMBERED, RecallLevel.ALMOST_REMEMBERED,
					RecallLevel.NOT_REMEMBERED };
			for (int i = 0; i < count; i++) {
				progress.save(progressRow(user, deckCards.get(i), cycle[i % cycle.length], now.minus(Duration.ofHours(5))));
				rated++;
			}
		}
		if (all.size() > 1) {
			for (Flashcard card : cards.findByDeckIdOrderBySortOrderAscIdAsc(all.get(1).getId())) {
				progress.save(progressRow(user, card, RecallLevel.REMEMBERED, now.minus(Duration.ofDays(3))));
				rated++;
			}
		}
		return rated;
	}

	private static UserFlashcardProgress progressRow(User user, Flashcard card, RecallLevel level, Instant reviewedAt) {
		UserFlashcardProgress p = new UserFlashcardProgress();
		p.setUser(user);
		p.setFlashcard(card);
		p.setRecallLevel(level);
		p.setReviewCount(1);
		int interval = switch (level) {
			case REMEMBERED -> 7;
			case ALMOST_REMEMBERED -> 3;
			case NOT_REMEMBERED -> 0;
		};
		p.setIntervalDays(interval);
		p.setLastReviewedAt(reviewedAt);
		p.setNextReviewAt(interval == 0 ? reviewedAt.plus(Duration.ofMinutes(10))
				: reviewedAt.plus(Duration.ofDays(interval)));
		return p;
	}

	// --- Lượt làm bài -------------------------------------------------------------------------------

	private int seedAttempts(User user, ZoneId zone, LocalDate today) {
		List<ListeningLesson> listening = listeningLessons.findAll();
		List<ReadingLesson> reading = readingLessons.findAll();
		List<Exam> examList = exams.findAll();
		int count = 0;
		// (ngày lùi, tỉ lệ đúng): bài đầu làm lại sau một thời gian, điểm tiến bộ dần.
		double[][] plan = { { 1, 0.8 }, { 3, 0.6 }, { 40, 0.5 }, { 20, 0.9 } };
		for (int i = 0; i < listening.size() && i < 2; i++) {
			ListeningLesson lesson = listening.get(i);
			List<Question> qs = questions.findByListeningLessonIdOrderBySortOrder(lesson.getId());
			count += save(user, zone, today, qs, plan[i], a -> a.setListeningLesson(lesson), 4 * 60);
		}
		if (!listening.isEmpty()) {
			ListeningLesson lesson = listening.get(0);
			count += save(user, zone, today, questions.findByListeningLessonIdOrderBySortOrder(lesson.getId()),
					plan[2], a -> a.setListeningLesson(lesson), 5 * 60);
		}
		for (int i = 0; i < reading.size() && i < 2; i++) {
			ReadingLesson lesson = reading.get(i);
			count += save(user, zone, today, questions.findByReadingLessonIdOrderBySortOrder(lesson.getId()),
					i == 0 ? new double[] { 2, 0.7 } : new double[] { 35, 1.0 }, a -> a.setReadingLesson(lesson),
					8 * 60);
		}
		for (int i = 0; i < examList.size() && i < 2; i++) {
			Exam exam = examList.get(i);
			count += save(user, zone, today, questions.findByExamIdOrderBySortOrder(exam.getId()),
					i == 0 ? new double[] { 4, 0.75 } : plan[3], a -> a.setExam(exam), 20 * 60);
		}
		return count;
	}

	/** Trả lời đúng {@code ratio} số câu đầu, sai phần còn lại, rồi chấm bằng đúng bộ chấm điểm thật. */
	private int save(User user, ZoneId zone, LocalDate today, List<Question> qs, double[] daysAndRatio,
			Consumer<PracticeAttempt> setParent, int durationSeconds) {
		if (qs.isEmpty()) {
			return 0;
		}
		int correctTarget = (int) Math.round(qs.size() * daysAndRatio[1]);
		List<AnswerSubmission> submitted = new ArrayList<>();
		for (int i = 0; i < qs.size(); i++) {
			AnswerSubmission answer = answer(qs.get(i), i < correctTarget);
			if (answer != null) {
				submitted.add(answer);
			}
		}
		Grading grading = grader.grade(qs, submitted);
		Instant submittedAt = today.minusDays((long) daysAndRatio[0]).atTime(20, 0).atZone(zone).toInstant();

		PracticeAttempt attempt = new PracticeAttempt();
		attempt.setUser(user);
		setParent.accept(attempt);
		attempt.setStatus(AttemptStatus.COMPLETED);
		attempt.setStartedAt(submittedAt.minusSeconds(durationSeconds));
		attempt.setSubmittedAt(submittedAt);
		attempt.setDurationSeconds(durationSeconds);
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
		return 1;
	}

	/** Null nghĩa là bỏ trống câu (khi không có lựa chọn sai để chọn). */
	private static AnswerSubmission answer(Question q, boolean correct) {
		if (q.getKind() == QuestionKind.SINGLE_CHOICE) {
			return q.getOptions().stream().filter(o -> o.isCorrect() == correct).findFirst()
					.map(QuestionOption::getId).map(id -> new AnswerSubmission(q.getId(), id, null)).orElse(null);
		}
		String text = correct && !q.getAcceptedAnswers().isEmpty() ? q.getAcceptedAnswers().get(0) : "sai";
		return new AnswerSubmission(q.getId(), null, text);
	}
}
