package vn.enlearning.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.auth.repository.EmailVerificationCodeRepository;
import vn.enlearning.backend.auth.service.TokenHasher;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.EmailVerificationCode;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.mail.EmailMessage;
import vn.enlearning.backend.mail.EmailSender;

/**
 * Chạy toàn bộ chồng Security → Controller → Service → JPA trên MySQL thật. Mỗi test nằm trong giao dịch tự
 * rollback và dùng email ngẫu nhiên, nên không để lại dữ liệu và không đụng tài khoản seed nếu có.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiTests {

	private static final String PASSWORD = "matkhau-dung-1";
	private static final Pattern SIX_DIGITS = Pattern.compile("(?<!\\d)\\d{6}(?!\\d)");
	private static final Pattern REFRESH_COOKIE = Pattern.compile("refresh_token=([^;]*)");

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private UserSettingRepository settings;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private EmailVerificationCodeRepository verificationCodes;
	@Autowired
	private SecurityProperties securityProperties;

	/** Thay bean thật để bắt nội dung email; không bao giờ có email nào rời khỏi máy. */
	@MockitoBean
	private EmailSender emailSender;

	// --- tiện ích -------------------------------------------------------------------------------

	private static String newEmail() {
		return "it-" + UUID.randomUUID() + "@test.local";
	}

	private ResultActions post(String url, String json, Cookie... cookies) throws Exception {
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		// MockMvc từ chối danh sách cookie rỗng.
		if (cookies.length > 0) {
			request.cookie(cookies);
		}
		return mvc.perform(request);
	}

	private static String credentials(String email, String password) {
		return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
	}

	private static final String VERIFY_CODE = "246810";

	/** Tạo sẵn mã xác minh cho email (không qua SMTP) rồi dựng thân request đăng ký kèm mã đó. */
	private String registerJson(String email, String password) {
		String normalized = vn.enlearning.backend.auth.dto.Emails.normalize(email);
		EmailVerificationCode code = new EmailVerificationCode();
		code.setEmail(normalized);
		code.setCodeHash(TokenHasher.hmacSha256Hex(securityProperties.resetCodeSecret(),
				"verify:" + normalized + ":" + VERIFY_CODE));
		code.setExpiresAt(java.time.Instant.now().plusSeconds(600));
		verificationCodes.saveAndFlush(code);
		// acceptTerms là ô của form đăng ký mà backend không dùng: phải bị bỏ qua chứ không gây lỗi.
		return "{\"fullName\":\"Người Thử\",\"email\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\",\"code\":\"%s\",\"acceptTerms\":true}"
				.formatted(email, password, password, VERIFY_CODE);
	}

	private static String resetJson(String email, String code, String password) {
		return "{\"email\":\"%s\",\"code\":\"%s\",\"password\":\"%s\",\"confirmPassword\":\"%s\"}"
				.formatted(email, code, password, password);
	}

	private User createUser(Role role, AccountStatus status) {
		User user = new User();
		user.setEmail(newEmail());
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setFullName("Tài khoản thử");
		user.setRole(role);
		user.setStatus(status);
		return users.saveAndFlush(user);
	}

	private MvcResult login(String email, String password) throws Exception {
		return post("/auth/login", credentials(email, password)).andExpect(status().isOk()).andReturn();
	}

	private static Cookie refreshCookie(MvcResult result) {
		String header = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		assertThat(header).isNotNull();
		Matcher matcher = REFRESH_COOKIE.matcher(header);
		assertThat(matcher.find()).isTrue();
		return new Cookie("refresh_token", matcher.group(1));
	}

	private static String accessToken(MvcResult result) throws Exception {
		String body = result.getResponse().getContentAsString();
		Matcher matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);
		assertThat(matcher.find()).isTrue();
		return matcher.group(1);
	}

	private static String bearer(MvcResult loginResult) throws Exception {
		return "Bearer " + accessToken(loginResult);
	}

	private String issuedCode() {
		ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
		verify(emailSender).send(captor.capture());
		Matcher matcher = SIX_DIGITS.matcher(captor.getValue().text());
		assertThat(matcher.find()).as("email phải chứa mã 6 số").isTrue();
		return matcher.group();
	}

	private static String differentFrom(String code) {
		return code.equals("000000") ? "111111" : "000000";
	}

	// --- đăng ký ---------------------------------------------------------------------------------

	@Test
	@DisplayName("Đăng ký: 201, có access token, refresh token chỉ nằm trong cookie HttpOnly, không lộ passwordHash")
	void registerCreatesAccountAndSession() throws Exception {
		String email = newEmail();

		MvcResult result = post("/auth/register", registerJson(email, PASSWORD))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.user.email").value(email))
				.andExpect(jsonPath("$.data.user.role").value("USER"))
				.andExpect(jsonPath("$.data.user.status").value("ACTIVE"))
				.andExpect(jsonPath("$.data.user.createdAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.+")))
				.andExpect(jsonPath("$.data.user.passwordHash").doesNotExist())
				.andExpect(jsonPath("$.data.refreshToken").doesNotExist())
				.andReturn();

		String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		assertThat(setCookie).contains("HttpOnly", "SameSite=Lax", "Path=/api/auth", "Max-Age=604800");
		assertThat(result.getResponse().getContentAsString()).doesNotContain(refreshCookie(result).getValue());

		User saved = users.findByEmail(email).orElseThrow();
		assertThat(passwordEncoder.matches(PASSWORD, saved.getPasswordHash())).isTrue();
		assertThat(saved.getPasswordHash()).startsWith("$2").contains("$12$");
		assertThat(settings.findByUserId(saved.getId())).isPresent();
	}

	@Test
	@DisplayName("Đăng ký trùng email (không phân biệt hoa thường) → 409 kèm lỗi ở ô email")
	void registerRejectsDuplicateEmail() throws Exception {
		String email = newEmail();
		post("/auth/register", registerJson(email, PASSWORD)).andExpect(status().isCreated());

		post("/auth/register", registerJson(email.toUpperCase(), PASSWORD))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("EMAIL_TAKEN"))
				.andExpect(jsonPath("$.messageKey").value("errors.emailTaken"))
				.andExpect(jsonPath("$.fieldErrorKeys.email").value("errors.emailTaken"));
	}

	@Test
	@DisplayName("Đăng ký sai dữ liệu → 400, lỗi đổ đúng từng ô bằng khoá dịch")
	void registerValidatesEachField() throws Exception {
		post("/auth/register",
				"{\"fullName\":\"A\",\"email\":\"khong-hop-le\",\"password\":\"123\",\"confirmPassword\":\"khac\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"))
				.andExpect(jsonPath("$.messageKey").value("errors.checkRegisterInfo"))
				.andExpect(jsonPath("$.fieldErrorKeys.fullName").value("auth.validation.nameShort"))
				.andExpect(jsonPath("$.fieldErrorKeys.email").value("auth.validation.emailFormat"))
				.andExpect(jsonPath("$.fieldErrorKeys.password").value("auth.validation.passwordMin"))
				.andExpect(jsonPath("$.fieldErrorKeys.confirmPassword").value("auth.validation.confirmMismatch"));

		// Để trống thì báo "bắt buộc", không báo "sai định dạng".
		post("/auth/register", "{}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.fullName").value("auth.validation.nameRequired"))
				.andExpect(jsonPath("$.fieldErrorKeys.email").value("auth.validation.emailRequired"))
				.andExpect(jsonPath("$.fieldErrorKeys.password").value("auth.validation.passwordRequired"));
	}

	@Test
	@DisplayName("Mật khẩu quá 72 byte bị từ chối ở ô mật khẩu thay vì làm BCrypt ném 500")
	void registerRejectsPasswordBeyondBcryptLimit() throws Exception {
		post("/auth/register", registerJson(newEmail(), "x".repeat(73)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.password").value("errors.newPasswordInvalid"));
	}

	@Test
	@DisplayName("JSON hỏng → 400 đúng vỏ lỗi, không phải trang lỗi mặc định")
	void malformedJsonIsBadRequest() throws Exception {
		post("/auth/login", "{khong phai json")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("HTTP_400"))
				.andExpect(jsonPath("$.messageKey").value("errors.badRequest"));
	}

	// --- đăng nhập -------------------------------------------------------------------------------

	@Test
	@DisplayName("Đăng nhập đúng → 200, email không phân biệt hoa thường")
	void loginSucceeds() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);

		post("/auth/login", credentials("  " + user.getEmail().toUpperCase() + " ", PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.user.id").value(user.getId().toString()));

		assertThat(users.findById(user.getId()).orElseThrow().getLastActiveAt()).isNotNull();
	}

	@Test
	@DisplayName("Sai mật khẩu và email không tồn tại trả về đúng cùng một phản hồi")
	void wrongPasswordAndUnknownEmailAreIndistinguishable() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);

		String wrongPassword = post("/auth/login", credentials(user.getEmail(), "sai-mat-khau"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
				.andExpect(jsonPath("$.messageKey").value("errors.invalidCredentials"))
				.andReturn().getResponse().getContentAsString();
		String unknownEmail = post("/auth/login", credentials(newEmail(), "sai-mat-khau"))
				.andExpect(status().isUnauthorized())
				.andReturn().getResponse().getContentAsString();

		assertThat(wrongPassword).isEqualTo(unknownEmail);
	}

	@Test
	@DisplayName("Tài khoản bị khoá: đúng mật khẩu → 403, sai mật khẩu vẫn chỉ là 401 (không lộ trạng thái)")
	void lockedAccountCannotLogIn() throws Exception {
		User locked = createUser(Role.USER, AccountStatus.LOCKED);

		post("/auth/login", credentials(locked.getEmail(), PASSWORD))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"))
				.andExpect(jsonPath("$.messageKey").value("errors.accountLocked"));
		post("/auth/login", credentials(locked.getEmail(), "sai-mat-khau"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	@DisplayName("Access token cũ đính kèm không được cản đăng nhập, đăng ký, quên mật khẩu")
	void staleBearerTokenDoesNotBlockPublicEndpoints() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		String stale = "Bearer token.da.het.han";

		mvc.perform(MockMvcRequestBuilders.post("/auth/login").header(HttpHeaders.AUTHORIZATION, stale)
				.contentType(MediaType.APPLICATION_JSON).content(credentials(user.getEmail(), PASSWORD)))
				.andExpect(status().isOk());
		mvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password").header(HttpHeaders.AUTHORIZATION, stale)
				.contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + newEmail() + "\"}"))
				.andExpect(status().isOk());
		mvc.perform(get("/auth/check-email").param("email", newEmail()).header(HttpHeaders.AUTHORIZATION, stale))
				.andExpect(status().isOk());
	}

	// --- /auth/me và phân quyền ---------------------------------------------------------------------

	@Test
	@DisplayName("/auth/me cần access token hợp lệ; thiếu hoặc sai → 401 JSON")
	void meRequiresValidAccessToken() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		MvcResult login = login(user.getEmail(), PASSWORD);

		mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(login)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(user.getEmail()))
				.andExpect(jsonPath("$.data.passwordHash").doesNotExist());

		mvc.perform(get("/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.messageKey").value("errors.sessionExpired"));
		mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer khong.phai.jwt"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	@DisplayName("Tài khoản bị khoá sau khi đăng nhập: /auth/me trả 403 ngay dù access token còn hạn")
	void lockedAfterLoginIsBlockedAtMe() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		MvcResult login = login(user.getEmail(), PASSWORD);
		user.setStatus(AccountStatus.LOCKED);
		users.saveAndFlush(user);

		mvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(login)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
	}

	@Test
	@DisplayName("/admin/** chỉ cho ADMIN: khách 401, USER 403, ADMIN qua được (404 vì chưa có endpoint)")
	void adminAreaIsRestrictedToAdmins() throws Exception {
		MvcResult asUser = login(createUser(Role.USER, AccountStatus.ACTIVE).getEmail(), PASSWORD);
		MvcResult asAdmin = login(createUser(Role.ADMIN, AccountStatus.ACTIVE).getEmail(), PASSWORD);

		mvc.perform(get("/admin/bat-ky-dau"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
		mvc.perform(get("/admin/bat-ky-dau").header(HttpHeaders.AUTHORIZATION, bearer(asUser)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"))
				.andExpect(jsonPath("$.messageKey").value("errors.forbidden"));
		mvc.perform(get("/admin/bat-ky-dau").header(HttpHeaders.AUTHORIZATION, bearer(asAdmin)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("HTTP_404"));
	}

	@Test
	@DisplayName("Endpoint chưa khai báo mặc định cần đăng nhập")
	void everythingElseRequiresAuthentication() throws Exception {
		mvc.perform(get("/flashcards")).andExpect(status().isUnauthorized());
	}

	// --- refresh và đăng xuất ---------------------------------------------------------------------

	@Test
	@DisplayName("Refresh: đổi cookie lấy access token mới và cookie mới; token cũ vừa dùng không tự đá phiên mới")
	void refreshRotatesTheCookie() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		Cookie first = refreshCookie(login(user.getEmail(), PASSWORD));

		MvcResult refreshed = post("/auth/refresh", "", first)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.user.id").value(user.getId().toString()))
				.andReturn();
		Cookie second = refreshCookie(refreshed);
		assertThat(second.getValue()).isNotEqualTo(first.getValue());

		// Dùng lại token cũ ngay lập tức = hai tab refresh cùng lúc: bị từ chối nhưng phiên mới còn sống.
		post("/auth/refresh", "", first).andExpect(status().isUnauthorized());
		post("/auth/refresh", "", second).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Refresh không có cookie hoặc cookie rác → 401 JSON")
	void refreshWithoutValidCookieIsUnauthorized() throws Exception {
		post("/auth/refresh", "")
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
		post("/auth/refresh", "", new Cookie("refresh_token", "khong-co-that")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Đăng xuất thu hồi refresh token và xoá cookie; gọi lại vẫn 200")
	void logoutRevokesTheRefreshToken() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		Cookie cookie = refreshCookie(login(user.getEmail(), PASSWORD));

		MvcResult result = mvc.perform(delete("/auth/session").cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.loggedOut").value(true))
				.andReturn();
		assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)).contains("refresh_token=", "Max-Age=0");

		post("/auth/refresh", "", cookie).andExpect(status().isUnauthorized());
		mvc.perform(delete("/auth/session")).andExpect(status().isOk());
	}

	// --- check-email ----------------------------------------------------------------------------

	@Test
	@DisplayName("check-email: email mới còn trống, email đã đăng ký thì không; định dạng sai → 400")
	void checkEmailReportsAvailability() throws Exception {
		String email = newEmail();
		mvc.perform(get("/auth/check-email").param("email", email))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.available").value(true));

		post("/auth/register", registerJson(email, PASSWORD)).andExpect(status().isCreated());
		mvc.perform(get("/auth/check-email").param("email", email.toUpperCase()))
				.andExpect(jsonPath("$.data.available").value(false));

		mvc.perform(get("/auth/check-email").param("email", "khong-hop-le"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.email").value("auth.validation.emailFormat"));
		mvc.perform(get("/auth/check-email")).andExpect(status().isBadRequest());
	}

	// --- quên và đặt lại mật khẩu ------------------------------------------------------------------

	@Test
	@DisplayName("Quên mật khẩu → nhận mã 6 số qua email → đặt lại → mật khẩu cũ hết dùng, mọi phiên cũ bị thu hồi")
	void forgotAndResetPasswordEndToEnd() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		Cookie oldSession = refreshCookie(login(user.getEmail(), PASSWORD));

		post("/auth/forgot-password", "{\"email\":\"" + user.getEmail() + "\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.message").isNotEmpty());
		ArgumentCaptor<EmailMessage> mail = ArgumentCaptor.forClass(EmailMessage.class);
		verify(emailSender).send(mail.capture());
		assertThat(mail.getValue().to()).isEqualTo(user.getEmail());
		String code = issuedCodeFrom(mail.getValue());
		assertThat(mail.getValue().subject()).doesNotContain(code);

		post("/auth/reset-password", resetJson(user.getEmail(), differentFrom(code), "matkhau-moi-2"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CODE"))
				.andExpect(jsonPath("$.fieldErrorKeys.code").value("errors.invalidCode"));

		post("/auth/reset-password", resetJson(user.getEmail(), code, "matkhau-moi-2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.message").isNotEmpty());

		post("/auth/login", credentials(user.getEmail(), PASSWORD)).andExpect(status().isUnauthorized());
		post("/auth/login", credentials(user.getEmail(), "matkhau-moi-2")).andExpect(status().isOk());
		post("/auth/refresh", "", oldSession).andExpect(status().isUnauthorized());

		// Mã chỉ dùng được một lần.
		post("/auth/reset-password", resetJson(user.getEmail(), code, "mat-khau-khac-3"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CODE"));
	}

	@Test
	@DisplayName("Quên mật khẩu trả cùng một phản hồi dù email có tài khoản hay không, và không gửi gì cho email lạ")
	void forgotPasswordDoesNotRevealWhichEmailsExist() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);

		String known = post("/auth/forgot-password", "{\"email\":\"" + user.getEmail() + "\"}")
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String unknown = post("/auth/forgot-password", "{\"email\":\"" + newEmail() + "\"}")
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		assertThat(known).isEqualTo(unknown);
		verify(emailSender, times(1)).send(any());
	}

	@Test
	@DisplayName("Quên mật khẩu với email sai định dạng → 400 ở ô email")
	void forgotPasswordValidatesEmail() throws Exception {
		post("/auth/forgot-password", "{\"email\":\"khong-hop-le\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.email").value("auth.validation.emailFormat"));
		post("/auth/forgot-password", "{}").andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Nhập sai 5 lần thì mã bị huỷ: đúng mã cũng không dùng được nữa")
	void fiveWrongAttemptsBurnTheCode() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);
		post("/auth/forgot-password", "{\"email\":\"" + user.getEmail() + "\"}").andExpect(status().isOk());
		String code = issuedCode();

		for (int attempt = 0; attempt < 5; attempt++) {
			post("/auth/reset-password", resetJson(user.getEmail(), differentFrom(code), "matkhau-moi-2"))
					.andExpect(status().isBadRequest());
		}

		post("/auth/reset-password", resetJson(user.getEmail(), code, "matkhau-moi-2"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CODE"));
		post("/auth/login", credentials(user.getEmail(), PASSWORD)).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Xin mã liên tiếp trong 60 giây chỉ gửi một email")
	void requestingCodesTooFastSendsOnlyOneEmail() throws Exception {
		User user = createUser(Role.USER, AccountStatus.ACTIVE);

		post("/auth/forgot-password", "{\"email\":\"" + user.getEmail() + "\"}").andExpect(status().isOk());
		post("/auth/forgot-password", "{\"email\":\"" + user.getEmail() + "\"}").andExpect(status().isOk());

		verify(emailSender, times(1)).send(any());
	}

	@Test
	@DisplayName("Tài khoản bị khoá không nhận mã đặt lại")
	void lockedAccountGetsNoResetCode() throws Exception {
		User locked = createUser(Role.USER, AccountStatus.LOCKED);

		post("/auth/forgot-password", "{\"email\":\"" + locked.getEmail() + "\"}").andExpect(status().isOk());

		verify(emailSender, never()).send(any());
	}

	@Test
	@DisplayName("Đặt lại mật khẩu: mật khẩu mới ngắn hoặc xác nhận không khớp → 400 ở đúng ô, chưa tính là lần thử mã")
	void resetPasswordValidatesNewPassword() throws Exception {
		post("/auth/reset-password",
				"{\"email\":\"a@b.co\",\"code\":\"123456\",\"password\":\"123\",\"confirmPassword\":\"khac\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.messageKey").value("errors.checkInfo"))
				.andExpect(jsonPath("$.fieldErrorKeys.password").value("auth.validation.passwordMin"))
				.andExpect(jsonPath("$.fieldErrorKeys.confirmPassword").value("auth.validation.confirmMismatch"));
		post("/auth/reset-password", "{\"email\":\"a@b.co\",\"password\":\"matkhau-moi\"}")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.code").value("auth.validation.codeRequired"));
	}

	private static String issuedCodeFrom(EmailMessage message) {
		Matcher matcher = SIX_DIGITS.matcher(message.text());
		assertThat(matcher.find()).as("email phải chứa mã 6 số").isTrue();
		return matcher.group();
	}
}
