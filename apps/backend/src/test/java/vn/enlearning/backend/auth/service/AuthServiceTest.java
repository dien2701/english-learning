package vn.enlearning.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.enlearning.backend.auth.dto.LoginRequest;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.security.JwtService;

/** Các bất biến bảo mật của đăng nhập: không lộ email nào tồn tại, không lộ trạng thái khoá cho người không biết mật khẩu. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final ClientInfo CLIENT = new ClientInfo("JUnit", "127.0.0.1");

	@Mock
	private UserRepository users;
	@Mock
	private UserSettingRepository settings;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@Mock
	private RefreshTokenService refreshTokens;

	private AuthService service;
	private User user;

	@BeforeEach
	void setUp() {
		when(passwordEncoder.encode(any())).thenReturn("hash-gia");
		service = new AuthService(users, settings, passwordEncoder, jwtService, refreshTokens,
				Clock.fixed(Instant.parse("2026-09-20T10:00:00Z"), ZoneOffset.UTC));

		user = new User();
		user.setId(UUID.randomUUID());
		user.setEmail("hocvien@enlearning.vn");
		user.setPasswordHash("hash-that");
		user.setStatus(AccountStatus.ACTIVE);
	}

	private static void assertCode(Throwable thrown, ErrorCode expected) {
		assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getCode()).isEqualTo(expected));
	}

	@Test
	@DisplayName("Email không tồn tại vẫn tốn một lần so BCrypt để thời gian phản hồi không lộ ra")
	void unknownEmailStillPaysOneBcryptComparison() {
		when(users.findByEmail("khong-co@enlearning.vn")).thenReturn(Optional.empty());
		when(passwordEncoder.matches("matkhau1", "hash-gia")).thenReturn(false);

		assertThatThrownBy(() -> service.login(new LoginRequest("khong-co@enlearning.vn", "matkhau1"), CLIENT))
				.satisfies(e -> assertCode(e, ErrorCode.INVALID_CREDENTIALS));

		verify(passwordEncoder).matches("matkhau1", "hash-gia");
	}

	@Test
	@DisplayName("Sai mật khẩu và không có tài khoản cùng trả INVALID_CREDENTIALS")
	void wrongPasswordLooksLikeUnknownEmail() {
		when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("sai-mat-khau", "hash-that")).thenReturn(false);

		assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "sai-mat-khau"), CLIENT))
				.satisfies(e -> assertCode(e, ErrorCode.INVALID_CREDENTIALS));
	}

	@Test
	@DisplayName("Tài khoản bị khoá chỉ báo khoá khi đã đúng mật khẩu")
	void lockedStatusIsOnlyRevealedWithTheRightPassword() {
		user.setStatus(AccountStatus.LOCKED);
		when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("sai-mat-khau", "hash-that")).thenReturn(false);
		when(passwordEncoder.matches("dung-mat-khau", "hash-that")).thenReturn(true);

		assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "sai-mat-khau"), CLIENT))
				.satisfies(e -> assertCode(e, ErrorCode.INVALID_CREDENTIALS));
		assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "dung-mat-khau"), CLIENT))
				.satisfies(e -> assertCode(e, ErrorCode.ACCOUNT_LOCKED));

		verify(refreshTokens, never()).issue(any(), any());
	}

	@Test
	@DisplayName("Mật khẩu quá 72 byte không bao giờ khớp và không làm BCrypt ném lỗi")
	void overlongPasswordNeverMatches() {
		when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.login(new LoginRequest(user.getEmail(), "x".repeat(73)), CLIENT))
				.satisfies(e -> assertCode(e, ErrorCode.INVALID_CREDENTIALS));

		verify(passwordEncoder, never()).matches(any(), any());
	}

	@Test
	@DisplayName("Đăng nhập đúng: cấp access token và refresh token, ghi lại lần hoạt động cuối")
	void successfulLoginStartsSession() {
		when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("dung-mat-khau", "hash-that")).thenReturn(true);
		when(jwtService.issueAccessToken(user)).thenReturn("jwt");
		when(refreshTokens.issue(user, CLIENT)).thenReturn("refresh-tho");

		AuthService.Session session = service.login(new LoginRequest(user.getEmail(), "dung-mat-khau"), CLIENT);

		assertThat(session.accessToken()).isEqualTo("jwt");
		assertThat(session.refreshToken()).isEqualTo("refresh-tho");
		assertThat(session.user().email()).isEqualTo(user.getEmail());
		assertThat(user.getLastActiveAt()).isEqualTo(Instant.parse("2026-09-20T10:00:00Z"));
	}
}
