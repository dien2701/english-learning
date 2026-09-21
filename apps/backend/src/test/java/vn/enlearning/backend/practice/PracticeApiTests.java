package vn.enlearning.backend.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.security.JwtService;

/**
 * Chạy Security → Controller → Service → JPA trên MySQL thật cho Nghe, Đọc, Kiểm tra và lịch sử làm bài. Dữ liệu
 * tự dựng trong giao dịch test (tự rollback) với tiêu đề mang nhãn ngẫu nhiên, nên không phụ thuộc dữ liệu seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PracticeApiTests {

	private static final String TRANSCRIPT = "SECRET-TRANSCRIPT-TEXT";
	private static final String EXPLANATION = "SECRET-EXPLANATION-TEXT";

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private ListeningLessonRepository listeningLessons;
	@Autowired
	private ReadingLessonRepository readingLessons;
	@Autowired
	private ExamRepository exams;
	@Autowired
	private QuestionRepository questions;

	private String tag;
	private String tokenA;
	private String tokenB;
	private ListeningLesson listening;
	private Question listeningChoice;
	private Question listeningBlank;
	private ReadingLesson readingLesson;
	private Exam exam;
	private Question examListening;
	private Question examReading;

	@BeforeEach
	void setUp() {
		tag = "T" + UUID.randomUUID().toString().replace("-", "");
		tokenA = "Bearer " + jwt.issueAccessToken(newUser());
		tokenB = "Bearer " + jwt.issueAccessToken(newUser());

		Topic topic = new Topic();
		topic.setSlug(tag);
		topic.setNameVi("Chủ đề " + tag);
		topic.setNameEn("Topic " + tag);
		topics.save(topic);

		listening = new ListeningLesson();
		fillContent(listening, "Nghe " + tag);
		listening.setTopic(topic);
		listening.setDurationSeconds(60);
		listening.setTranscript(TRANSCRIPT);
		listeningLessons.saveAndFlush(listening);
		listeningChoice = choice(Skill.LISTENING, 1, "Listening choice?", "Right", "Wrong");
		listeningChoice.setListeningLesson(listening);
		listeningBlank = blank(Skill.LISTENING, 2, "Fill ___", "apple", "Apple pie");
		listeningBlank.setListeningLesson(listening);
		questions.saveAllAndFlush(List.of(listeningChoice, listeningBlank));

		readingLesson = new ReadingLesson();
		fillContent(readingLesson, "Doc " + tag);
		readingLesson.setTopic(topic);
		readingLesson.setWordCount(10);
		readingLesson.setTimeLimitMinutes(1);
		readingLesson.setParagraphs(List.of("Paragraph one.", "Paragraph two."));
		readingLessons.saveAndFlush(readingLesson);
		Question readingQuestion = choice(Skill.READING, 1, "Reading choice?", "Yes", "No");
		readingQuestion.setReadingLesson(readingLesson);
		questions.saveAndFlush(readingQuestion);

		exam = new Exam();
		fillContent(exam, "De " + tag);
		exam.setTimeLimitMinutes(0);
		exams.saveAndFlush(exam);
		examListening = choice(Skill.LISTENING, 1, "Exam listening?", "A", "B");
		examListening.setExam(exam);
		examReading = blank(Skill.READING, 2, "Exam reading ___", "dog");
		examReading.setExam(exam);
		questions.saveAllAndFlush(List.of(examListening, examReading));
	}

	// --- dữ liệu mẫu ----------------------------------------------------------------------------

	private User newUser() {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		return users.saveAndFlush(user);
	}

	private static void fillContent(vn.enlearning.backend.entity.ContentEntity content, String title) {
		content.setTitleVi(title);
		content.setTitleEn(title);
		content.setLevel(Level.BEGINNER);
		content.setStatus(ContentStatus.ACTIVE);
	}

	/** Phương án đầu tiên là đáp án đúng. */
	private static Question choice(Skill skill, int order, String content, String... options) {
		Question q = new Question();
		q.setSkill(skill);
		q.setKind(QuestionKind.SINGLE_CHOICE);
		q.setSortOrder(order);
		q.setContent(content);
		q.setExplanation(EXPLANATION);
		for (int i = 0; i < options.length; i++) {
			QuestionOption o = new QuestionOption();
			o.setQuestion(q);
			o.setSortOrder(i + 1);
			o.setContent(options[i]);
			o.setCorrect(i == 0);
			q.getOptions().add(o);
		}
		return q;
	}

	private static Question blank(Skill skill, int order, String content, String... accepted) {
		Question q = new Question();
		q.setSkill(skill);
		q.setKind(QuestionKind.FILL_BLANK);
		q.setSortOrder(order);
		q.setContent(content);
		q.setExplanation(EXPLANATION);
		q.setAcceptedAnswers(List.of(accepted));
		return q;
	}

	private static UUID correctOption(Question q) {
		return q.getOptions().stream().filter(QuestionOption::isCorrect).findFirst().orElseThrow().getId();
	}

	private static UUID wrongOption(Question q) {
		return q.getOptions().stream().filter(o -> !o.isCorrect()).findFirst().orElseThrow().getId();
	}

	private static String choiceAnswer(Question q, UUID optionId) {
		return "{\"questionId\":\"%s\",\"optionId\":\"%s\"}".formatted(q.getId(), optionId);
	}

	private static String textAnswer(Question q, String text) {
		return "{\"questionId\":\"%s\",\"text\":\"%s\"}".formatted(q.getId(), text);
	}

	private static String body(int seconds, String... answers) {
		return "{\"answers\":[%s],\"durationSeconds\":%d}".formatted(String.join(",", answers), seconds);
	}

	private ResultActions fetch(String url, String token) throws Exception {
		var request = get(url);
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private ResultActions submit(String url, String token, String json) throws Exception {
		var request = post(url).contentType(MediaType.APPLICATION_JSON).content(json);
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private static String json(MvcResult result) throws Exception {
		return result.getResponse().getContentAsString();
	}

	private String submitListeningPerfect(String token) throws Exception {
		MvcResult result = submit("/listening/lessons/" + listening.getId() + "/submit", token,
				body(30, choiceAnswer(listeningChoice, correctOption(listeningChoice)),
						textAnswer(listeningBlank, "APPLE")))
				.andExpect(status().isOk())
				.andReturn();
		return json(result);
	}

	private static String attemptId(String resultJson) {
		var matcher = java.util.regex.Pattern.compile("\"attemptId\":\"([0-9a-f-]{36})\"").matcher(resultJson);
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	// --- danh sách và chi tiết: không lộ đáp án ------------------------------------------------

	@Test
	@DisplayName("Chi tiết Nghe/Đọc/Kiểm tra trước khi nộp không chứa đáp án, giải thích, đáp án điền từ, transcript")
	void detailsNeverLeakAnswers() throws Exception {
		List<String> urls = List.of("/listening/lessons/" + listening.getId(),
				"/reading/lessons/" + readingLesson.getId(), "/exams/" + exam.getId());
		for (String url : urls) {
			String body = json(fetch(url, tokenA).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.questions").isNotEmpty()).andReturn());
			assertThat(body).doesNotContain("isCorrect", "\"correct\"", "correctOptionId", "correctText",
					"acceptedAnswers", "transcript", "explanation", TRANSCRIPT, EXPLANATION, "correctAnswer");
		}
		fetch("/listening/lessons/" + listening.getId(), tokenA)
				.andExpect(jsonPath("$.data.questions", hasSize(2)))
				.andExpect(jsonPath("$.data.questions[0].order").value(1))
				.andExpect(jsonPath("$.data.questions[0].options", hasSize(2)))
				.andExpect(jsonPath("$.data.questions[1].kind").value("FILL_BLANK"))
				.andExpect(jsonPath("$.data.questions[1].options").doesNotExist())
				.andExpect(jsonPath("$.data.isCompleted").value(false))
				.andExpect(jsonPath("$.data.questionCount").value(2));
		fetch("/reading/lessons/" + readingLesson.getId(), tokenA)
				.andExpect(jsonPath("$.data.passage", hasSize(2)))
				.andExpect(jsonPath("$.data.timeLimitMinutes").value(1));
	}

	@Test
	@DisplayName("Danh sách lọc theo từ khoá, có phân trang; đề kiểm tra liệt kê đủ kỹ năng; chưa làm là NOT_TAKEN")
	void listsFilterAndSummarize() throws Exception {
		fetch("/listening/lessons?search=" + tag, tokenA).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].topicName.en").value("Topic " + tag))
				.andExpect(jsonPath("$.data.items[0].questionCount").value(2))
				.andExpect(jsonPath("$.data.items[0].isCompleted").value(false));
		fetch("/reading/lessons?search=" + tag + "&level=BEGINNER", tokenA)
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].wordCount").value(10));
		fetch("/exams?search=" + tag, tokenA)
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].status").value("NOT_TAKEN"))
				.andExpect(jsonPath("$.data.items[0].skills[0]").value("LISTENING"))
				.andExpect(jsonPath("$.data.items[0].skills[1]").value("READING"))
				.andExpect(jsonPath("$.data.items[0].questionCount").value(2));
		fetch("/exams?search=" + tag + "&status=COMPLETED", tokenA).andExpect(jsonPath("$.data.total").value(0));
		fetch("/listening/lessons?level=NOPE", tokenA).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Bài không tồn tại hoặc INACTIVE: 404 ở chi tiết và khi nộp; không có token: 401")
	void unknownInactiveAndUnauthenticated() throws Exception {
		fetch("/listening/lessons/" + UUID.randomUUID(), tokenA).andExpect(status().isNotFound());
		readingLesson.setStatus(ContentStatus.INACTIVE);
		readingLessons.saveAndFlush(readingLesson);
		fetch("/reading/lessons/" + readingLesson.getId(), tokenA).andExpect(status().isNotFound());
		submit("/reading/lessons/" + readingLesson.getId() + "/submit", tokenA, body(1)).andExpect(status().isNotFound());
		fetch("/reading/lessons?search=" + tag, tokenA).andExpect(jsonPath("$.data.total").value(0));

		fetch("/listening/lessons", null).andExpect(status().isUnauthorized());
		fetch("/exams/" + exam.getId(), null).andExpect(status().isUnauthorized());
		fetch("/attempts", null).andExpect(status().isUnauthorized());
		submit("/exams/" + exam.getId() + "/submit", null, body(1)).andExpect(status().isUnauthorized());
	}

	// --- nộp bài --------------------------------------------------------------------------------

	@Test
	@DisplayName("Nộp bài nghe: điểm 10, điền từ không phân biệt hoa/thường, nay mới có đáp án đúng và transcript")
	void submitListening() throws Exception {
		submit("/listening/lessons/" + listening.getId() + "/submit", tokenA,
				body(30, choiceAnswer(listeningChoice, correctOption(listeningChoice)),
						textAnswer(listeningBlank, "APPLE")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.score").value(10.0))
				.andExpect(jsonPath("$.data.correctCount").value(2))
				.andExpect(jsonPath("$.data.wrongCount").value(0))
				.andExpect(jsonPath("$.data.totalQuestions").value(2))
				.andExpect(jsonPath("$.data.skill").value("LISTENING"))
				.andExpect(jsonPath("$.data.timedOut").value(false))
				.andExpect(jsonPath("$.data.transcript").value(TRANSCRIPT))
				.andExpect(jsonPath("$.data.breakdown").doesNotExist())
				.andExpect(jsonPath("$.data.answers[0].userAnswer").value("Right"))
				.andExpect(jsonPath("$.data.answers[0].correctAnswer").value("Right"))
				.andExpect(jsonPath("$.data.answers[0].isCorrect").value(true))
				.andExpect(jsonPath("$.data.answers[0].explanation").value(EXPLANATION))
				.andExpect(jsonPath("$.data.answers[1].correctAnswer").value("apple / Apple pie"));

		fetch("/listening/lessons?search=" + tag, tokenA)
				.andExpect(jsonPath("$.data.items[0].isCompleted").value(true))
				.andExpect(jsonPath("$.data.items[0].lastScore").value(10.0));
		fetch("/listening/lessons?search=" + tag + "&status=NOT_COMPLETED", tokenA)
				.andExpect(jsonPath("$.data.total").value(0));
		// Người khác chưa làm bài này.
		fetch("/listening/lessons?search=" + tag, tokenB).andExpect(jsonPath("$.data.items[0].isCompleted").value(false));
	}

	@Test
	@DisplayName("Câu sai và câu bỏ trống tính sai, vẫn có một dòng cho mỗi câu; làm lại tạo lượt mới")
	void wrongAndBlankAnswers() throws Exception {
		submit("/listening/lessons/" + listening.getId() + "/submit", tokenA,
				body(10, choiceAnswer(listeningChoice, wrongOption(listeningChoice))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.score").value(0.0))
				.andExpect(jsonPath("$.data.answers", hasSize(2)))
				.andExpect(jsonPath("$.data.answers[0].userAnswer").value("Wrong"))
				.andExpect(jsonPath("$.data.answers[1].userAnswer").isEmpty());
		submit("/listening/lessons/" + listening.getId() + "/submit", tokenA,
				body(10, choiceAnswer(listeningChoice, correctOption(listeningChoice))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.score").value(5.0));
		fetch("/attempts?skill=LISTENING", tokenA).andExpect(jsonPath("$.data.total").value(2));
	}

	@Test
	@DisplayName("questionId của bài khác, câu trùng, lựa chọn của câu khác, thiếu body: 400, không phải 500")
	void malformedSubmissionsAre400() throws Exception {
		String url = "/listening/lessons/" + listening.getId() + "/submit";
		submit(url, tokenA, body(5, textAnswer(examReading, "dog")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.fieldErrorKeys.answers").value("errors.invalidAnswers"));
		submit(url, tokenA, body(5, textAnswer(listeningBlank, "a"), textAnswer(listeningBlank, "b")))
				.andExpect(status().isBadRequest());
		submit(url, tokenA, body(5, choiceAnswer(listeningChoice, correctOption(examListening))))
				.andExpect(status().isBadRequest());
		submit(url, tokenA, "{}").andExpect(status().isBadRequest());
		submit(url, tokenA, "{\"answers\":[],\"durationSeconds\":-1}").andExpect(status().isBadRequest());
		submit(url, tokenA, "").andExpect(status().isBadRequest());
		fetch("/attempts", tokenA).andExpect(jsonPath("$.data.items").isEmpty());
	}

	@Test
	@DisplayName("Bài đọc nộp quá giới hạn thời gian: vẫn nhận và chấm, timedOut = true")
	void lateReadingSubmission() throws Exception {
		Question q = questions.findByReadingLessonIdOrderBySortOrder(readingLesson.getId()).get(0);
		String url = "/reading/lessons/" + readingLesson.getId() + "/submit";
		submit(url, tokenA, body(200, choiceAnswer(q, correctOption(q))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.timedOut").value(true))
				.andExpect(jsonPath("$.data.score").value(10.0))
				.andExpect(jsonPath("$.data.transcript").doesNotExist());
		submit(url, tokenA, body(60, choiceAnswer(q, correctOption(q))))
				.andExpect(jsonPath("$.data.timedOut").value(false));
	}

	@Test
	@DisplayName("Kiểm tra: điểm tách theo kỹ năng")
	void examBreakdown() throws Exception {
		submit("/exams/" + exam.getId() + "/submit", tokenA,
				body(90, choiceAnswer(examListening, correctOption(examListening)), textAnswer(examReading, "cat")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.skill").value("EXAM"))
				.andExpect(jsonPath("$.data.score").value(5.0))
				.andExpect(jsonPath("$.data.breakdown", hasSize(2)))
				.andExpect(jsonPath("$.data.breakdown[0].skill").value("LISTENING"))
				.andExpect(jsonPath("$.data.breakdown[0].score").value(10.0))
				.andExpect(jsonPath("$.data.breakdown[1].skill").value("READING"))
				.andExpect(jsonPath("$.data.breakdown[1].correctCount").value(0))
				.andExpect(jsonPath("$.data.breakdown[1].totalQuestions").value(1));
		fetch("/exams?search=" + tag, tokenA)
				.andExpect(jsonPath("$.data.items[0].status").value("COMPLETED"))
				.andExpect(jsonPath("$.data.items[0].lastScore").value(5.0));
	}

	// --- lịch sử và quyền xem -------------------------------------------------------------------

	@Test
	@DisplayName("Xem lại lượt của mình; lượt của người khác là 404; lịch sử lọc theo kỹ năng và chỉ của mình")
	void attemptsAreOwnerOnly() throws Exception {
		String resultJson = submitListeningPerfect(tokenA);
		String attemptId = attemptId(resultJson);
		submit("/exams/" + exam.getId() + "/submit", tokenA, body(5)).andExpect(status().isOk());

		fetch("/attempts/" + attemptId, tokenA).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attemptId").value(attemptId))
				.andExpect(jsonPath("$.data.transcript").value(TRANSCRIPT))
				.andExpect(jsonPath("$.data.answers", hasSize(2)))
				.andExpect(jsonPath("$.data.answers[0].correctAnswer").value("Right"));
		fetch("/attempts/" + attemptId, tokenB).andExpect(status().isNotFound());
		fetch("/attempts/" + UUID.randomUUID(), tokenA).andExpect(status().isNotFound());

		fetch("/attempts", tokenA).andExpect(jsonPath("$.data.total").value(2))
				.andExpect(jsonPath("$.data.items[0].skill").value("EXAM"))
				.andExpect(jsonPath("$.data.items[0].detailPath").value(org.hamcrest.Matchers.startsWith("/exam/result/")))
				.andExpect(jsonPath("$.data.items[1].detailPath").value("/listening/result/" + attemptId))
				.andExpect(jsonPath("$.data.items[1].lessonTitle.vi").value("Nghe " + tag));
		fetch("/attempts?skill=LISTENING", tokenA).andExpect(jsonPath("$.data.total").value(1));
		fetch("/attempts?skill=READING", tokenA).andExpect(jsonPath("$.data.total").value(0));
		fetch("/attempts?skill=SPEAKING", tokenA).andExpect(jsonPath("$.data.total").value(0));
		fetch("/attempts?skill=BOGUS", tokenA).andExpect(status().isBadRequest());
		fetch("/attempts?pageSize=1&page=2", tokenA).andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.totalPages").value(2));
		fetch("/attempts", tokenB).andExpect(jsonPath("$.data.total").value(0));
	}

	@Test
	@DisplayName("study_sessions nhận skill và refId của bài nghe, bài đọc, đề kiểm tra")
	void heartbeatAcceptsPracticeContent() throws Exception {
		for (String[] pair : new String[][] { { "LISTENING", listening.getId().toString() },
				{ "READING", readingLesson.getId().toString() }, { "EXAM", exam.getId().toString() } }) {
			submit("/study/heartbeat", tokenA,
					"{\"skill\":\"%s\",\"refId\":\"%s\"}".formatted(pair[0], pair[1]))
					.andExpect(status().isOk());
		}
	}
}
