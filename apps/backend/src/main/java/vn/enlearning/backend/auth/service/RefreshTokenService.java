package vn.enlearning.backend.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.RefreshTokenRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.RefreshToken;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;

/**
 * Vòng đời refresh token: phát hành, xoay vòng, thu hồi. Token là chuỗi ngẫu nhiên 256 bit; DB chỉ giữ
 * SHA-256 của nó (chuỗi ngẫu nhiên dài nên không cần muối hay HMAC).
 *
 * <p>Mỗi lần refresh, token cũ bị thu hồi và một token mới được cấp. Nếu một token đã thu hồi lại được
 * đưa ra dùng, khả năng cao nó đã bị đánh cắp, nên mọi phiên của tài khoản bị thu hồi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private static final int TOKEN_BYTES = 32;

	private final RefreshTokenRepository tokens;
	private final SecurityProperties properties;
	private final Clock clock;

	/** Người dùng sở hữu phiên và token thô mới; token thô chỉ tồn tại ở đây để đặt vào cookie. */
	public record Rotation(User user, String rawToken) {
	}

	@Transactional
	public String issue(User user, ClientInfo client) {
		String raw = TokenHasher.randomToken(TOKEN_BYTES);
		RefreshToken token = new RefreshToken();
		token.setUser(user);
		token.setTokenHash(TokenHasher.sha256Hex(raw));
		token.setExpiresAt(clock.instant().plus(properties.refreshTokenTtl()));
		token.setUserAgent(client.userAgent());
		token.setIpAddress(client.ipAddress());
		tokens.save(token);
		return raw;
	}

	/**
	 * Đổi token đang dùng lấy token mới. {@code noRollbackFor}: khi phát hiện dùng lại token, việc thu hồi
	 * mọi phiên phải được ghi lại dù sau đó ném lỗi 401.
	 */
	@Transactional(noRollbackFor = ApiException.class)
	public Rotation rotate(String rawToken, ClientInfo client) {
		Instant now = clock.instant();
		RefreshToken current = tokens.findActiveOwnerByTokenHash(TokenHasher.sha256Hex(rawToken))
				.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
		User user = current.getUser();

		if (current.getRevokedAt() != null) {
			// Hai tab refresh cùng lúc sẽ cùng đưa một token; chỉ coi là bị đánh cắp khi đã thu hồi từ lâu.
			boolean recentRace = current.getRevokedAt().plus(properties.refreshReuseGrace()).isAfter(now);
			if (!recentRace) {
				log.warn("Refresh token đã thu hồi bị dùng lại, thu hồi mọi phiên của user {}", user.getId());
				tokens.revokeAllActiveByUserId(user.getId(), now);
			}
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}
		if (!current.getExpiresAt().isAfter(now)) {
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}
		if (user.getStatus() != AccountStatus.ACTIVE) {
			// Tài khoản bị khoá sau khi đăng nhập: cắt luôn mọi phiên còn lại.
			tokens.revokeAllActiveByUserId(user.getId(), now);
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}
		if (tokens.revokeIfActive(current.getId(), now) == 0) {
			// Request khác vừa thu hồi đúng token này giữa hai câu lệnh của ta.
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}
		return new Rotation(user, issue(user, client));
	}

	/** Đăng xuất: thu hồi token nếu có, không lỗi khi token lạ để gọi lại nhiều lần vẫn an toàn. */
	@Transactional
	public void revoke(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return;
		}
		tokens.findByTokenHash(TokenHasher.sha256Hex(rawToken))
				.ifPresent(token -> tokens.revokeIfActive(token.getId(), clock.instant()));
	}

	@Transactional
	public void revokeAll(UUID userId) {
		tokens.revokeAllActiveByUserId(userId, clock.instant());
	}
}
