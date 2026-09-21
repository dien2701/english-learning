package vn.enlearning.backend.speaking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.security.JwtService;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;

/**
 * Security → Controller → Service → JPA trên MySQL thật, với bản giả của AI. Bài chấm nền chạy đồng bộ trong
 * giao dịch test; dữ liệu tự rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SpeakingApiTests {

	private static final byte[] AUDIO = { 1, 2, 3, 4 };

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private SpeakingLessonRepository lessons;
	@Autowired
	private SpeakingAttemptRepository attempts;
	@Autowired
	private EntityManager em;
	@Autowired
	private JdbcTemplate jdbc;

	private String tag;
	private User userA;
	private String tokenA;
	private String tokenB;
	private SpeakingLesson lesson;
	private UUID p1;
	private UUID p2;

	@BeforeEach
	void setUp() {
		tag = "S" + UUID.randomUUID().toString().replace("-", "");
		userA = newUser();
		tokenA = "Bearer " + jwt.issueAccessToken(userA);
		tokenB = "Bearer " + jwt.issueAccessToken(newUser());

		Topic topic = new Topic();
		topic.setSlug(tag);
		topic.setNameVi("Chủ đề " + tag);
		topic.setNameEn("Topic " + tag);
		topics.save(topic);

		p1 = UUID.randomUUID();
		p2 = UUID.randomUUID();
		lesson = new SpeakingLesson();
		lesson.setTopic(topic);
		lesson.setTitleVi("Nói " + tag);
		lesson.setTitleEn("Speak " + tag);
		lesson.setLevel(Level.BEGINNER);
		lesson.setStatus(ContentStatus.ACTIVE);
		lesson.setPrompts(List.of(
				new SpeakingPrompt(p1, "Nice to meet you, my name is Alex.", "/naɪs/", "Rất vui được gặp bạn"),
				new SpeakingPrompt(p2, "I usually study English in the evening.", null, "Tôi thường học tiếng Anh")));
		lessons.saveAndFlush(lesson);
	}

	@Test
	@DisplayName("Danh sách và chi tiết: câu theo thứ tự từ 1, chưa hoàn thành; lọc theo từ khoá")
	void listAndDetail() throws Exception {
		fetch("/speaking/lessons?search=" + tag, tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].promptCount").value(2))
				.andExpect(jsonPath("$.data.items[0].isCompleted").value(false))
				.andExpect(jsonPath("$.data.items[0].lastScore").doesNotExist())
				.andExpect(jsonPath("$.data.items[0].prompts").doesNotExist());
		fetch("/speaking/lessons/" + lesson.getId(), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.prompts", hasSize(2)))
				.andExpect(jsonPath("$.data.prompts[0].order").value(1))
				.andExpect(jsonPath("$.data.prompts[0].text").value("Nice to meet you, my name is Alex."))
				.andExpect(jsonPath("$.data.prompts[0].meaning").value("Rất vui được gặp bạn"))
				.andExpect(jsonPath("$.data.prompts[1].order").value(2));
	}

	@Test
	@DisplayName("Nộp hai bản ghi: chấm xong có điểm, transcript từng câu; bài chuyển hoàn thành; DB không có cột âm thanh")
	void submitGrades() throws Exception {
		String attemptId = idOf(submit(tokenA, List.of(p1, p2), List.of(file("a.webm", "audio/webm;codecs=opus"),
				file("b.webm", "audio/webm")), 42)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED")));
		em.flush();
		em.clear();

		fetch("/speaking/results/" + attemptId, tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.overallScore").isNumber())
				.andExpect(jsonPath("$.data.scores.pronunciation").isNumber())
				.andExpect(jsonPath("$.data.scores.relevance").isNumber())
				.andExpect(jsonPath("$.data.improvements", hasSize(2)))
				.andExpect(jsonPath("$.data.promptFeedback", hasSize(2)))
				.andExpect(jsonPath("$.data.promptFeedback[0].promptId").value(p1.toString()))
				.andExpect(jsonPath("$.data.promptFeedback[0].text").value("Nice to meet you, my name is Alex."))
				.andExpect(jsonPath("$.data.promptFeedback[0].transcript").isNotEmpty())
				.andExpect(jsonPath("$.data.promptFeedback[1].mispronounced[0]").value("usually"));
		fetch("/speaking/lessons?search=" + tag, tokenA)
				.andExpect(jsonPath("$.data.items[0].isCompleted").value(true))
				.andExpect(jsonPath("$.data.items[0].lastScore").isNumber());

		SpeakingAttempt saved = attempts.findById(UUID.fromString(attemptId)).orElseThrow();
		assertThat(saved.getDurationSeconds()).isEqualTo(42);
		assertThat(saved.getModelName()).isEqualTo("fake-speaking-grader");
		List<String> columns = jdbc.queryForList(
				"select column_name from information_schema.columns where table_schema = database() "
						+ "and table_name = 'speaking_attempts'", String.class);
		assertThat(columns).noneMatch(c -> c.toLowerCase().contains("audio"));
	}

	@Test
	@DisplayName("AI lỗi (tên tệp chứa fail): lượt FAILED, không điểm, bài chưa hoàn thành; ghi âm lại được")
	void failureThenRetake() throws Exception {
		String attemptId = idOf(submit(tokenA, List.of(p1), List.of(file("fail.webm", "audio/webm")), 5)
				.andExpect(status().isOk()));
		em.flush();
		em.clear();
		fetch("/speaking/results/" + attemptId, tokenA)
				.andExpect(jsonPath("$.data.status").value("FAILED"))
				.andExpect(jsonPath("$.data.overallScore").doesNotExist())
				.andExpect(jsonPath("$.data.scores").doesNotExist())
				.andExpect(jsonPath("$.data.promptFeedback", hasSize(0)));
		fetch("/speaking/lessons/" + lesson.getId(), tokenA).andExpect(jsonPath("$.data.isCompleted").value(false));

		submit(tokenA, List.of(p1), List.of(file("ok.webm", "audio/webm")), 5)
				.andExpect(jsonPath("$.data.status").value("GRADED"));
		fetch("/speaking/lessons/" + lesson.getId(), tokenA).andExpect(jsonPath("$.data.isCompleted").value(true));
	}

	@Test
	@DisplayName("Đầu vào sai: không có bản ghi, lệch số lượng, câu lạ hoặc trùng, tệp rỗng: 400")
	void badInput() throws Exception {
		submit(tokenA, List.of(), List.of(), 0)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.audio").value("errors.noRecording"));
		submit(tokenA, List.of(p1, p2), List.of(file("a.webm", "audio/webm")), 1)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.audio").value("errors.field.audioMismatch"));
		submit(tokenA, List.of(UUID.randomUUID()), List.of(file("a.webm", "audio/webm")), 1)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.promptIds").value("errors.field.invalidPrompt"));
		submit(tokenA, List.of(p1, p1), List.of(file("a.webm", "audio/webm"), file("b.webm", "audio/webm")), 1)
				.andExpect(status().isBadRequest());
		submit(tokenA, List.of(p1), List.of(new MockMultipartFile("audio", "a.webm", "audio/webm", new byte[0])), 1)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.audio").value("errors.noRecording"));
	}

	@Test
	@DisplayName("Tệp sai định dạng: 415; quá lớn: 413 (không phải 500); không lưu lượt nào")
	void unsupportedAndTooLarge() throws Exception {
		submit(tokenA, List.of(p1), List.of(file("a.txt", "text/plain")), 1)
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("AUDIO_UNSUPPORTED"));
		submit(tokenA, List.of(p1), List.of(new MockMultipartFile("audio", "a.webm", "audio/webm",
				new byte[5 * 1024 * 1024 + 1])), 1)
				.andExpect(status().isPayloadTooLarge())
				.andExpect(jsonPath("$.code").value("AUDIO_TOO_LARGE"));
		assertThat(attempts.findAll().stream().filter(a -> a.getUser().getId().equals(userA.getId()))).isEmpty();
	}

	@Test
	@DisplayName("Không có token: 401; bài INACTIVE hoặc không có: 404; kết quả của người khác: 404")
	void securityAndOwnership() throws Exception {
		submit(null, List.of(p1), List.of(file("a.webm", "audio/webm")), 1).andExpect(status().isUnauthorized());
		fetch("/speaking/lessons", null).andExpect(status().isUnauthorized());
		fetch("/speaking/results/" + UUID.randomUUID(), null).andExpect(status().isUnauthorized());

		String attemptId = idOf(submit(tokenA, List.of(p1), List.of(file("a.webm", "audio/webm")), 1));
		fetch("/speaking/results/" + attemptId, tokenB).andExpect(status().isNotFound());
		fetch("/speaking/results/" + UUID.randomUUID(), tokenA).andExpect(status().isNotFound());

		lesson.setStatus(ContentStatus.INACTIVE);
		lessons.saveAndFlush(lesson);
		fetch("/speaking/lessons/" + lesson.getId(), tokenA).andExpect(status().isNotFound());
		submit(tokenA, List.of(p1), List.of(file("a.webm", "audio/webm")), 1).andExpect(status().isNotFound());
		fetch("/speaking/lessons?search=" + tag, tokenA).andExpect(jsonPath("$.data.total").value(0));
	}

	@Test
	@DisplayName("Lượt GRADING quá hạn (AI treo hoặc server khởi động lại): hỏi lại thì chuyển FAILED")
	void staleGradingBecomesFailed() throws Exception {
		SpeakingAttempt stuck = new SpeakingAttempt();
		stuck.setUser(userA);
		stuck.setLesson(lesson);
		stuck.setStatus(SpeakingAttemptStatus.GRADING);
		stuck.setStartedAt(Instant.now());
		stuck.setSubmittedAt(Instant.now());
		attempts.saveAndFlush(stuck);

		fetch("/speaking/results/" + stuck.getId(), tokenA).andExpect(jsonPath("$.data.status").value("GRADING"));

		jdbc.update("update speaking_attempts set updated_at = ? where id = ?",
				java.sql.Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)), uuidBytes(stuck.getId()));
		em.clear();
		fetch("/speaking/results/" + stuck.getId(), tokenA).andExpect(jsonPath("$.data.status").value("FAILED"));
	}

	private static MockMultipartFile file(String name, String contentType) {
		return new MockMultipartFile("audio", name, contentType, AUDIO);
	}

	private ResultActions submit(String token, List<UUID> promptIds, List<MockMultipartFile> files, int seconds)
			throws Exception {
		MockMultipartHttpServletRequestBuilder request = multipart("/speaking/lessons/" + lesson.getId() + "/submit");
		promptIds.forEach(id -> request.param("promptIds", id.toString()));
		files.forEach(request::file);
		request.param("durationSeconds", String.valueOf(seconds));
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private ResultActions fetch(String url, String token) throws Exception {
		var request = get(url);
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private static String idOf(ResultActions result) throws Exception {
		String body = result.andReturn().getResponse().getContentAsString();
		return body.replaceAll(".*\"data\":\\{\"attemptId\":\"([^\"]+)\".*", "$1");
	}

	private static byte[] uuidBytes(UUID id) {
		java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(16);
		buffer.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());
		return buffer.array();
	}

	private User newUser() {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		return users.saveAndFlush(user);
	}
}
