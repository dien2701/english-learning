package vn.enlearning.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import vn.enlearning.backend.auth.dto.ResetPasswordRequest;
import vn.enlearning.backend.auth.repository.PasswordResetTokenRepository;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.PasswordResetToken;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.mail.PasswordResetMailer;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");
	private static final String EMAIL = "hocvien@enlearning.vn";
	private static final String OTP_SECRET = "khoa-otp-chi-dung-cho-test-0123456789abcdef0123456789abcdef";

	@Mock
	private UserRepository users;
	@Mock
	private UserSettingRepository settings;
	@Mock
	private PasswordResetTokenRepository resetTokens;
	@Mock
	private RefreshTokenService refreshTokens;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private PasswordResetMailer mailer;

	private PasswordResetService service;
	private User user;

	@BeforeEach
	void setUp() {
		SecurityProperties properties = new SecurityProperties("j".repeat(32), "en-learning", Duration.ofMinutes(15),
				Duration.ofDays(7), Duration.ofSeconds(10), false, OTP_SECRET, Duration.ofMinutes(10), 5,
				Duration.ofSeconds(60));
		TransactionTemplate transactionTemplate = new TransactionTemplate(mock(PlatformTransactionManager.class));
		service = new PasswordResetService(users, settings, resetTokens, refreshTokens, passwordEncoder, mailer,
				properties, Clock.fixed(NOW, ZoneOffset.UTC), transactionTemplate);

		user = new User();
		user.setId(UUID.randomUUID());
		user.setEmail(EMAIL);
		user.setFullName("Học Viên");
		user.setPasswordHash("hash-cu");
		user.setStatus(AccountStatus.ACTIVE);
	}

	private static String hash(UUID userId, String code) {
		return TokenHasher.hmacSha256Hex(OTP_SECRET, userId + ":" + code);
	}

	private PasswordResetToken openToken(String code, Instant expiresAt, int attempts) {
		PasswordResetToken token = new PasswordResetToken();
		token.setId(UUID.randomUUID());
		token.setUser(user);
		token.setTokenHash(hash(user.getId(), code));
		token.setExpiresAt(expiresAt);
		token.setAttempts(attempts);
		when(resetTokens.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(token));
		return token;
	}

	private static ResetPasswordRequest reset(String code) {
		return new ResetPasswordRequest(EMAIL, code, "matkhaumoi", "matkhaumoi");
	}

	private static void assertInvalidCode(Throwable thrown) {
		assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> {
			assertThat(e.getCode()).isEqualTo(ErrorCode.INVALID_CODE);
			assertThat(e.getFieldErrorKeys()).containsEntry("code", "errors.invalidCode");
		});
	}

	@Test
	@DisplayName("Xin mã: lưu HMAC của mã kèm id người dùng, hạn 10 phút, rồi gửi mã 6 số")
	void requestCodeStoresHashAndSendsSixDigitCode() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(resetTokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.empty());
		UserSetting english = new UserSetting();
		english.setLanguage(UiLanguage.EN);
		when(settings.findByUserId(user.getId())).thenReturn(Optional.of(english));

		service.requestCode(EMAIL);

		ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
		ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
		verify(resetTokens).closeOpenByUserId(user.getId(), NOW);
		verify(resetTokens).save(saved.capture());
		verify(mailer).send(eq(user), code.capture(), eq(Duration.ofMinutes(10)), eq(UiLanguage.EN));

		assertThat(code.getValue()).matches("\\d{6}");
		assertThat(saved.getValue().getTokenHash()).isEqualTo(hash(user.getId(), code.getValue()));
		assertThat(saved.getValue().getTokenHash()).doesNotContain(code.getValue()).hasSize(64);
		assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
		assertThat(saved.getValue().getAttempts()).isZero();
	}

	@Test
	@DisplayName("Email không có tài khoản: không tạo mã, không gửi gì")
	void unknownEmailDoesNothing() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.empty());

		service.requestCode(EMAIL);

		verifyNoInteractions(mailer, resetTokens);
	}

	@Test
	@DisplayName("Tài khoản bị khoá không nhận được mã")
	void lockedAccountGetsNoCode() {
		user.setStatus(AccountStatus.LOCKED);
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		service.requestCode(EMAIL);

		verifyNoInteractions(mailer, resetTokens);
	}

	@Test
	@DisplayName("Xin mã lần nữa trong thời gian chờ 60 giây thì bỏ qua lặng lẽ")
	void requestWithinCooldownIsIgnored() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		PasswordResetToken recent = new PasswordResetToken();
		recent.setCreatedAt(NOW.minusSeconds(30));
		when(resetTokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(recent));

		service.requestCode(EMAIL);

		verify(resetTokens, never()).save(any());
		verifyNoInteractions(mailer);
	}

	@Test
	@DisplayName("Hết thời gian chờ thì cấp mã mới")
	void requestAfterCooldownIssuesNewCode() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		PasswordResetToken old = new PasswordResetToken();
		old.setCreatedAt(NOW.minusSeconds(61));
		when(resetTokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.of(old));
		when(settings.findByUserId(user.getId())).thenReturn(Optional.empty());

		service.requestCode(EMAIL);

		verify(mailer).send(eq(user), any(), any(), eq(UiLanguage.VI));
	}

	@Test
	@DisplayName("Gửi email hỏng không được làm lộ lỗi ra ngoài")
	void mailFailureNeverEscapes() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(resetTokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.empty());
		when(settings.findByUserId(user.getId())).thenReturn(Optional.empty());
		doThrow(new IllegalStateException("SMTP sập")).when(mailer).send(any(), any(), any(), any());

		assertThatCode(() -> service.requestCode(EMAIL)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Mã đúng: đổi mật khẩu, đóng mã, thu hồi mọi refresh token")
	void correctCodeResetsPasswordAndRevokesSessions() {
		PasswordResetToken token = openToken("482913", NOW.plusSeconds(60), 2);
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(passwordEncoder.encode("matkhaumoi")).thenReturn("hash-moi");

		service.resetPassword(reset("482913"));

		assertThat(user.getPasswordHash()).isEqualTo("hash-moi");
		verify(resetTokens).closeOpenByUserId(user.getId(), NOW);
		verify(refreshTokens).revokeAll(user.getId());
		verify(resetTokens, never()).incrementAttempts(token.getId());
	}

	@Test
	@DisplayName("Mã sai: tăng số lần thử và báo lỗi chung, mật khẩu giữ nguyên")
	void wrongCodeCountsAnAttempt() {
		PasswordResetToken token = openToken("482913", NOW.plusSeconds(60), 0);
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.resetPassword(reset("000000"))).satisfies(PasswordResetServiceTest::assertInvalidCode);

		verify(resetTokens).incrementAttempts(token.getId());
		verify(refreshTokens, never()).revokeAll(any());
		assertThat(user.getPasswordHash()).isEqualTo("hash-cu");
	}

	@Test
	@DisplayName("Đủ 5 lần sai thì mã bị huỷ: đúng mã cũng không dùng được và không đếm thêm")
	void exhaustedAttemptsBurnTheCode() {
		PasswordResetToken token = openToken("482913", NOW.plusSeconds(60), 5);
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.resetPassword(reset("482913"))).satisfies(PasswordResetServiceTest::assertInvalidCode);

		verify(resetTokens, never()).incrementAttempts(token.getId());
		assertThat(user.getPasswordHash()).isEqualTo("hash-cu");
	}

	@Test
	@DisplayName("Mã hết hạn bị từ chối")
	void expiredCodeIsRejected() {
		openToken("482913", NOW.minusSeconds(1), 0);
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.resetPassword(reset("482913"))).satisfies(PasswordResetServiceTest::assertInvalidCode);

		verify(refreshTokens, never()).revokeAll(any());
	}

	@Test
	@DisplayName("Không có mã nào đang mở: báo cùng một lỗi")
	void noOpenCodeIsRejected() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(resetTokens.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.resetPassword(reset("482913"))).satisfies(PasswordResetServiceTest::assertInvalidCode);
	}

	@Test
	@DisplayName("Email lạ báo cùng lỗi với mã sai, không lộ email nào có tài khoản")
	void unknownEmailLooksLikeWrongCode() {
		when(users.findByEmail(EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.resetPassword(reset("482913"))).satisfies(PasswordResetServiceTest::assertInvalidCode);

		verify(resetTokens, never()).incrementAttempts(any());
	}

	@Test
	@DisplayName("Băm kèm id người dùng: hai người có cùng mã 6 số vẫn ra hai bản băm khác nhau")
	void sameCodeHashesDifferentlyPerUser() {
		assertThat(hash(UUID.randomUUID(), "123456")).isNotEqualTo(hash(UUID.randomUUID(), "123456"));
	}
}
