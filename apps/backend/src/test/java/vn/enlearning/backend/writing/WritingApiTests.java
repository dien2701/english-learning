package vn.enlearning.backend.writing;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.SubmissionStatus;
import vn.enlearning.backend.security.JwtService;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/**
 * Security → Controller → Service → JPA trên MySQL thật, với bản giả của AI. Bài chấm nền chạy đồng bộ trong
 * giao dịch test ({@code app.async.synchronous}), nên nộp xong là thấy kết quả; dữ liệu tự rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WritingApiTests {

	private static final String ESSAY = "I like to learn English every day because it helps me talk to many people.";

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private WritingPromptRepository prompts;
	@Autowired
	private WritingSubmissionRepository submissions;
	@Autowired
	private EntityManager em;
	@Autowired
	private JdbcTemplate jdbc;

	private String tag;
	private String tokenA;
	private String tokenB;
	private User userA;
	private WritingPrompt prompt;

	@BeforeEach
	void setUp() {
		tag = "W" + UUID.randomUUID().toString().replace("-", "");
		userA = newUser();
		tokenA = "Bearer " + jwt.issueAccessToken(userA);
		tokenB = "Bearer " + jwt.issueAccessToken(newUser());

		Topic topic = new Topic();
		topic.setSlug(tag);
		topic.setNameVi("Chủ đề " + tag);
		topic.setNameEn("Topic " + tag);
		topics.save(topic);

		prompt = new WritingPrompt();
		prompt.setTopic(topic);
		prompt.setTitleVi("Đề " + tag);
		prompt.setTitleEn("Prompt " + tag);
		prompt.setLevel(Level.BEGINNER);
		prompt.setStatus(ContentStatus.ACTIVE);
		prompt.setInstructions("Write about your daily study habits.");
		prompt.setMinWords(10);
		prompt.setHints(List.of("Say when you study", "Say why"));
		prompts.saveAndFlush(prompt);
	}

	@Test
	@DisplayName("Danh sách và chi tiết đề: có gợi ý, NOT_STARTED, chưa có điểm; lọc theo từ khoá")
	void listAndDetail() throws Exception {
		fetch("/writing/prompts?search=" + tag, tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].status").value("NOT_STARTED"))
				.andExpect(jsonPath("$.data.items[0].minWords").value(10))
				.andExpect(jsonPath("$.data.items[0].lastScore").doesNotExist())
				.andExpect(jsonPath("$.data.items[0].prompt").doesNotExist());
		fetch("/writing/prompts/" + prompt.getId(), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.prompt").value("Write about your daily study habits."))
				.andExpect(jsonPath("$.data.hints", hasSize(2)))
				.andExpect(jsonPath("$.data.hints[0]").value("Say when you study"));
	}

	@Test
	@DisplayName("Nộp bài: lưu rồi chấm, có điểm, danh sách lỗi đọc lại được từ DB, đề chuyển sang GRADED")
	void submitGrades() throws Exception {
		String id = idOf(submit(tokenA, ESSAY)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.wordCount").value(15)));
		em.flush();
		em.clear();

		fetch("/writing/submissions/" + id, tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.feedback.overallScore").isNumber())
				.andExpect(jsonPath("$.data.feedback.grammarScore").isNumber())
				.andExpect(jsonPath("$.data.feedback.summary").isNotEmpty())
				.andExpect(jsonPath("$.data.feedback.issues", hasSize(3)))
				.andExpect(jsonPath("$.data.feedback.issues[0].category").value("GRAMMAR"))
				.andExpect(jsonPath("$.data.feedback.issues[0].suggestion").isNotEmpty());
		fetch("/writing/prompts/" + prompt.getId(), tokenA)
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.lastScore").isNumber());
		fetch("/writing/submissions", tokenA)
				.andExpect(jsonPath("$.data.total").value(1))
				.andExpect(jsonPath("$.data.items[0].feedback.overallScore").isNumber());
	}

	@Test
	@DisplayName("Thiếu số từ tối thiểu, rỗng, quá dài: 400 kèm khoá lỗi; không có token: 401")
	void validation() throws Exception {
		submit(tokenA, "Too short.")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.content").value("errors.field.essayTooShort"));
		submit(tokenA, "   ")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.content").value("errors.field.essayRequired"));
		submit(tokenA, "word ".repeat(5000))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.content").value("errors.field.essayTooLong"));
		fetch("/writing/prompts", null).andExpect(status().isUnauthorized());
		submit(null, ESSAY).andExpect(status().isUnauthorized());
		fetch("/writing/submissions", null).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Đề không tồn tại hoặc INACTIVE: 404 ở chi tiết, nộp và danh sách")
	void missingOrInactivePrompt() throws Exception {
		fetch("/writing/prompts/" + UUID.randomUUID(), tokenA).andExpect(status().isNotFound());
		prompt.setStatus(ContentStatus.INACTIVE);
		prompts.saveAndFlush(prompt);
		fetch("/writing/prompts/" + prompt.getId(), tokenA).andExpect(status().isNotFound());
		submit(tokenA, ESSAY).andExpect(status().isNotFound());
		fetch("/writing/prompts?search=" + tag, tokenA).andExpect(jsonPath("$.data.total").value(0));
	}

	@Test
	@DisplayName("Bài của người khác: 404 ở xem và chấm lại; lịch sử chỉ có bài của mình")
	void otherUsersSubmissionIsNotFound() throws Exception {
		String id = idOf(submit(tokenA, ESSAY));
		fetch("/writing/submissions/" + id, tokenB).andExpect(status().isNotFound());
		mvc.perform(post("/writing/submissions/" + id + "/regrade").header("Authorization", tokenB))
				.andExpect(status().isNotFound());
		fetch("/writing/submissions", tokenB).andExpect(jsonPath("$.data.total").value(0));
		fetch("/writing/prompts/" + prompt.getId(), tokenB).andExpect(jsonPath("$.data.status").value("NOT_STARTED"));
	}

	@Test
	@DisplayName("AI lỗi: bài vẫn được giữ ở NEEDS_RETRY; chấm lại thành GRADED; chấm lại bài không lỗi: 409")
	void failureThenRegrade() throws Exception {
		String content = ESSAY + " [fail] " + tag;
		String id = idOf(submit(tokenA, content).andExpect(status().isOk()));
		// Phản hồi nộp bài có thể còn ghi GRADING (chấm nền); trạng thái thật đọc lại từ DB.
		em.clear();
		fetch("/writing/submissions/" + id, tokenA)
				.andExpect(jsonPath("$.data.status").value("NEEDS_RETRY"))
				.andExpect(jsonPath("$.data.feedback").doesNotExist())
				.andExpect(jsonPath("$.data.content").isNotEmpty());
		fetch("/writing/prompts/" + prompt.getId(), tokenA).andExpect(jsonPath("$.data.status").value("NEEDS_RETRY"));

		mvc.perform(post("/writing/submissions/" + id + "/regrade").header("Authorization", tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("GRADED"))
				.andExpect(jsonPath("$.data.feedback.overallScore").isNumber());

		mvc.perform(post("/writing/submissions/" + id + "/regrade").header("Authorization", tokenA))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("Bài luôn lỗi: chấm lại vẫn NEEDS_RETRY, không mất bài")
	void alwaysFailing() throws Exception {
		String id = idOf(submit(tokenA, ESSAY + " [fail-always]"));
		em.clear();
		fetch("/writing/submissions/" + id, tokenA).andExpect(jsonPath("$.data.status").value("NEEDS_RETRY"));
		mvc.perform(post("/writing/submissions/" + id + "/regrade").header("Authorization", tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("NEEDS_RETRY"));
		fetch("/writing/submissions/" + id, tokenA).andExpect(jsonPath("$.data.content").isNotEmpty());
	}

	@Test
	@DisplayName("Bài GRADING quá hạn (AI treo hoặc server khởi động lại): hỏi lại thì chuyển NEEDS_RETRY")
	void staleGradingBecomesRetry() throws Exception {
		WritingSubmission stuck = new WritingSubmission();
		stuck.setUser(userA);
		stuck.setPrompt(prompt);
		stuck.setContent(ESSAY);
		stuck.setWordCount(15);
		stuck.setStatus(SubmissionStatus.GRADING);
		stuck.setSubmittedAt(Instant.now());
		submissions.saveAndFlush(stuck);

		fetch("/writing/submissions/" + stuck.getId(), tokenA).andExpect(jsonPath("$.data.status").value("GRADING"));

		jdbc.update("update writing_submissions set updated_at = ? where id = ?",
				java.sql.Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)),
				uuidBytes(stuck.getId()));
		em.clear();
		fetch("/writing/submissions/" + stuck.getId(), tokenA)
				.andExpect(jsonPath("$.data.status").value("NEEDS_RETRY"));
	}

	private ResultActions submit(String token, String content) throws Exception {
		var request = post("/writing/prompts/" + prompt.getId() + "/submit")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"" + content.replace("\"", "\\\"") + "\"}");
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
		return body.replaceAll(".*\"data\":\\{\"id\":\"([^\"]+)\".*", "$1");
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
