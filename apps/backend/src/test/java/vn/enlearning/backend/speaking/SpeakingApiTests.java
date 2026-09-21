package vn.enlearning.backend.speaking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
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
	@DisplayName("Chấm từng câu: mở lượt, dùng lại lượt, chấm/thu lại ghi đè, thiếu câu thì 400, đủ câu nộp ra GRADED")
	void perPromptFlow() throws Exception {
		String attemptId = idOf(startAttempt(tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
				.andExpect(jsonPath("$.data.results", hasSize(0))));
		assertThat(idOf(startAttempt(tokenA))).isEqualTo(attemptId);
		fetch("/speaking/lessons/" + lesson.getId(), tokenA).andExpect(jsonPath("$.data.isCompleted").value(false));

		assess(tokenA, attemptId, p1, file("a.webm", "audio/webm;codecs=opus"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.promptId").value(p1.toString()))
				.andExpect(jsonPath("$.data.transcript").value("Nice to meet you, my name is Alex."))
				.andExpect(jsonPath("$.data.score").value(7.5))
				.andExpect(jsonPath("$.data.wordIssues", hasSize(0)))
				.andExpect(jsonPath("$.data.tips", hasSize(1)));
		em.flush();
		em.clear();
		startAttempt(tokenA)
				.andExpect(jsonPath("$.data.attemptId").value(attemptId))
				.andExpect(jsonPath("$.data.results", hasSize(1)));

		submitAttempt(tokenA, attemptId, 30)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("SPEAKING_INCOMPLETE"))
				.andExpect(jsonPath("$.messageKey").value("errors.speakingIncomplete"));

		assess(tokenA, attemptId, p2, file("b.webm", "audio/webm"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.wordIssues[0].word").value("usually"))
				.andExpect(jsonPath("$.data.wordIssues[0].heardAs").value("usuall"))
				.andExpect(jsonPath("$.data.wordIssues[0].tip").isNotEmpty());
		assess(tokenA, attemptId, p1, file("c.webm", "audio/webm")).andExpect(status().isOk());
		em.flush();
		em.clear();
		assertThat(jdbc.queryForObject("select count(*) from speaking_prompt_results where attempt_id = ?",
				Integer.class, uuidBytes(UUID.fromString(attemptId)))).isEqualTo(2);

		submitAttempt(tokenA, attemptId, 42)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.overallScore").value(7.5))
				.andExpect(jsonPath("$.data.scores.pronunciation").value(7.5))
				.andExpect(jsonPath("$.data.improvements", hasSize(2)))
				.andExpect(jsonPath("$.data.promptFeedback", hasSize(2)))
				.andExpect(jsonPath("$.data.promptFeedback[1].mispronounced[0]").value("usually"));
		em.flush();
		em.clear();

		SpeakingAttempt saved = attempts.findById(UUID.fromString(attemptId)).orElseThrow();
		assertThat(saved.getStatus()).isEqualTo(SpeakingAttemptStatus.GRADED);
		assertThat(saved.getDurationSeconds()).isEqualTo(42);
		assertThat(saved.getModelName()).isEqualTo("fake-speaking-grader");
		fetch("/speaking/results/" + attemptId, tokenA)
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.promptFeedback[0].text").value("Nice to meet you, my name is Alex."));
		fetch("/speaking/lessons/" + lesson.getId(), tokenA)
				.andExpect(jsonPath("$.data.isCompleted").value(true))
				.andExpect(jsonPath("$.data.lastScore").isNumber());
		submitAttempt(tokenA, attemptId, 42).andExpect(status().isConflict());
		assess(tokenA, attemptId, p1, file("d.webm", "audio/webm")).andExpect(status().isConflict());
		// Sau khi nộp, mở lượt mới thay vì dùng lại lượt đã xong.
		assertThat(idOf(startAttempt(tokenA))).isNotEqualTo(attemptId);

		// Âm thanh không được lưu: không bảng nào của Luyện nói có cột âm thanh.
		List<String> columns = jdbc.queryForList(
				"select column_name from information_schema.columns where table_schema = database() "
						+ "and table_name in ('speaking_attempts', 'speaking_prompt_results')", String.class);
		assertThat(columns).noneMatch(c -> c.toLowerCase().contains("audio"));
	}

	@Test
	@DisplayName("Chấm câu khi AI lỗi (tên tệp chứa fail): 503, không lưu kết quả, thu lại được")
	void assessFailure() throws Exception {
		String attemptId = idOf(startAttempt(tokenA));
		assess(tokenA, attemptId, p1, file("fail.webm", "audio/webm"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
		assertThat(jdbc.queryForObject("select count(*) from speaking_prompt_results where attempt_id = ?",
				Integer.class, uuidBytes(UUID.fromString(attemptId)))).isZero();
		assess(tokenA, attemptId, p1, file("ok.webm", "audio/webm")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Chấm câu với đầu vào sai: câu lạ 400, thiếu/rỗng tệp 400, sai định dạng 415, quá lớn 413")
	void assessBadInput() throws Exception {
		String attemptId = idOf(startAttempt(tokenA));
		assess(tokenA, attemptId, UUID.randomUUID(), file("a.webm", "audio/webm"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.promptId").value("errors.field.invalidPrompt"));
		assess(tokenA, attemptId, p1)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.audio").value("errors.noRecording"));
		assess(tokenA, attemptId, p1, new MockMultipartFile("audio", "a.webm", "audio/webm", new byte[0]))
				.andExpect(status().isBadRequest());
		assess(tokenA, attemptId, p1, file("a.txt", "text/plain"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("AUDIO_UNSUPPORTED"));
		assess(tokenA, attemptId, p1, new MockMultipartFile("audio", "a.webm", "audio/webm",
				new byte[5 * 1024 * 1024 + 1]))
				.andExpect(status().isPayloadTooLarge())
				.andExpect(jsonPath("$.code").value("AUDIO_TOO_LARGE"));
	}

	@Test
	@DisplayName("Không token 401; lượt/kết quả của người khác 404; bài INACTIVE ẩn khỏi danh sách và không mở được lượt")
	void securityAndOwnership() throws Exception {
		fetch("/speaking/lessons", null).andExpect(status().isUnauthorized());
		fetch("/speaking/results/" + UUID.randomUUID(), null).andExpect(status().isUnauthorized());
		startAttempt(null).andExpect(status().isUnauthorized());
		String attemptId = idOf(startAttempt(tokenA));
		assess(null, attemptId, p1, file("a.webm", "audio/webm")).andExpect(status().isUnauthorized());
		submitAttempt(null, attemptId, 1).andExpect(status().isUnauthorized());

		assess(tokenB, attemptId, p1, file("a.webm", "audio/webm")).andExpect(status().isNotFound());
		submitAttempt(tokenB, attemptId, 1).andExpect(status().isNotFound());
		fetch("/speaking/results/" + attemptId, tokenB).andExpect(status().isNotFound());
		fetch("/speaking/results/" + UUID.randomUUID(), tokenA).andExpect(status().isNotFound());
		assess(tokenA, UUID.randomUUID().toString(), p1, file("a.webm", "audio/webm"))
				.andExpect(status().isNotFound());
		// Lượt của mỗi người là riêng: người B mở lượt mới, không dùng lượt của A.
		assertThat(idOf(startAttempt(tokenB))).isNotEqualTo(attemptId);

		lesson.setStatus(ContentStatus.INACTIVE);
		lessons.saveAndFlush(lesson);
		fetch("/speaking/lessons/" + lesson.getId(), tokenA).andExpect(status().isNotFound());
		startAttempt(tokenA).andExpect(status().isNotFound());
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

	private ResultActions startAttempt(String token) throws Exception {
		var request = post("/speaking/lessons/" + lesson.getId() + "/attempts");
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private ResultActions assess(String token, String attemptId, UUID promptId, MockMultipartFile... files)
			throws Exception {
		MockMultipartHttpServletRequestBuilder request = multipart(
				"/speaking/attempts/" + attemptId + "/prompts/" + promptId + "/assess");
		for (MockMultipartFile f : files) {
			request.file(f);
		}
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private ResultActions submitAttempt(String token, String attemptId, int seconds) throws Exception {
		var request = post("/speaking/attempts/" + attemptId + "/submit").contentType(MediaType.APPLICATION_JSON)
				.content("{\"durationSeconds\":" + seconds + "}");
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
