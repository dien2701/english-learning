package vn.enlearning.backend.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserFlashcardProgress;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.security.JwtService;
import vn.enlearning.backend.seed.HistorySeedService;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Dashboard và Thống kê qua toàn bộ chồng Security → Controller → Service → JPA trên MySQL thật. Dữ liệu tự dựng
 * trong giao dịch test (tự rollback), nên không phụ thuộc dữ liệu seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardApiTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private FlashcardDeckRepository decks;
	@Autowired
	private FlashcardRepository cards;
	@Autowired
	private UserFlashcardProgressRepository progress;
	@Autowired
	private ListeningLessonRepository listeningLessons;
	@Autowired
	private ExamRepository exams;
	@Autowired
	private PracticeAttemptRepository attempts;
	@Autowired
	private StudySessionRepository sessions;
	@Autowired
	private HistorySeedService historySeed;
	@Autowired
	private QuestionRepository questions;

	private String tag;
	private User userA;
	private User userB;
	private String tokenA;
	private String tokenB;
	private Topic topic;

	@BeforeEach
	void setUp() {
		tag = "T" + UUID.randomUUID().toString().replace("-", "");
		userA = newUser();
		userB = newUser();
		tokenA = "Bearer " + jwt.issueAccessToken(userA);
		tokenB = "Bearer " + jwt.issueAccessToken(userB);

		topic = new Topic();
		topic.setSlug(tag);
		topic.setNameVi("Chủ đề " + tag);
		topic.setNameEn("Topic " + tag);
		topics.saveAndFlush(topic);
	}

	// --- dữ liệu mẫu ----------------------------------------------------------------------------

	private User newUser() {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Trịnh Xuân Diện");
		return users.saveAndFlush(user);
	}

	private void fillContent(ContentEntity content, String title) {
		content.setTitleVi(title);
		content.setTitleEn(title);
		content.setLevel(Level.BEGINNER);
		content.setStatus(ContentStatus.ACTIVE);
	}

	/** Bộ thẻ 4 thẻ; {@code remembered} thẻ đầu đã thuộc, {@code reviewedAt} là lần ôn gần nhất. */
	private FlashcardDeck deckWithProgress(User user, int remembered, Instant reviewedAt) {
		FlashcardDeck deck = new FlashcardDeck();
		fillContent(deck, "Bo the " + tag);
		deck.setTopic(topic);
		decks.saveAndFlush(deck);
		for (int i = 0; i < 4; i++) {
			Flashcard card = new Flashcard();
			card.setDeck(deck);
			card.setWord("word" + i);
			card.setMeaningVi("nghia " + i);
			card.setSortOrder(i + 1);
			cards.saveAndFlush(card);
			if (i < remembered) {
				UserFlashcardProgress p = new UserFlashcardProgress();
				p.setUser(user);
				p.setFlashcard(card);
				p.setRecallLevel(RecallLevel.REMEMBERED);
				p.setIntervalDays(3);
				p.setLastReviewedAt(reviewedAt);
				p.setNextReviewAt(reviewedAt.plus(3, ChronoUnit.DAYS));
				progress.saveAndFlush(p);
			}
		}
		return deck;
	}

	private ListeningLesson listening() {
		ListeningLesson lesson = new ListeningLesson();
		fillContent(lesson, "Nghe " + tag);
		lesson.setTopic(topic);
		lesson.setDurationSeconds(60);
		lesson.setTranscript("transcript");
		return listeningLessons.saveAndFlush(lesson);
	}

	private Exam exam() {
		Exam exam = new Exam();
		fillContent(exam, "De " + tag);
		exam.setTimeLimitMinutes(0);
		return exams.saveAndFlush(exam);
	}

	private PracticeAttempt attempt(User user, ContentEntity parent, String score, Instant submittedAt) {
		PracticeAttempt a = new PracticeAttempt();
		a.setUser(user);
		if (parent instanceof ListeningLesson l) {
			a.setListeningLesson(l);
		} else {
			a.setExam((Exam) parent);
		}
		a.setStatus(AttemptStatus.COMPLETED);
		a.setStartedAt(submittedAt.minusSeconds(120));
		a.setSubmittedAt(submittedAt);
		a.setDurationSeconds(120);
		a.setCorrectCount(4);
		a.setTotalQuestions(5);
		a.setScore(new BigDecimal(score));
		return attempts.saveAndFlush(a);
	}

	private ResultActions call(String token, String url) throws Exception {
		return mvc.perform(get(url).header("Authorization", token));
	}

	// --- bảo mật và đầu vào -----------------------------------------------------------------------

	@Test
	@DisplayName("Không có token: cả ba endpoint trả 401")
	void requiresAuthentication() throws Exception {
		mvc.perform(get("/dashboard/summary")).andExpect(status().isUnauthorized());
		mvc.perform(get("/dashboard/study-time")).andExpect(status().isUnauthorized());
		mvc.perform(get("/statistics/overview")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("period hoặc status sai là 400 có fieldErrorKeys, không phải 500")
	void rejectsBadParameters() throws Exception {
		call(tokenA, "/dashboard/study-time?period=year").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.period").value("errors.badRequest"));
		call(tokenA, "/dashboard/summary?status=DONE").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.status").value("errors.badRequest"));
	}

	// --- người chưa học gì -------------------------------------------------------------------------

	@Test
	@DisplayName("Người mới: summary rỗng, biểu đồ toàn 0, thống kê rỗng, tất cả đều 200")
	void brandNewUserSeesEmptyState() throws Exception {
		call(tokenA, "/dashboard/summary").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.greetingName").value("Diện"))
				.andExpect(jsonPath("$.data.continueLearning").value(nullValue()))
				.andExpect(jsonPath("$.data.attendedLessons", hasSize(0)));

		call(tokenA, "/dashboard/study-time?period=WEEK").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.period").value("WEEK"))
				.andExpect(jsonPath("$.data.points", hasSize(7)))
				.andExpect(jsonPath("$.data.points[*].minutes", everyItem(equalTo(0))))
				.andExpect(jsonPath("$.data.totalMinutes").value(0))
				.andExpect(jsonPath("$.data.previousTotalMinutes").value(0))
				.andExpect(jsonPath("$.data.changePercent").value(0));
		call(tokenA, "/dashboard/study-time?period=month").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.period").value("MONTH"))
				.andExpect(jsonPath("$.data.points", hasSize(4)))
				.andExpect(jsonPath("$.data.points[0].label.vi").value("Tuần 1"))
				.andExpect(jsonPath("$.data.points[0].label.en").value("Week 1"));
		// Không có period: mặc định là tuần.
		call(tokenA, "/dashboard/study-time").andExpect(jsonPath("$.data.period").value("WEEK"));

		call(tokenA, "/statistics/overview").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalMinutes").value(0))
				.andExpect(jsonPath("$.data.weekPoints", hasSize(7)))
				.andExpect(jsonPath("$.data.monthPoints", hasSize(4)))
				.andExpect(jsonPath("$.data.skills", hasSize(3)))
				.andExpect(jsonPath("$.data.skills[*].attempts", everyItem(equalTo(0))))
				.andExpect(jsonPath("$.data.skills[*].averageScore", everyItem(equalTo(0.0))))
				.andExpect(jsonPath("$.data.recentAttempts", hasSize(0)));
	}

	// --- có dữ liệu -------------------------------------------------------------------------------

	@Test
	@DisplayName("Summary: bộ thẻ học dở là 'Tiếp tục học', bài Nghe/Kiểm tra đã nộp là hoàn thành; lọc theo trạng thái")
	void summaryWithHistory() throws Exception {
		Instant now = Instant.now();
		FlashcardDeck deck = deckWithProgress(userA, 2, now.minus(1, ChronoUnit.HOURS));
		ListeningLesson lesson = listening();
		Exam exam = exam();
		PracticeAttempt listeningAttempt = attempt(userA, lesson, "8.0", now.minus(1, ChronoUnit.DAYS));
		// Làm lại lần hai: chỉ lượt mới nhất được hiển thị.
		attempt(userA, exam, "6.0", now.minus(3, ChronoUnit.DAYS));
		PracticeAttempt examRetake = attempt(userA, exam, "9.0", now.minus(2, ChronoUnit.DAYS));

		call(tokenA, "/dashboard/summary").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.continueLearning.id").value(deck.getId().toString()))
				.andExpect(jsonPath("$.data.continueLearning.skill").value("VOCABULARY"))
				.andExpect(jsonPath("$.data.continueLearning.progress").value(50))
				.andExpect(jsonPath("$.data.continueLearning.completedItems").value(2))
				.andExpect(jsonPath("$.data.continueLearning.totalItems").value(4))
				.andExpect(jsonPath("$.data.continueLearning.resumePath")
						.value("/flashcard/" + deck.getId() + "/study"))
				.andExpect(jsonPath("$.data.attendedLessons", hasSize(3)))
				// Sắp mới nhất trước: bộ thẻ (1 giờ), Nghe (1 ngày), Kiểm tra (2 ngày).
				.andExpect(jsonPath("$.data.attendedLessons[0].skill").value("VOCABULARY"))
				.andExpect(jsonPath("$.data.attendedLessons[0].status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.data.attendedLessons[0].score").doesNotExist())
				.andExpect(jsonPath("$.data.attendedLessons[1].skill").value("LISTENING"))
				.andExpect(jsonPath("$.data.attendedLessons[1].status").value("COMPLETED"))
				.andExpect(jsonPath("$.data.attendedLessons[1].score").value(8.0))
				.andExpect(jsonPath("$.data.attendedLessons[1].detailPath")
						.value("/listening/result/" + listeningAttempt.getId()))
				.andExpect(jsonPath("$.data.attendedLessons[2].skill").value("EXAM"))
				.andExpect(jsonPath("$.data.attendedLessons[2].score").value(9.0))
				.andExpect(jsonPath("$.data.attendedLessons[2].detailPath")
						.value("/exam/result/" + examRetake.getId()));

		call(tokenA, "/dashboard/summary?status=COMPLETED").andExpect(jsonPath("$.data.attendedLessons", hasSize(2)))
				.andExpect(jsonPath("$.data.attendedLessons[*].status", everyItem(equalTo("COMPLETED"))))
				// Lọc danh sách không làm mất banner "Tiếp tục học".
				.andExpect(jsonPath("$.data.continueLearning.id").value(deck.getId().toString()));
		call(tokenA, "/dashboard/summary?status=in_progress")
				.andExpect(jsonPath("$.data.attendedLessons", hasSize(1)));
	}

	@Test
	@DisplayName("Bộ thẻ thuộc hết là hoàn thành và không còn là 'Tiếp tục học'; bộ thẻ INACTIVE bị ẩn")
	void completedAndInactiveDecks() throws Exception {
		FlashcardDeck done = deckWithProgress(userA, 4, Instant.now().minus(1, ChronoUnit.HOURS));
		call(tokenA, "/dashboard/summary")
				.andExpect(jsonPath("$.data.continueLearning").value(nullValue()))
				.andExpect(jsonPath("$.data.attendedLessons", hasSize(1)))
				.andExpect(jsonPath("$.data.attendedLessons[0].status").value("COMPLETED"))
				.andExpect(jsonPath("$.data.attendedLessons[0].progress").value(100));

		done.setStatus(ContentStatus.INACTIVE);
		decks.saveAndFlush(done);
		call(tokenA, "/dashboard/summary").andExpect(jsonPath("$.data.attendedLessons", hasSize(0)));
	}

	@Test
	@DisplayName("Thống kê: điểm trung bình, số lượt, chênh lệch 30 ngày, lượt gần đây; số liệu không lẫn sang người khác")
	void statisticsAndIsolation() throws Exception {
		Instant now = Instant.now();
		ListeningLesson lesson = listening();
		// 30 ngày gần nhất: 8.0; 30 ngày trước đó: 6.0 -> trung bình 7.0, chênh +2.0.
		attempt(userA, lesson, "8.0", now.minus(2, ChronoUnit.DAYS));
		attempt(userA, lesson, "6.0", now.minus(40, ChronoUnit.DAYS));
		// Lượt của người khác không được tính.
		attempt(userB, lesson, "10.0", now.minus(1, ChronoUnit.DAYS));

		call(tokenA, "/statistics/overview").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.skills[0].skill").value("LISTENING"))
				.andExpect(jsonPath("$.data.skills[0].attempts").value(2))
				.andExpect(jsonPath("$.data.skills[0].averageScore").value(7.0))
				.andExpect(jsonPath("$.data.skills[0].change").value(2.0))
				.andExpect(jsonPath("$.data.skills[1].skill").value("READING"))
				.andExpect(jsonPath("$.data.skills[1].attempts").value(0))
				.andExpect(jsonPath("$.data.recentAttempts", hasSize(2)))
				.andExpect(jsonPath("$.data.recentAttempts[0].score").value(8.0));

		call(tokenB, "/statistics/overview")
				.andExpect(jsonPath("$.data.skills[0].attempts").value(1))
				.andExpect(jsonPath("$.data.skills[0].averageScore").value(10.0))
				// Chỉ có khoảng gần đây, khoảng trước trống nên không so sánh được.
				.andExpect(jsonPath("$.data.skills[0].change").value(0.0));
		call(tokenB, "/dashboard/summary").andExpect(jsonPath("$.data.attendedLessons", hasSize(1)));
	}

	@Test
	@DisplayName("Phiên học hôm nay hiện trong biểu đồ tuần, tổng tuần khớp thống kê")
	void studyTimeShowsRecentSession() throws Exception {
		StudySession s = new StudySession();
		s.setUser(userA);
		s.setSkill(StudySkill.VOCABULARY);
		Instant start = Instant.now().minus(30, ChronoUnit.MINUTES);
		s.setStartedAt(start);
		s.setLastHeartbeatAt(start.plusSeconds(1800));
		s.setActiveSeconds(1800);
		sessions.saveAndFlush(s);

		call(tokenA, "/dashboard/study-time?period=WEEK")
				.andExpect(jsonPath("$.data.totalMinutes").value(30))
				.andExpect(jsonPath("$.data.changePercent").value(0));
		call(tokenA, "/statistics/overview").andExpect(jsonPath("$.data.totalMinutes").value(30));
		call(tokenB, "/dashboard/study-time?period=WEEK").andExpect(jsonPath("$.data.totalMinutes").value(0));
	}

	// --- seed lịch sử --------------------------------------------------------------------------

	@Test
	@DisplayName("Seed lịch sử: nạp một lần cho người chưa có phiên học, Dashboard hết rỗng; chạy lại thì bỏ qua")
	void historySeedFillsDashboard() throws Exception {
		deckWithProgress(userB, 0, Instant.now());
		ListeningLesson lesson = listening();
		Question choice = new Question();
		choice.setListeningLesson(lesson);
		choice.setSkill(Skill.LISTENING);
		choice.setKind(QuestionKind.SINGLE_CHOICE);
		choice.setSortOrder(1);
		choice.setContent("Choice?");
		for (int i = 0; i < 2; i++) {
			QuestionOption option = new QuestionOption();
			option.setQuestion(choice);
			option.setSortOrder(i + 1);
			option.setContent("Option " + i);
			option.setCorrect(i == 0);
			choice.getOptions().add(option);
		}
		Question blank = new Question();
		blank.setListeningLesson(lesson);
		blank.setSkill(Skill.LISTENING);
		blank.setKind(QuestionKind.FILL_BLANK);
		blank.setSortOrder(2);
		blank.setContent("Fill ___");
		blank.setAcceptedAnswers(List.of("apple"));
		questions.saveAllAndFlush(List.of(choice, blank));

		HistorySeedService.Report report = historySeed.seedFor(userA).orElseThrow();
		assertThat(report.sessions()).isGreaterThan(30);
		assertThat(report.attempts()).isGreaterThanOrEqualTo(1);
		assertThat(sessions.existsByUserId(userA.getId())).isTrue();
		assertThat(historySeed.seedFor(userA)).isEmpty();

		call(tokenA, "/dashboard/study-time?period=WEEK").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalMinutes", greaterThan(0)));
		call(tokenA, "/dashboard/study-time?period=MONTH").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalMinutes", greaterThan(0)));
		// Người khác không bị nạp theo.
		call(tokenB, "/dashboard/study-time?period=MONTH").andExpect(jsonPath("$.data.totalMinutes").value(0));
	}
}
