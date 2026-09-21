package vn.enlearning.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import vn.enlearning.backend.auth.repository.RefreshTokenRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.RefreshToken;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");
	private static final ClientInfo CLIENT = new ClientInfo("JUnit", "127.0.0.1");
	private static final String RAW = "token-cu-do-client-gui-len";

	@Mock
	private RefreshTokenRepository tokens;

	private RefreshTokenService service;
	private User user;

	@BeforeEach
	void setUp() {
		SecurityProperties properties = new SecurityProperties("j".repeat(32), "en-learning", Duration.ofMinutes(15),
				Duration.ofDays(7), Duration.ofSeconds(10), false, "r".repeat(32), Duration.ofMinutes(10), 5,
				Duration.ofSeconds(60));
		service = new RefreshTokenService(tokens, properties, Clock.fixed(NOW, ZoneOffset.UTC));
		user = new User();
		user.setId(UUID.randomUUID());
		user.setStatus(AccountStatus.ACTIVE);
	}

	private RefreshToken storedToken(Instant expiresAt, Instant revokedAt) {
		RefreshToken token = new RefreshToken();
		token.setId(UUID.randomUUID());
		token.setUser(user);
		token.setTokenHash(TokenHasher.sha256Hex(RAW));
		token.setExpiresAt(expiresAt);
		token.setRevokedAt(revokedAt);
		when(tokens.findActiveOwnerByTokenHash(TokenHasher.sha256Hex(RAW))).thenReturn(Optional.of(token));
		return token;
	}

	private static void assertUnauthorized(Throwable thrown) {
		assertThat(thrown).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.getCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
	}

	@Test
	@DisplayName("Xoay vòng: thu hồi token cũ, cấp token mới và chỉ lưu bản băm")
	void rotateRevokesOldAndIssuesNew() {
		RefreshToken current = storedToken(NOW.plus(Duration.ofDays(1)), null);
		when(tokens.revokeIfActive(current.getId(), NOW)).thenReturn(1);

		RefreshTokenService.Rotation rotation = service.rotate(RAW, CLIENT);

		ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
		verify(tokens).save(saved.capture());
		assertThat(rotation.user()).isSameAs(user);
		assertThat(rotation.rawToken()).isNotEqualTo(RAW).hasSizeGreaterThanOrEqualTo(43);
		assertThat(saved.getValue().getTokenHash()).isEqualTo(TokenHasher.sha256Hex(rotation.rawToken()));
		assertThat(saved.getValue().getTokenHash()).isNotEqualTo(rotation.rawToken());
		assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
		assertThat(saved.getValue().getUserAgent()).isEqualTo("JUnit");
	}

	@Test
	@DisplayName("Token đã thu hồi từ lâu bị dùng lại: thu hồi mọi phiên của tài khoản")
	void reuseOfOldRevokedTokenRevokesEverySession() {
		storedToken(NOW.plus(Duration.ofDays(1)), NOW.minus(Duration.ofHours(1)));

		assertThatThrownBy(() -> service.rotate(RAW, CLIENT)).satisfies(RefreshTokenServiceTest::assertUnauthorized);

		verify(tokens).revokeAllActiveByUserId(user.getId(), NOW);
		verify(tokens, never()).save(any());
	}

	@Test
	@DisplayName("Token vừa bị thu hồi vài giây trước là hai tab refresh cùng lúc, không thu hồi mọi phiên")
	void recentRevocationIsTreatedAsRace() {
		storedToken(NOW.plus(Duration.ofDays(1)), NOW.minusSeconds(3));

		assertThatThrownBy(() -> service.rotate(RAW, CLIENT)).satisfies(RefreshTokenServiceTest::assertUnauthorized);

		verify(tokens, never()).revokeAllActiveByUserId(any(), any());
		verify(tokens, never()).save(any());
	}

	@Test
	@DisplayName("Token hết hạn bị từ chối")
	void expiredTokenIsRejected() {
		storedToken(NOW.minusSeconds(1), null);

		assertThatThrownBy(() -> service.rotate(RAW, CLIENT)).satisfies(RefreshTokenServiceTest::assertUnauthorized);

		verify(tokens, never()).save(any());
	}

	@Test
	@DisplayName("Tài khoản bị khoá sau khi đăng nhập: cắt mọi phiên còn lại")
	void lockedAccountLosesAllSessions() {
		user.setStatus(AccountStatus.LOCKED);
		storedToken(NOW.plus(Duration.ofDays(1)), null);

		assertThatThrownBy(() -> service.rotate(RAW, CLIENT)).satisfies(RefreshTokenServiceTest::assertUnauthorized);

		verify(tokens).revokeAllActiveByUserId(user.getId(), NOW);
		verify(tokens, never()).save(any());
	}

	@Test
	@DisplayName("Token không tồn tại (hoặc của tài khoản đã xoá) bị từ chối")
	void unknownTokenIsRejected() {
		when(tokens.findActiveOwnerByTokenHash(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.rotate("khong-co-that", CLIENT))
				.satisfies(RefreshTokenServiceTest::assertUnauthorized);
	}

	@Test
	@DisplayName("Hai request cùng xoay một token: chỉ request thu hồi được mới thắng")
	void concurrentRotationHasOnlyOneWinner() {
		RefreshToken current = storedToken(NOW.plus(Duration.ofDays(1)), null);
		when(tokens.revokeIfActive(current.getId(), NOW)).thenReturn(0);

		assertThatThrownBy(() -> service.rotate(RAW, CLIENT)).satisfies(RefreshTokenServiceTest::assertUnauthorized);

		verify(tokens, never()).save(any());
	}

	@Test
	@DisplayName("Đăng xuất với token lạ hoặc thiếu token không gây lỗi")
	void revokeIsIdempotent() {
		when(tokens.findByTokenHash(any())).thenReturn(Optional.empty());

		service.revoke("khong-co-that");
		service.revoke(null);
		service.revoke("  ");

		verify(tokens, never()).revokeIfActive(any(), any());
	}
}
