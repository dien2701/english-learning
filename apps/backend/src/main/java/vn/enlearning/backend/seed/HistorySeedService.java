package vn.enlearning.backend.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.chat.repository.ChatConversationRepository;
import vn.enlearning.backend.chat.repository.ChatMessageRepository;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.dashboard.service.StudyTimeService;
import vn.enlearning.backend.entity.AiFeedback;
import vn.enlearning.backend.entity.ChatConversation;
import vn.enlearning.backend.entity.ChatMessage;
import vn.enlearning.backend.entity.ChatSuggestionLink;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.PracticeAttemptAnswer;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.SpeakingPromptFeedback;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserFlashcardProgress;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.entity.enums.SubmissionStatus;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.practice.dto.AnswerSubmission;
import vn.enlearning.backend.practice.service.AnswerGrader;
import vn.enlearning.backend.practice.service.AnswerGrader.Grading;
import vn.enlearning.backend.seed.HistorySeedContent.Chat;
import vn.enlearning.backend.seed.HistorySeedContent.ChatTurn;
import vn.enlearning.backend.seed.HistorySeedContent.Essay;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.study.repository.StudySessionRepository;
import vn.enlearning.backend.writing.repository.AiFeedbackRepository;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/**
 * Nạp lịch sử học mẫu để Dashboard, Thống kê và các trang lịch sử có dữ liệu khi chạy dev.
 * Mọi mốc thời gian tính lùi từ giờ hiện tại theo múi giờ của người dùng, nên biểu đồ tuần/tháng luôn có dữ liệu.
 * Mỗi người dùng chỉ được nạp khi chưa có phiên học nào, nên chạy lại không nhân đôi. Dữ liệu ngẫu nhiên dùng
 * {@link Random} có seed cố định theo email, nên lần nào cũng ra cùng một kết quả (chỉ mốc thời gian trượt theo giờ chạy).
 *
 * <ul>
 * <li>Học viên mẫu chính ({@link #DEMO_EMAIL}): 90 ngày học có chuỗi 14 ngày liền gần đây, cuối tuần ít hơn; lượt làm
 * bài điểm tăng dần; tiến độ thẻ (có thẻ đến hạn hôm nay); bài viết đã chấm, lượt nói (có một lượt {@code FAILED}) và
 * bốn hội thoại Chat.</li>
 * <li>Người dùng demo đang hoạt động: phiên học thưa và vài lượt làm bài trong khoảng từ ngày tạo tới lần hoạt động
 * cuối, để trang quản trị có số liệu theo ngày.</li>
 * </ul>
 *
 * <p>Có hai phiên nằm sát nửa đêm giờ địa phương (23:30 và 00:10) để kiểm tra việc gom ngày theo múi giờ.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class HistorySeedService {

	static final String DEMO_EMAIL = "hocvien@enlearning.vn";
	private static final int DAYS = 90;
	private static final int STREAK_DAYS = 14;
	private static final long RANDOM_SEED = 20260921L;
	private static final int MAIN_ATTEMPTS = 36;
	/** Người dùng demo chỉ được coi là đang hoạt động nếu lần cuối trong khoảng này. */
	private static final int ACTIVE_WINDOW_DAYS = 30;
	private static final LocalTime[] STUDY_TIMES = { LocalTime.of(7, 10), LocalTime.of(12, 20), LocalTime.of(19, 30),
			LocalTime.of(20, 15), LocalTime.of(21, 5) };
	/** Tỉ trọng kỹ năng của phiên học: từ vựng nhiều nhất. */
	private static final StudySkill[] SKILL_MIX = { StudySkill.VOCABULARY, StudySkill.VOCABULARY,
			StudySkill.VOCABULARY, StudySkill.LISTENING, StudySkill.LISTENING, StudySkill.READING, StudySkill.READING,
			StudySkill.WRITING, StudySkill.SPEAKING, StudySkill.EXAM, StudySkill.CHAT };
	/** Bảng có cột {@code user_id}; bảng con (đáp án, phản hồi, tin nhắn) tự xoá theo khoá ngoại. */
	private static final List<String> HISTORY_TABLES = List.of("study_sessions", "practice_attempts",
			"user_flashcard_progress", "writing_submissions", "speaking_attempts", "chat_conversations");
	private static final String FAKE_SPEAKING_MODEL = "fake-speaking-grader";
	private static final String FAKE_WRITING_MODEL = "fake-writing-grader";
	private static final String FAKE_CHAT_MODEL = "fake-chat-assistant";

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
	private final WritingPromptRepository writingPrompts;
	private final SpeakingLessonRepository speakingLessons;
	private final WritingSubmissionRepository submissions;
	private final AiFeedbackRepository feedbacks;
	private final SpeakingAttemptRepository speakingAttempts;
	private final ChatConversationRepository conversations;
	private final ChatMessageRepository messages;
	private final AnswerGrader grader;
	private final EntityManager em;
	private final Clock clock;

	/** Số dòng đã nạp; test đối chiếu với dữ liệu đọc lại. */
	public record Report(int sessions, int ratedCards, int attempts, int writing, int speaking, int chats) {
	}

	/** Nội dung ACTIVE dùng để gắn lịch sử, xếp theo thời điểm tạo cho ổn định. */
	private record Content(List<FlashcardDeck> decks, List<ListeningLesson> listening, List<ReadingLesson> reading,
			List<Exam> exams, List<WritingPrompt> writing, List<SpeakingLesson> speaking) {
	}

	/** Trạng thái của một lần nạp cho một người dùng. */
	private static final class Run {
		final User user;
		final ZoneId zone;
		final LocalDate today;
		final Instant now;
		final Random random;
		final Content content;
		final Map<UUID, List<Question>> questionCache = new HashMap<>();

		Run(User user, ZoneId zone, Instant now, Content content, Clock clock) {
			this.user = user;
			this.zone = zone;
			this.now = now;
			this.today = LocalDate.now(clock.withZone(zone));
			this.random = new Random(RANDOM_SEED ^ user.getEmail().hashCode());
			this.content = content;
		}
	}

	/** Nạp cho tài khoản học viên mẫu; rỗng nếu tài khoản không có hoặc đã có lịch sử. */
	@Transactional
	public Optional<Report> seedIfEmpty() {
		return users.findByEmail(DEMO_EMAIL).flatMap(this::seedFor);
	}

	/** Nạp lịch sử đầy đủ (90 ngày, mọi kỹ năng) cho một người dùng. */
	@Transactional
	public Optional<Report> seedFor(User user) {
		if (sessions.existsByUserId(user.getId())) {
			log.info("{} đã có lịch sử học, bỏ qua nạp lịch sử mẫu.", user.getEmail());
			return Optional.empty();
		}
		Run run = run(user, loadContent());
		int sessionCount = seedSessions(run, 0, DAYS - 1, STREAK_DAYS, 0.12, 0.5, true);
		int rated = seedFlashcards(run);
		int attemptCount = seedAttempts(run, MAIN_ATTEMPTS, DAYS - 2, 1);
		int writing = seedWriting(run);
		int speaking = seedSpeaking(run);
		int chats = seedChats(run);
		Report report = new Report(sessionCount, rated, attemptCount, writing, speaking, chats);
		log.info("Đã nạp lịch sử học mẫu cho {}: {}", user.getEmail(), report);
		return Optional.of(report);
	}

	/**
	 * Nạp lịch sử nhẹ cho người dùng demo đang hoạt động: phiên học thưa và vài lượt làm bài, chỉ trong khoảng từ
	 * ngày tạo tài khoản tới lần hoạt động cuối. Người bị khoá hoặc im lặng quá 30 ngày được bỏ qua.
	 *
	 * @return số người dùng đã nạp
	 */
	@Transactional
	public int seedDemoUsers() {
		Content content = loadContent();
		int seeded = 0;
		int sessionCount = 0;
		int attemptCount = 0;
		for (User user : users.findByEmailEndingWithAndStatusOrderByEmail(DemoUserSeedService.EMAIL_SUFFIX,
				AccountStatus.ACTIVE)) {
			if (user.getLastActiveAt() == null || sessions.existsByUserId(user.getId())) {
				continue;
			}
			Run run = run(user, content);
			int newest = daysAgo(run, user.getLastActiveAt());
			if (newest > ACTIVE_WINDOW_DAYS) {
				continue;
			}
			int oldest = Math.min(DAYS - 1, Math.max(newest, daysAgo(run, user.getCreatedAt())));
			sessionCount += seedSessions(run, newest, oldest, 1, 0.55, 0.8, false);
			attemptCount += seedAttempts(run, 3 + run.random.nextInt(6), oldest, newest);
			seeded++;
		}
		log.info("Đã nạp lịch sử nhẹ cho {} người dùng demo ({} phiên học, {} lượt làm bài).", seeded, sessionCount,
				attemptCount);
		return seeded;
	}

	/** Xoá lịch sử của học viên mẫu chính để nạp lại (dùng ở chế độ {@code reset-demo}); người dùng khác giữ nguyên. */
	@Transactional
	public void deleteMainHistory() {
		users.findByEmail(DEMO_EMAIL).ifPresent(user -> {
			for (String table : HISTORY_TABLES) {
				em.createNativeQuery("delete from " + table + " where user_id = :id").setParameter("id", user.getId())
						.executeUpdate();
			}
			log.info("Đã xoá lịch sử học của {}.", DEMO_EMAIL);
		});
	}

	// --- Dựng ngữ cảnh -------------------------------------------------------------------------------

	private Run run(User user, Content content) {
		return new Run(user, studyTime.zoneOf(user.getId()), clock.instant(), content, clock);
	}

	private Content loadContent() {
		return new Content(active(decks.findAll()), active(listeningLessons.findAll()), active(readingLessons.findAll()),
				active(exams.findAll()), active(writingPrompts.findAll()), active(speakingLessons.findAll()));
	}

	private static <T extends ContentEntity> List<T> active(List<T> all) {
		return all.stream().filter(c -> c.getStatus() == ContentStatus.ACTIVE)
				.sorted(Comparator.comparing(ContentEntity::getCreatedAt).thenComparing(ContentEntity::getId)).toList();
	}

	private static int daysAgo(Run run, Instant instant) {
		return (int) Math.max(0, ChronoUnit.DAYS.between(instant.atZone(run.zone).toLocalDate(), run.today));
	}

	/** Không cho mốc thời gian rơi vào tương lai (hôm nay, giờ hiện tại còn sớm hơn giờ mẫu). */
	private static Instant at(Run run, LocalDate day, LocalTime time) {
		Instant start = day.atTime(time).atZone(run.zone).toInstant();
		Instant limit = run.now.minus(Duration.ofHours(1));
		return start.isAfter(limit) ? limit : start;
	}

	// --- Phiên học ----------------------------------------------------------------------------------

	/**
	 * Mỗi ngày trong [newestDay, oldestDay] (tính lùi từ hôm nay) có một phiên, trừ khi bị bỏ qua ngẫu nhiên với xác
	 * suất riêng cho ngày thường/cuối tuần. {@code forcedDays} ngày mới nhất luôn có phiên (tạo chuỗi ngày liền).
	 */
	private int seedSessions(Run run, int newestDay, int oldestDay, int forcedDays, double weekdaySkip,
			double weekendSkip, boolean extras) {
		List<StudySession> rows = new ArrayList<>();
		for (int d = newestDay; d <= oldestDay; d++) {
			LocalDate day = run.today.minusDays(d);
			boolean weekend = day.getDayOfWeek().getValue() >= 6;
			boolean forced = d - newestDay < forcedDays;
			if (!forced && run.random.nextDouble() < (weekend ? weekendSkip : weekdaySkip)) {
				continue;
			}
			StudySkill skill = SKILL_MIX[run.random.nextInt(SKILL_MIX.length)];
			int minutes = weekend ? 8 + run.random.nextInt(18) : 15 + run.random.nextInt(31);
			LocalTime time = STUDY_TIMES[run.random.nextInt(STUDY_TIMES.length)];
			rows.add(session(run, skill, at(run, day, time), minutes * 60 + run.random.nextInt(60)));
			if (extras && !weekend && d > 0 && run.random.nextInt(3) == 0) {
				rows.add(session(run, StudySkill.VOCABULARY, at(run, day, LocalTime.of(6, 45)), 600));
			}
		}
		if (extras) {
			// Sát nửa đêm giờ địa phương: khác ngày nếu (sai) gom theo UTC.
			rows.add(session(run, StudySkill.READING, at(run, run.today.minusDays(1), LocalTime.of(23, 30)), 25 * 60));
			rows.add(session(run, StudySkill.LISTENING, at(run, run.today.minusDays(2), LocalTime.of(0, 10)), 20 * 60));
		}
		sessions.saveAll(rows);
		return rows.size();
	}

	private StudySession session(Run run, StudySkill skill, Instant start, int seconds) {
		StudySession s = new StudySession();
		s.setUser(run.user);
		s.setSkill(skill);
		s.setRefId(pickRef(run, skill));
		s.setStartedAt(start);
		s.setActiveSeconds(seconds);
		Instant end = start.plusSeconds(seconds);
		s.setLastHeartbeatAt(end.isAfter(run.now) ? run.now : end);
		return s;
	}

	/** Chat không gắn với nội dung cụ thể nên {@code refId} để trống. */
	private static UUID pickRef(Run run, StudySkill skill) {
		Content c = run.content;
		List<? extends ContentEntity> pool = switch (skill) {
			case VOCABULARY -> c.decks();
			case LISTENING -> c.listening();
			case READING -> c.reading();
			case EXAM -> c.exams();
			case WRITING -> c.writing();
			case SPEAKING -> c.speaking();
			case CHAT -> List.of();
		};
		return pool.isEmpty() ? null : pool.get(run.random.nextInt(pool.size())).getId();
	}

	// --- Tiến độ thẻ ---------------------------------------------------------------------------------

	/**
	 * Sáu bộ thẻ đầu học ở các mức khác nhau: bộ đầu học xong và phần lớn đã thuộc, bộ sau càng ít thẻ và ít thuộc.
	 * Thẻ chưa có dòng nào là "mới". Hạn ôn rải đều; thẻ chưa nhớ và một phần thẻ khác đến hạn hôm nay.
	 */
	private int seedFlashcards(Run run) {
		double[] coverage = { 1.0, 0.85, 0.6, 0.5, 0.35, 0.2 };
		double[] mastery = { 0.8, 0.65, 0.5, 0.4, 0.3, 0.2 };
		List<UserFlashcardProgress> rows = new ArrayList<>();
		List<FlashcardDeck> deckList = run.content.decks();
		for (int d = 0; d < coverage.length && d < deckList.size(); d++) {
			List<Flashcard> deckCards = cards.findByDeckIdOrderBySortOrderAscIdAsc(deckList.get(d).getId());
			int count = (int) Math.ceil(deckCards.size() * coverage[d]);
			for (int i = 0; i < count; i++) {
				rows.add(progressRow(run, deckCards.get(i), mastery[d]));
			}
		}
		progress.saveAll(rows);
		return rows.size();
	}

	private UserFlashcardProgress progressRow(Run run, Flashcard card, double mastery) {
		Random random = run.random;
		double x = random.nextDouble();
		RecallLevel level = x < mastery ? RecallLevel.REMEMBERED
				: x < mastery + (1 - mastery) * 0.6 ? RecallLevel.ALMOST_REMEMBERED : RecallLevel.NOT_REMEMBERED;
		int reviewCount;
		int interval;
		switch (level) {
			case REMEMBERED -> {
				reviewCount = 2 + random.nextInt(4);
				interval = reviewCount >= 4 ? 30 : reviewCount == 3 ? 14 : 7;
			}
			case ALMOST_REMEMBERED -> {
				reviewCount = 1 + random.nextInt(3);
				interval = 3;
			}
			default -> {
				reviewCount = 1 + random.nextInt(2);
				interval = 0;
			}
		}
		Instant last;
		Instant next;
		if (interval == 0) {
			last = run.now.minus(Duration.ofHours(1 + random.nextInt(72)));
			next = last.plus(Duration.ofMinutes(10));
		} else {
			// Hạn ôn từ trễ 20 giờ tới trọn một chu kỳ nữa; lần ôn cuối luôn nằm trong quá khứ.
			next = run.now.plus(Duration.ofHours(random.nextInt(interval * 24 + 20) - 20));
			last = next.minus(Duration.ofDays(interval));
		}
		UserFlashcardProgress p = new UserFlashcardProgress();
		p.setUser(run.user);
		p.setFlashcard(card);
		p.setRecallLevel(level);
		p.setReviewCount(reviewCount);
		p.setIntervalDays(interval);
		p.setLastReviewedAt(last);
		p.setNextReviewAt(next);
		return p;
	}

	// --- Lượt làm bài -------------------------------------------------------------------------------

	private enum Kind { LISTENING, READING, EXAM }

	/**
	 * {@code count} lượt Nghe/Đọc/Kiểm tra rải đều từ {@code oldestDay} tới {@code newestDay}; tỉ lệ đúng tăng dần theo
	 * thời gian (kèm nhiễu nhỏ) nên điểm thang 10 đi lên. Chấm bằng đúng bộ chấm điểm thật.
	 */
	private int seedAttempts(Run run, int count, int oldestDay, int newestDay) {
		Kind[] pattern = { Kind.LISTENING, Kind.READING, Kind.LISTENING, Kind.READING, Kind.EXAM };
		Map<Kind, Integer> used = new HashMap<>();
		int saved = 0;
		for (int i = 0; i < count; i++) {
			double progressRatio = count > 1 ? i / (double) (count - 1) : 0.5;
			int day = oldestDay - (int) Math.round((oldestDay - newestDay) * progressRatio);
			double ratio = Math.min(1.0, Math.max(0.2, 0.45 + 0.42 * progressRatio + (run.random.nextDouble() - 0.5) * 0.14));
			Instant submittedAt = at(run, run.today.minusDays(day),
					LocalTime.of(18 + run.random.nextInt(4), run.random.nextInt(60)));
			Kind kind = pattern[i % pattern.length];
			int turn = used.merge(kind, 1, Integer::sum) - 1;
			saved += switch (kind) {
				case LISTENING -> run.content.listening().isEmpty() ? 0 : saveListening(run,
						run.content.listening().get(turn % run.content.listening().size()), ratio, submittedAt);
				case READING -> run.content.reading().isEmpty() ? 0 : saveReading(run,
						run.content.reading().get(turn % run.content.reading().size()), ratio, submittedAt);
				case EXAM -> run.content.exams().isEmpty() ? 0 : saveExam(run,
						run.content.exams().get(turn % run.content.exams().size()), ratio, submittedAt);
			};
		}
		return saved;
	}

	private int saveListening(Run run, ListeningLesson lesson, double ratio, Instant submittedAt) {
		List<Question> qs = run.questionCache.computeIfAbsent(lesson.getId(),
				id -> questions.findByListeningLessonIdOrderBySortOrder(id));
		return save(run, qs, ratio, submittedAt, a -> a.setListeningLesson(lesson));
	}

	private int saveReading(Run run, ReadingLesson lesson, double ratio, Instant submittedAt) {
		List<Question> qs = run.questionCache.computeIfAbsent(lesson.getId(),
				id -> questions.findByReadingLessonIdOrderBySortOrder(id));
		return save(run, qs, ratio, submittedAt, a -> a.setReadingLesson(lesson));
	}

	private int saveExam(Run run, Exam exam, double ratio, Instant submittedAt) {
		List<Question> qs = run.questionCache.computeIfAbsent(exam.getId(),
				id -> questions.findByExamIdOrderBySortOrder(id));
		return save(run, qs, ratio, submittedAt, a -> a.setExam(exam));
	}

	/** Trả lời đúng {@code ratio} số câu đầu, sai phần còn lại, rồi chấm bằng đúng bộ chấm điểm thật. */
	private int save(Run run, List<Question> qs, double ratio, Instant submittedAt,
			Consumer<PracticeAttempt> setParent) {
		if (qs.isEmpty()) {
			return 0;
		}
		int correctTarget = (int) Math.round(qs.size() * ratio);
		List<AnswerSubmission> submitted = new ArrayList<>();
		for (int i = 0; i < qs.size(); i++) {
			AnswerSubmission answer = answer(qs.get(i), i < correctTarget);
			if (answer != null) {
				submitted.add(answer);
			}
		}
		Grading grading = grader.grade(qs, submitted);
		int durationSeconds = qs.size() * (30 + run.random.nextInt(30));

		PracticeAttempt attempt = new PracticeAttempt();
		attempt.setUser(run.user);
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

	// --- Luyện viết ---------------------------------------------------------------------------------

	/** Bài viết mẫu gắn với đề cùng tên; bài cuối ở trạng thái {@code NEEDS_RETRY} (chưa có phản hồi). */
	private int seedWriting(Run run) {
		Map<String, WritingPrompt> byTitle = run.content.writing().stream()
				.collect(Collectors.toMap(WritingPrompt::getTitleEn, p -> p, (a, b) -> a));
		int count = 0;
		for (Essay essay : HistorySeedContent.ESSAYS) {
			WritingPrompt prompt = byTitle.get(essay.titleEn());
			if (prompt == null) {
				continue;
			}
			WritingSubmission submission = new WritingSubmission();
			submission.setUser(run.user);
			submission.setPrompt(prompt);
			submission.setContent(essay.content());
			submission.setWordCount(essay.content().trim().split("\\s+").length);
			submission.setStatus(essay.needsRetry() ? SubmissionStatus.NEEDS_RETRY : SubmissionStatus.GRADED);
			submission.setSubmittedAt(at(run, run.today.minusDays(essay.daysAgo()), LocalTime.of(21, 5)));
			submissions.save(submission);
			if (!essay.needsRetry()) {
				feedbacks.save(feedback(submission, essay));
			}
			count++;
		}
		return count;
	}

	private static AiFeedback feedback(WritingSubmission submission, Essay essay) {
		BigDecimal grammar = score(essay.grade() - 0.3);
		BigDecimal vocabulary = score(essay.grade() + 0.2);
		BigDecimal expression = score(essay.grade() - 0.1);
		AiFeedback feedback = new AiFeedback();
		feedback.setSubmission(submission);
		feedback.setGrammarScore(grammar);
		feedback.setVocabularyScore(vocabulary);
		feedback.setExpressionScore(expression);
		feedback.setOverallScore(average(grammar, vocabulary, expression));
		feedback.setSummary(essay.summary());
		feedback.setModelName(FAKE_WRITING_MODEL);
		feedback.setIssues(new ArrayList<>(essay.issues()));
		return feedback;
	}

	// --- Luyện nói ----------------------------------------------------------------------------------

	/** Tối đa tám lượt trên tám bài đầu, điểm tăng dần; lượt thứ sáu {@code FAILED} (không có điểm). */
	private int seedSpeaking(Run run) {
		int[] daysAgo = { 85, 70, 58, 47, 33, 21, 12, 4 };
		int failedIndex = 5;
		List<SpeakingLesson> lessons = run.content.speaking();
		int count = 0;
		for (int i = 0; i < daysAgo.length && i < lessons.size(); i++) {
			SpeakingLesson lesson = lessons.get(i);
			List<SpeakingPrompt> prompts = lesson.getPrompts();
			int duration = Math.max(30, prompts.size() * (25 + run.random.nextInt(20)));
			Instant submittedAt = at(run, run.today.minusDays(daysAgo[i]), LocalTime.of(20, 30));

			SpeakingAttempt attempt = new SpeakingAttempt();
			attempt.setUser(run.user);
			attempt.setLesson(lesson);
			attempt.setSubmittedAt(submittedAt);
			attempt.setStartedAt(submittedAt.minusSeconds(duration));
			attempt.setDurationSeconds(duration);
			if (i == failedIndex) {
				attempt.setStatus(SpeakingAttemptStatus.FAILED);
			} else {
				double base = 5.5 + 2.8 * (i / (double) (daysAgo.length - 1)) + (run.random.nextDouble() - 0.5) * 0.4;
				fillSpeakingScores(attempt, base);
				attempt.setImprovements(improvements(i));
				attempt.setPromptFeedback(promptFeedback(run, prompts, base));
				attempt.setModelName(FAKE_SPEAKING_MODEL);
				attempt.setStatus(SpeakingAttemptStatus.GRADED);
			}
			speakingAttempts.save(attempt);
			count++;
		}
		return count;
	}

	private static void fillSpeakingScores(SpeakingAttempt attempt, double base) {
		BigDecimal pronunciation = score(base - 0.3);
		BigDecimal vocabulary = score(base + 0.2);
		BigDecimal grammar = score(base);
		BigDecimal fluency = score(base - 0.2);
		BigDecimal relevance = score(base + 0.4);
		attempt.setPronunciationScore(pronunciation);
		attempt.setVocabularyScore(vocabulary);
		attempt.setGrammarScore(grammar);
		attempt.setFluencyScore(fluency);
		attempt.setRelevanceScore(relevance);
		attempt.setOverallScore(average(pronunciation, vocabulary, grammar, fluency, relevance));
	}

	private static List<String> improvements(int index) {
		List<String> all = HistorySeedContent.SPEAKING_IMPROVEMENTS;
		List<String> picked = new ArrayList<>();
		for (int k = 0; k < 3; k++) {
			picked.add(all.get((index + k) % all.size()));
		}
		return picked;
	}

	/** Transcript là câu mẫu, thỉnh thoảng rớt một từ; từ phát âm chưa chuẩn chọn trong các từ dài. */
	private static List<SpeakingPromptFeedback> promptFeedback(Run run, List<SpeakingPrompt> prompts, double base) {
		List<SpeakingPromptFeedback> result = new ArrayList<>();
		for (SpeakingPrompt prompt : prompts) {
			List<String> words = new ArrayList<>(List.of(prompt.text().split("\\s+")));
			if (words.size() > 3 && run.random.nextInt(100) < 35) {
				words.remove(1 + run.random.nextInt(words.size() - 1));
			}
			List<String> hard = Arrays.stream(prompt.text().split("\\s+"))
					.map(w -> w.replaceAll("[^A-Za-z']", "").toLowerCase()).filter(w -> w.length() >= 6).distinct()
					.collect(Collectors.toCollection(ArrayList::new));
			List<String> mispronounced = new ArrayList<>();
			for (int n = run.random.nextInt(3); n > 0 && !hard.isEmpty(); n--) {
				mispronounced.add(hard.remove(run.random.nextInt(hard.size())));
			}
			BigDecimal score = score(base + (run.random.nextDouble() - 0.5) * 1.2);
			result.add(new SpeakingPromptFeedback(prompt.id(), String.join(" ", words), score, mispronounced,
					HistorySeedContent.speakingComment(score.doubleValue())));
		}
		return result;
	}

	// --- Chat ---------------------------------------------------------------------------------------

	private int seedChats(Run run) {
		List<UUID> messageIds = new ArrayList<>();
		List<Instant> messageTimes = new ArrayList<>();
		List<UUID> conversationIds = new ArrayList<>();
		List<Instant> conversationTimes = new ArrayList<>();
		for (Chat chat : HistorySeedContent.CHATS) {
			Instant base = chat.daysAgo() == 0 ? run.now.minus(Duration.ofHours(3))
					: at(run, run.today.minusDays(chat.daysAgo()), LocalTime.of(20, 15));
			List<ChatTurn> turns = chat.turns();
			String first = turns.get(0).content();

			ChatConversation conversation = new ChatConversation();
			conversation.setUser(run.user);
			conversation.setTitle(first.length() > 200 ? first.substring(0, 200) : first);
			conversation.setLastMessageAt(base.plusSeconds(90L * (turns.size() - 1)));
			conversations.save(conversation);
			conversationIds.add(conversation.getId());
			conversationTimes.add(base);

			for (int k = 0; k < turns.size(); k++) {
				ChatTurn turn = turns.get(k);
				ChatMessage message = new ChatMessage();
				message.setConversation(conversation);
				message.setRole(turn.role());
				message.setContent(turn.content());
				if (turn.role() == ChatRole.ASSISTANT) {
					message.setLinks(links(run, turn.skills()));
					message.setRefusal(turn.refusal());
					message.setModelName(FAKE_CHAT_MODEL);
					message.setPromptTokens(tokens(turns.get(k - 1).content()));
					message.setCompletionTokens(tokens(turn.content()));
				}
				messages.save(message);
				messageIds.add(message.getId());
				messageTimes.add(base.plusSeconds(90L * k));
			}
		}
		// created_at không sửa được qua entity: đặt lại bằng SQL để thứ tự tin nhắn và ngày hội thoại đúng.
		em.flush();
		for (int i = 0; i < messageIds.size(); i++) {
			em.createNativeQuery("update chat_messages set created_at = :t where id = :id")
					.setParameter("t", messageTimes.get(i)).setParameter("id", messageIds.get(i)).executeUpdate();
		}
		for (int i = 0; i < conversationIds.size(); i++) {
			em.createNativeQuery("update chat_conversations set created_at = :t, updated_at = :t where id = :id")
					.setParameter("t", conversationTimes.get(i)).setParameter("id", conversationIds.get(i))
					.executeUpdate();
		}
		return conversationIds.size();
	}

	/** Cùng quy tắc với gợi ý của Chat thật: bài ACTIVE đầu tiên của mỗi nhóm, đường dẫn đúng route của FE. */
	private static List<ChatSuggestionLink> links(Run run, List<StudySkill> skills) {
		List<ChatSuggestionLink> links = new ArrayList<>();
		for (StudySkill skill : skills) {
			switch (skill) {
				case LISTENING -> first(run.content.listening(), "/listening/", skill, links);
				case READING -> first(run.content.reading(), "/reading/", skill, links);
				case WRITING -> first(run.content.writing(), "/writing/", skill, links);
				case SPEAKING -> first(run.content.speaking(), "/speaking/", skill, links);
				case VOCABULARY -> first(run.content.decks(), "/flashcard/", skill, links);
				default -> {
				}
			}
		}
		return links;
	}

	private static void first(List<? extends ContentEntity> pool, String prefix, StudySkill skill,
			List<ChatSuggestionLink> links) {
		if (!pool.isEmpty()) {
			ContentEntity c = pool.get(0);
			links.add(new ChatSuggestionLink(c.getTitleVi(), prefix + c.getId(), skill));
		}
	}

	/** Ước lượng thô (2 token mỗi từ) đủ cho số liệu mẫu. */
	private static int tokens(String text) {
		return text.trim().split("\\s+").length * 2;
	}

	// --- Điểm ---------------------------------------------------------------------------------------

	private static BigDecimal score(double value) {
		return BigDecimal.valueOf(Math.min(10.0, Math.max(0.0, value))).setScale(1, RoundingMode.HALF_UP);
	}

	private static BigDecimal average(BigDecimal... values) {
		BigDecimal sum = BigDecimal.ZERO;
		for (BigDecimal v : values) {
			sum = sum.add(v);
		}
		return sum.divide(BigDecimal.valueOf(values.length), 1, RoundingMode.HALF_UP);
	}
}
