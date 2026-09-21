package vn.enlearning.backend.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

import vn.enlearning.backend.auth.repository.RefreshTokenRepository;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.service.ClientInfo;
import vn.enlearning.backend.auth.service.RefreshTokenService;
import vn.enlearning.backend.auth.service.TokenHasher;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.notification.repository.UserNotificationRepository;
import vn.enlearning.backend.security.JwtService;

/**
 * Người dùng, thông báo và bảng điều khiển của Admin, cùng hộp thư phía người học. Chạy trên MySQL thật trong
 * giao dịch test (tự rollback); người dùng tạo riêng cho từng test nên không phụ thuộc dữ liệu seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserNotificationApiTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private RefreshTokenService refreshTokens;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	@Autowired
	private UserNotificationRepository inbox;
	@Autowired
	private jakarta.persistence.EntityManager em;

	private String tag;
	private User adminUser;
	private String admin;

	@BeforeEach
	void setUp() {
		tag = "T" + UUID.randomUUID().toString().replace("-", "");
		adminUser = newUser(Role.ADMIN, AccountStatus.ACTIVE, Instant.now());
		admin = bearer(adminUser);
	}

	// --- 401 / 403 --------------------------------------------------------------------------------

	@Test
	@DisplayName("Endpoint quản trị mới: 401 khi chưa đăng nhập, 403 với USER; hộp thư cần đăng nhập")
	void endpointsRequireProperRole() throws Exception {
		String learner = bearer(newUser(Role.USER, AccountStatus.ACTIVE, Instant.now()));
		UUID any = UUID.randomUUID();
		String body = "{\"title\":\"t\",\"content\":\"c\",\"audience\":\"ALL\"}";
		Object[][] calls = {
				{ get("/admin/dashboard"), null }, { get("/admin/users"), null }, { get("/admin/users/" + any), null },
				{ patch("/admin/users/" + any), "{\"status\":\"LOCKED\"}" }, { get("/admin/notifications"), null },
				{ post("/admin/notifications"), body }, { post("/admin/notifications/" + any + "/send"), null } };
		for (Object[] call : calls) {
			send((MockHttpServletRequestBuilder) call[0], null, (String) call[1]).andExpect(status().isUnauthorized());
		}
		for (Object[] call : calls) {
			send((MockHttpServletRequestBuilder) call[0], learner, (String) call[1]).andExpect(status().isForbidden());
		}
		Object[][] inboxCalls = { { get("/notifications") }, { get("/notifications/unread-count") },
				{ patch("/notifications/" + any + "/read") }, { post("/notifications/read-all") } };
		for (Object[] call : inboxCalls) {
			send((MockHttpServletRequestBuilder) call[0], null, null).andExpect(status().isUnauthorized());
		}
	}

	// --- dashboard --------------------------------------------------------------------------------

	@Test
	@DisplayName("Dashboard trả số liệu, 6 tháng đăng ký và đủ 6 loại nội dung")
	void dashboardShape() throws Exception {
		send(get("/admin/dashboard"), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.overview.totalUsers").isNumber())
				.andExpect(jsonPath("$.data.overview.activeUsers").isNumber())
				.andExpect(jsonPath("$.data.overview.studySessions").isNumber())
				.andExpect(jsonPath("$.data.overview.totalContent").isNumber())
				.andExpect(jsonPath("$.data.signups", hasSize(6)))
				.andExpect(jsonPath("$.data.signups[5].count").isNumber())
				.andExpect(jsonPath("$.data.signups[5].label.en").isString())
				.andExpect(jsonPath("$.data.contentCounts", hasSize(6)))
				.andExpect(jsonPath("$.data.activities", hasSize(0)));
	}

	// --- người dùng -------------------------------------------------------------------------------

	@Test
	@DisplayName("Danh sách và chi tiết người dùng: lọc được, không lộ passwordHash")
	void listAndGetUsers() throws Exception {
		User target = newUser(Role.USER, AccountStatus.ACTIVE, Instant.now());
		target.setFullName("Bạn " + tag);
		users.saveAndFlush(target);

		send(get("/admin/users?search=" + tag + "&role=USER&status=ACTIVE"), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total", is(1)))
				.andExpect(jsonPath("$.data.items[0].email", is(target.getEmail())))
				.andExpect(jsonPath("$.data.items[0].completedLessons", is(0)))
				.andExpect(content().string(not(containsString("passwordHash"))))
				.andExpect(content().string(not(containsString("khong-dung-de-dang-nhap"))));
		send(get("/admin/users?search=" + tag + "&status=LOCKED"), admin, null)
				.andExpect(jsonPath("$.data.total", is(0)));
		send(get("/admin/users/" + target.getId()), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id", is(target.getId().toString())))
				.andExpect(content().string(not(containsString("passwordHash"))));
		send(get("/admin/users/" + UUID.randomUUID()), admin, null).andExpect(status().isNotFound());
		send(get("/admin/users?role=NOPE"), admin, null).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Khoá thu hồi mọi refresh token; mở khoá và đổi vai trò được; không tự đổi mình; body sai là 400")
	void lockUnlockAndRole() throws Exception {
		User target = newUser(Role.USER, AccountStatus.ACTIVE, Instant.now());
		String raw = refreshTokens.issue(target, new ClientInfo("test", "127.0.0.1"));
		String hash = TokenHasher.sha256Hex(raw);

		send(patch("/admin/users/" + target.getId()), admin, "{\"status\":\"LOCKED\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status", is("LOCKED")));
		em.clear(); // thu hồi bằng UPDATE hàng loạt, không cập nhật thực thể đang nằm trong bộ nhớ
		org.assertj.core.api.Assertions.assertThat(refreshTokenRepository.findByTokenHash(hash).orElseThrow().getRevokedAt())
				.isNotNull();

		send(patch("/admin/users/" + target.getId()), admin, "{\"status\":\"ACTIVE\",\"role\":\"ADMIN\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status", is("ACTIVE")))
				.andExpect(jsonPath("$.data.role", is("ADMIN")));

		send(patch("/admin/users/" + adminUser.getId()), admin, "{\"status\":\"LOCKED\"}")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("INVALID_STATE")));
		send(patch("/admin/users/" + target.getId()), admin, "{}").andExpect(status().isBadRequest());
		send(patch("/admin/users/" + target.getId()), admin, "{\"status\":\"PENDING\"}").andExpect(status().isBadRequest());
		send(patch("/admin/users/" + UUID.randomUUID()), admin, "{\"status\":\"LOCKED\"}").andExpect(status().isNotFound());
	}

	// --- thông báo --------------------------------------------------------------------------------

	@Test
	@DisplayName("Nháp rồi gửi: bản nhận khớp recipientCount, người học đọc và đánh dấu đã đọc, gửi lại là 409")
	void draftSendAndRead() throws Exception {
		User a = newUser(Role.USER, AccountStatus.ACTIVE, Instant.now());
		User b = newUser(Role.USER, AccountStatus.ACTIVE, null);
		User locked = newUser(Role.USER, AccountStatus.LOCKED, Instant.now());
		String tokenA = bearer(a);
		String tokenB = bearer(b);

		MvcResult draft = send(post("/admin/notifications"), admin,
				"{\"title\":\"Tin " + tag + "\",\"content\":\"Nội dung\",\"audience\":\"ALL\"}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.status", is("DRAFT")))
				.andExpect(jsonPath("$.data.recipientCount").doesNotExist())
				.andReturn();
		String id = JsonPath.read(json(draft), "$.data.id");
		send(get("/notifications/unread-count"), tokenA, null).andExpect(jsonPath("$.data.count", is(0)));

		MvcResult sent = send(post("/admin/notifications/" + id + "/send"), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status", is("SENT")))
				.andExpect(jsonPath("$.data.sentAt").exists())
				.andReturn();
		int recipients = JsonPath.read(json(sent), "$.data.recipientCount");
		org.assertj.core.api.Assertions.assertThat(recipients).isGreaterThanOrEqualTo(3);
		org.assertj.core.api.Assertions.assertThat(inbox.countByNotificationId(UUID.fromString(id))).isEqualTo(recipients);

		send(post("/admin/notifications/" + id + "/send"), admin, null)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code", is("INVALID_STATE")));
		send(get("/notifications/unread-count"), bearer(locked), null).andExpect(jsonPath("$.data.count", is(0)));

		send(get("/notifications/unread-count"), tokenA, null).andExpect(jsonPath("$.data.count", is(1)));
		MvcResult list = send(get("/notifications"), tokenA, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.items[0].title.vi", is("Tin " + tag)))
				.andExpect(jsonPath("$.data.items[0].isRead", is(false)))
				.andReturn();
		String itemId = JsonPath.read(json(list), "$.data.items[0].id");

		send(patch("/notifications/" + itemId + "/read"), tokenB, null).andExpect(status().isNotFound());
		send(patch("/notifications/" + itemId + "/read"), tokenA, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isRead", is(true)));
		send(get("/notifications/unread-count"), tokenA, null).andExpect(jsonPath("$.data.count", is(0)));

		send(get("/notifications/unread-count"), tokenB, null).andExpect(jsonPath("$.data.count", is(1)));
		send(post("/notifications/read-all"), tokenB, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.count", is(1)));
		send(get("/notifications/unread-count"), tokenB, null).andExpect(jsonPath("$.data.count", is(0)));
	}

	@Test
	@DisplayName("Nhóm nhận: ACTIVE theo lastActiveAt 30 ngày, INACTIVE là phần còn lại, ADMIN chỉ quản trị viên; gửi ngay khi send=true")
	void audiences() throws Exception {
		User recent = newUser(Role.USER, AccountStatus.ACTIVE, Instant.now().minus(5, ChronoUnit.DAYS));
		User stale = newUser(Role.USER, AccountStatus.ACTIVE, Instant.now().minus(90, ChronoUnit.DAYS));
		User never = newUser(Role.USER, AccountStatus.ACTIVE, null);

		sendNow("ACTIVE");
		expectUnread(recent, 1);
		expectUnread(stale, 0);
		expectUnread(never, 0);

		sendNow("INACTIVE");
		expectUnread(recent, 1);
		expectUnread(stale, 1);
		expectUnread(never, 1);

		sendNow("ADMIN");
		expectUnread(recent, 1);
		expectUnread(stale, 1);
		expectUnread(adminUser, 2); // ACTIVE (hoạt động gần đây) + ADMIN

		send(post("/admin/notifications"), admin, "{\"title\":\"\",\"content\":\"c\",\"audience\":\"ALL\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.title").exists());
		send(get("/admin/notifications"), admin, null)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total").isNumber());
	}

	// --- tiện ích ---------------------------------------------------------------------------------

	private void sendNow(String audience) throws Exception {
		send(post("/admin/notifications"), admin,
				"{\"title\":\"Tin " + tag + "\",\"content\":\"c\",\"audience\":\"" + audience + "\",\"send\":true}")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.status", is("SENT")));
	}

	/** Số thông báo chưa đọc của chính người dùng (mỗi lần gửi đều tạo tiêu đề có nhãn ngẫu nhiên riêng). */
	private void expectUnread(User user, int expected) throws Exception {
		send(get("/notifications/unread-count"), bearer(user), null).andExpect(jsonPath("$.data.count", is(expected)));
	}

	private User newUser(Role role, AccountStatus status, Instant lastActiveAt) {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		user.setRole(role);
		user.setStatus(status);
		user.setLastActiveAt(lastActiveAt);
		return users.saveAndFlush(user);
	}

	private String bearer(User user) {
		return "Bearer " + jwt.issueAccessToken(user);
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
}
