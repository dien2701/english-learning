package vn.enlearning.backend.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.entity.User;

/** Hồ sơ, cài đặt, đổi mật khẩu và heartbeat qua toàn bộ chồng Security → Controller → Service → JPA. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileApiTests {

	private static final String PASSWORD = "matkhau-dung-1";
	private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
	private static final Pattern REFRESH_COOKIE = Pattern.compile("refresh_token=([^;]*)");

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private User user;
	private MvcResult loginResult;
	private String bearer;

	@BeforeEach
	void setUp() throws Exception {
		user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setFullName("Người thử");
		users.saveAndFlush(user);
		loginResult = login(user.getEmail(), PASSWORD);
		Matcher m = TOKEN.matcher(loginResult.getResponse().getContentAsString());
		assertThat(m.find()).isTrue();
		bearer = "Bearer " + m.group(1);
	}

	private MvcResult login(String email, String password) throws Exception {
		return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
				.andExpect(status().isOk()).andReturn();
	}

	private static Cookie refreshCookie(MvcResult result) {
		String header = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		assertThat(header).isNotNull();
		Matcher m = REFRESH_COOKIE.matcher(header);
		assertThat(m.find()).isTrue();
		return new Cookie("refresh_token", m.group(1));
	}

	private ResultActions send(MockHttpServletRequestBuilder request, String json) throws Exception {
		return mvc.perform(request.header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	// --- Hồ sơ ------------------------------------------------------------------------------------

	@Test
	@DisplayName("GET /profile có số thống kê, không có passwordHash")
	void readsProfile() throws Exception {
		mvc.perform(get("/profile").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(user.getEmail()))
				.andExpect(jsonPath("$.data.totalMinutes").value(0))
				.andExpect(jsonPath("$.data.completedLessons").value(0))
				.andExpect(jsonPath("$.data.masteredWords").value(0))
				.andExpect(jsonPath("$.data.joinedAt").exists())
				.andExpect(jsonPath("$.data.passwordHash").doesNotExist());
	}

	@Test
	@DisplayName("PATCH /profile đổi tên, xoá số điện thoại; email trùng hoặc sai định dạng thì 4xx")
	void updatesProfile() throws Exception {
		send(patch("/profile"), "{\"fullName\":\"  Tên Mới \",\"phoneNumber\":\"0901234567\"}")
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.fullName").value("Tên Mới"))
				.andExpect(jsonPath("$.data.phoneNumber").value("0901234567"));
		mvc.perform(get("/profile").header("Authorization", bearer))
				.andExpect(jsonPath("$.data.fullName").value("Tên Mới"));
		send(patch("/profile"), "{\"phoneNumber\":\"\"}").andExpect(jsonPath("$.data.phoneNumber").doesNotExist());

		send(patch("/profile"), "{\"fullName\":\"   \"}").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.fullName").value("auth.validation.nameRequired"));
		send(patch("/profile"), "{\"email\":\"khong-hop-le\"}").andExpect(status().isBadRequest());

		User other = new User();
		other.setEmail("it-" + UUID.randomUUID() + "@test.local");
		other.setPasswordHash(passwordEncoder.encode(PASSWORD));
		other.setFullName("Người khác");
		users.saveAndFlush(other);
		send(patch("/profile"), "{\"email\":\"%s\"}".formatted(other.getEmail().toUpperCase()))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_TAKEN"));

		String fresh = "it-" + UUID.randomUUID() + "@test.local";
		send(patch("/profile"), "{\"email\":\"%s\"}".formatted(fresh)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(fresh));
	}

	// --- Đổi mật khẩu ---------------------------------------------------------------------------

	@Test
	@DisplayName("Đổi mật khẩu thu hồi phiên cũ, cấp cookie mới cho thiết bị này, mật khẩu mới đăng nhập được")
	void changesPasswordAndRevokesOtherSessions() throws Exception {
		Cookie elsewhere = refreshCookie(login(user.getEmail(), PASSWORD));

		MvcResult changed = send(post("/profile/password"),
				"{\"currentPassword\":\"%s\",\"newPassword\":\"moi-123456\"}".formatted(PASSWORD))
				.andExpect(status().isOk()).andReturn();

		mvc.perform(post("/auth/refresh").cookie(elsewhere)).andExpect(status().isUnauthorized());
		mvc.perform(post("/auth/refresh").cookie(refreshCookie(loginResult))).andExpect(status().isUnauthorized());
		mvc.perform(post("/auth/refresh").cookie(refreshCookie(changed))).andExpect(status().isOk());
		login(user.getEmail(), "moi-123456");
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(user.getEmail(), PASSWORD)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Mật khẩu hiện tại sai hoặc mật khẩu mới ngắn thì 400")
	void rejectsBadPasswordChange() throws Exception {
		send(post("/profile/password"), "{\"currentPassword\":\"sai-sai-sai\",\"newPassword\":\"moi-123456\"}")
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("WRONG_PASSWORD"))
				.andExpect(jsonPath("$.fieldErrorKeys.currentPassword").value("errors.wrongPassword"));
		send(post("/profile/password"), "{\"currentPassword\":\"%s\",\"newPassword\":\"12345\"}".formatted(PASSWORD))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.fieldErrorKeys.newPassword").value("auth.validation.passwordMin"));
		login(user.getEmail(), PASSWORD);
	}

	// --- Cài đặt --------------------------------------------------------------------------------

	@Test
	@DisplayName("GET/PATCH /settings giữ giá trị; giá trị sai thì 400")
	void savesSettings() throws Exception {
		mvc.perform(get("/settings").header("Authorization", bearer)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.language").value("vi"))
				.andExpect(jsonPath("$.data.theme").value("light"))
				.andExpect(jsonPath("$.data.reminderTime").value("20:00"))
				.andExpect(jsonPath("$.data.timeZone").value("Asia/Ho_Chi_Minh"));

		send(patch("/settings"), "{\"language\":\"en\",\"theme\":\"dark\",\"emailReminders\":false,"
				+ "\"reminderTime\":\"07:30\",\"dailyGoalMinutes\":45,\"timeZone\":\"Asia/Tokyo\"}")
				.andExpect(status().isOk());
		mvc.perform(get("/settings").header("Authorization", bearer))
				.andExpect(jsonPath("$.data.language").value("en"))
				.andExpect(jsonPath("$.data.theme").value("dark"))
				.andExpect(jsonPath("$.data.emailReminders").value(false))
				.andExpect(jsonPath("$.data.reminderTime").value("07:30"))
				.andExpect(jsonPath("$.data.dailyGoalMinutes").value(45))
				.andExpect(jsonPath("$.data.timeZone").value("Asia/Tokyo"));

		send(patch("/settings"), "{\"language\":\"fr\"}").andExpect(status().isBadRequest());
		send(patch("/settings"), "{\"reminderTime\":\"25:99\"}").andExpect(status().isBadRequest());
		send(patch("/settings"), "{\"dailyGoalMinutes\":0}").andExpect(status().isBadRequest());
		send(patch("/settings"), "{\"timeZone\":\"Mars/Base\"}").andExpect(status().isBadRequest());
	}

	// --- Heartbeat ------------------------------------------------------------------------------

	@Test
	@DisplayName("Heartbeat tạo rồi tái dùng một phiên; thiếu skill thì 400; đổi nội dung mở phiên mới")
	void heartbeatOpensAndReusesSession() throws Exception {
		String deck = UUID.randomUUID().toString();
		String body = "{\"skill\":\"VOCABULARY\",\"refId\":\"%s\"}".formatted(deck);

		String first = send(post("/study/heartbeat"), body).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.activeSeconds").value(0)).andReturn().getResponse().getContentAsString();
		send(post("/study/heartbeat"), body).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.sessionId").value(sessionId(first)));
		send(post("/study/heartbeat"), "{\"skill\":\"VOCABULARY\",\"refId\":\"%s\"}".formatted(UUID.randomUUID()))
				.andExpect(status().isOk());
		send(post("/study/heartbeat"), "{}").andExpect(status().isBadRequest());
		send(post("/study/heartbeat"), "{\"skill\":\"DANCING\"}").andExpect(status().isBadRequest());

		mvc.perform(post("/study/heartbeat").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized());
	}

	private static String sessionId(String json) {
		Matcher m = Pattern.compile("\"sessionId\":\"([^\"]+)\"").matcher(json);
		assertThat(m.find()).isTrue();
		return m.group(1);
	}
}
