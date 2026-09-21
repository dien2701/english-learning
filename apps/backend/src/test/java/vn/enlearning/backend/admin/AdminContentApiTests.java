package vn.enlearning.backend.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.security.JwtService;

/**
 * Chạy Security → Controller → Service → JPA trên MySQL thật cho khu quản trị nội dung và chủ đề. Dữ liệu tự dựng
 * trong giao dịch test (tự rollback) với nhãn ngẫu nhiên, nên không phụ thuộc dữ liệu seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminContentApiTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;

	private String tag;
	private String admin;
	private String learner;
	private String topicId;

	@BeforeEach
	void setUp() throws Exception {
		tag = "T" + UUID.randomUUID().toString().replace("-", "");
		admin = "Bearer " + jwt.issueAccessToken(newUser(Role.ADMIN));
		learner = "Bearer " + jwt.issueAccessToken(newUser(Role.USER));
		topicId = id(send(post("/admin/topics"), admin, "{\"nameVi\":\"Chủ đề " + tag + "\",\"nameEn\":\"Topic " + tag + "\"}")
				.andExpect(status().isCreated()).andReturn());
	}

	// --- 401 / 403 --------------------------------------------------------------------------------

	@Test
	@DisplayName("Mọi /admin/** trả 401 khi chưa đăng nhập và 403 với tài khoản USER")
	void adminEndpointsRequireAdminRole() throws Exception {
		UUID any = UUID.randomUUID();
		String topic = "{\"nameVi\":\"x\"}";
		String status = "{\"status\":\"INACTIVE\"}";
		Object[][] calls = {
				{ get("/admin/content"), null }, { get("/admin/content/" + any), null },
				{ post("/admin/content"), "{}" }, { put("/admin/content/" + any), "{}" },
				{ patch("/admin/content/" + any), status }, { delete("/admin/content/" + any), null },
				{ get("/admin/topics"), null }, { post("/admin/topics"), topic },
				{ put("/admin/topics/" + any), topic }, { delete("/admin/topics/" + any), null } };
		for (Object[] call : calls) {
			MockHttpServletRequestBuilder builder = (MockHttpServletRequestBuilder) call[0];
			send(builder, null, (String) call[1]).andExpect(status().isUnauthorized());
		}
		for (Object[] call : calls) {
			MockHttpServletRequestBuilder builder = (MockHttpServletRequestBuilder) call[0];
			send(builder, learner, (String) call[1]).andExpect(status().isForbidden());
		}
	}

	// --- tạo, đọc lại 6 loại ----------------------------------------------------------------------

	@Test
	@DisplayName("Tạo được cả 6 loại nội dung; GET chi tiết trả lại payload có id phần tử con")
	void createEachTypeAndReadBack() throws Exception {
		String[] bodies = { vocabulary(), listening(), reading(), writing(), speaking(), exam() };
		String[] skills = { "VOCABULARY", "LISTENING", "READING", "WRITING", "SPEAKING", "EXAM" };
		int[] counts = { 2, 2, 1, 1, 2, 2 };
		for (int i = 0; i < bodies.length; i++) {
			String id = id(send(post("/admin/content"), admin, bodies[i])
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.data.skill", is(skills[i])))
					.andExpect(jsonPath("$.data.itemCount", is(counts[i])))
					.andExpect(jsonPath("$.data.inUse", is(false)))
					.andExpect(jsonPath("$.data.status", is("ACTIVE"))).andReturn());
			send(get("/admin/content/" + id), admin, null)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.payload.skill", is(skills[i])))
					.andExpect(jsonPath("$.data.title.vi", is("Tiêu đề " + tag + " " + skills[i].charAt(0))));
		}
		send(get("/admin/content?search=" + tag), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total", is(6)));
		send(get("/admin/content?search=" + tag + "&skill=EXAM"), admin, null)
				.andExpect(jsonPath("$.data.total", is(1)))
				.andExpect(jsonPath("$.data.items[0].topicName").doesNotExist());
	}

	@Test
	@DisplayName("Thiếu trường bắt buộc, chủ đề lạ, hai đáp án đúng: 400 kèm fieldErrorKeys")
	void invalidContentIsRejected() throws Exception {
		send(post("/admin/content"), admin, "{\"skill\":\"READING\",\"level\":\"BEGINNER\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.titleVi").exists())
				.andExpect(jsonPath("$.fieldErrorKeys.topicId").exists());

		send(post("/admin/content"), admin, reading().replace(topicId, UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.topicId", is("errors.field.topicNotFound")));

		String twoCorrect = reading().replace("{\"content\":\"No\",\"correct\":false}",
				"{\"content\":\"No\",\"correct\":true}");
		send(post("/admin/content"), admin, twoCorrect)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys['questions[0].options']", is("errors.field.oneCorrectOption")));

		send(post("/admin/content"), admin, "{\"skill\":\"UNKNOWN\"}").andExpect(status().isBadRequest());
	}

	// --- INACTIVE -----------------------------------------------------------------------------------

	@Test
	@DisplayName("Chuyển INACTIVE thì người học không còn thấy; ACTIVE lại thì thấy")
	void inactiveContentIsHiddenFromLearners() throws Exception {
		String id = createReading();
		send(get("/reading/lessons?search=" + tag), learner, null).andExpect(jsonPath("$.data.items", hasSize(1)));

		send(patch("/admin/content/" + id), admin, "{\"status\":\"INACTIVE\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status", is("INACTIVE")));
		send(get("/reading/lessons?search=" + tag), learner, null).andExpect(jsonPath("$.data.items", hasSize(0)));
		send(get("/admin/content?search=" + tag + "&status=INACTIVE"), admin, null)
				.andExpect(jsonPath("$.data.total", is(1)));

		send(patch("/admin/content/" + id), admin, "{\"status\":\"ACTIVE\"}").andExpect(status().isOk());
		send(get("/reading/lessons?search=" + tag), learner, null).andExpect(jsonPath("$.data.items", hasSize(1)));
	}

	// --- sửa, xoá, khoá ngoại -----------------------------------------------------------------------

	@Test
	@DisplayName("PUT giữ id câu hỏi cũ, thêm câu mới, bớt câu vắng mặt khi chưa có lịch sử")
	void updateSyncsQuestionsById() throws Exception {
		String id = createReading();
		MvcResult detail = send(get("/admin/content/" + id), admin, null).andReturn();
		String questionId = JsonPath.read(json(detail), "$.data.payload.questions[0].id");

		String updated = reading().replace("\"questions\":[", "\"questions\":[{\"id\":\"" + questionId
				+ "\",\"kind\":\"FILL_BLANK\",\"content\":\"Fill ___\",\"acceptedAnswers\":[\"apple\"]},");
		send(put("/admin/content/" + id), admin, updated)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.itemCount", is(2)))
				.andExpect(jsonPath("$.data.payload.questions[0].id", is(questionId)))
				.andExpect(jsonPath("$.data.payload.questions[0].kind", is("FILL_BLANK")))
				.andExpect(jsonPath("$.data.payload.questions[0].options", hasSize(0)));

		String onlyNew = reading();
		send(put("/admin/content/" + id), admin, onlyNew)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.itemCount", is(1)));

		send(put("/admin/content/" + id), admin, listening())
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.skill", is("errors.field.skillMismatch")));
	}

	@Test
	@DisplayName("Nội dung đã có lượt làm: DELETE và bớt câu hỏi trả 409 (không phải 500), PATCH INACTIVE vẫn được")
	void contentInHistoryCannotBeDeleted() throws Exception {
		String id = createReading();
		MvcResult detail = send(get("/admin/content/" + id), admin, null).andReturn();
		String questionId = JsonPath.read(json(detail), "$.data.payload.questions[0].id");
		send(post("/reading/lessons/" + id + "/submit"), learner,
				"{\"answers\":[{\"questionId\":\"" + questionId + "\"}],\"durationSeconds\":5}")
				.andExpect(status().isOk());

		send(get("/admin/content/" + id), admin, null).andExpect(jsonPath("$.data.inUse", is(true)));
		send(delete("/admin/content/" + id), admin, null)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("CONTENT_IN_USE")));
		String keepOldAddNew = "{\"id\":\"" + questionId + "\",\"kind\":\"FILL_BLANK\",\"content\":\"Keep\","
				+ "\"acceptedAnswers\":[\"y\"]}," + choice();
		send(put("/admin/content/" + id), admin, readingWith(keepOldAddNew))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.itemCount", is(2)));
		send(put("/admin/content/" + id), admin, reading())
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("CONTENT_IN_USE")));
		send(patch("/admin/content/" + id), admin, "{\"status\":\"INACTIVE\"}").andExpect(status().isOk());
	}

	@Test
	@DisplayName("Nội dung chưa dùng xoá mềm được; sau đó GET là 404")
	void unusedContentCanBeDeleted() throws Exception {
		String id = createReading();
		send(delete("/admin/content/" + id), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.deleted", is(true)));
		send(get("/admin/content/" + id), admin, null).andExpect(status().isNotFound());
	}

	// --- chủ đề -------------------------------------------------------------------------------------

	@Test
	@DisplayName("Chủ đề: slug duy nhất, còn nội dung thì xoá 409, hết nội dung thì xoá được")
	void topicLifecycle() throws Exception {
		send(post("/admin/topics"), admin, "{\"nameVi\":\"Chủ đề " + tag + "\",\"nameEn\":\"Topic " + tag + "\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.slug", is("topic-" + tag.toLowerCase() + "-2")));

		send(put("/admin/topics/" + topicId), admin, "{\"nameVi\":\"Đã đổi " + tag + "\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name.vi", is("Đã đổi " + tag)))
				.andExpect(jsonPath("$.data.name.en", is("Đã đổi " + tag)));
		send(post("/admin/topics"), admin, "{\"nameVi\":\"\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.nameVi").exists());

		String contentId = createReading();
		send(get("/admin/topics"), admin, null).andExpect(status().isOk());
		send(delete("/admin/topics/" + topicId), admin, null)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("CONTENT_IN_USE")));

		send(delete("/admin/content/" + contentId), admin, null).andExpect(status().isOk());
		send(delete("/admin/topics/" + topicId), admin, null).andExpect(status().isOk());
		send(delete("/admin/topics/" + topicId), admin, null).andExpect(status().isNotFound());
	}

	// --- dữ liệu mẫu --------------------------------------------------------------------------------

	private String title(String skill) {
		return "\"titleVi\":\"Tiêu đề " + tag + " " + skill.charAt(0) + "\",\"titleEn\":\"Title " + tag + "\",\"level\":\"BEGINNER\"";
	}

	private String vocabulary() {
		return "{\"skill\":\"VOCABULARY\"," + title("VOCABULARY") + ",\"topicId\":\"" + topicId + "\",\"cards\":["
				+ "{\"word\":\"apple\",\"meaningVi\":\"quả táo\"},{\"word\":\"pear\",\"meaningVi\":\"quả lê\"}]}";
	}

	private String choice() {
		return "{\"kind\":\"SINGLE_CHOICE\",\"content\":\"Q?\",\"explanation\":\"Because\",\"options\":["
				+ "{\"content\":\"Yes\",\"correct\":true},{\"content\":\"No\",\"correct\":false}]}";
	}

	private String blank() {
		return "{\"kind\":\"FILL_BLANK\",\"content\":\"Fill ___\",\"acceptedAnswers\":[\"apple\"]}";
	}

	private String listening() {
		return "{\"skill\":\"LISTENING\"," + title("LISTENING") + ",\"topicId\":\"" + topicId
				+ "\",\"durationSeconds\":30,\"transcript\":\"Hello there\",\"questions\":[" + choice() + "," + blank() + "]}";
	}

	private String reading() {
		return readingWith(choice());
	}

	private String readingWith(String questions) {
		return "{\"skill\":\"READING\"," + title("READING") + ",\"topicId\":\"" + topicId
				+ "\",\"timeLimitMinutes\":5,\"paragraphs\":[\"One two three.\",\"Four five.\"],\"questions\":[" + questions + "]}";
	}

	private String writing() {
		return "{\"skill\":\"WRITING\"," + title("WRITING") + ",\"topicId\":\"" + topicId
				+ "\",\"instructions\":\"Write about your day.\",\"suggestedMinutes\":20,\"minWords\":50,\"hints\":[\"Use past tense\"]}";
	}

	private String speaking() {
		return "{\"skill\":\"SPEAKING\"," + title("SPEAKING") + ",\"topicId\":\"" + topicId + "\",\"prompts\":["
				+ "{\"text\":\"Good morning\",\"meaningVi\":\"Chào buổi sáng\"},{\"text\":\"Thank you\"}]}";
	}

	private String exam() {
		return "{\"skill\":\"EXAM\"," + title("EXAM") + ",\"timeLimitMinutes\":10,\"questions\":["
				+ "{\"skill\":\"LISTENING\"," + choice().substring(1) + ",{\"skill\":\"READING\"," + blank().substring(1) + "]}";
	}

	private String createReading() throws Exception {
		return id(send(post("/admin/content"), admin, reading()).andExpect(status().isCreated()).andReturn());
	}

	private User newUser(Role role) {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		user.setRole(role);
		return users.saveAndFlush(user);
	}

	private ResultActions send(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
		if (token != null) {
			request.header("Authorization", token);
		}
		if (body != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(body);
		}
		return mvc.perform(request);
	}

	private static String json(MvcResult result) throws Exception {
		return result.getResponse().getContentAsString();
	}

	private static String id(MvcResult result) throws Exception {
		return JsonPath.read(json(result), "$.data.id");
	}
}
