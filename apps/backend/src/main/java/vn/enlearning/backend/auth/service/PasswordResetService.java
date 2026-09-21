package vn.enlearning.backend.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.dto.ResetPasswordRequest;
import vn.enlearning.backend.auth.repository.PasswordResetTokenRepository;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.config.AsyncConfig;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.PasswordResetToken;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.mail.PasswordResetMailer;

/**
 * Quên và đặt lại mật khẩu bằng mã OTP 6 số gửi qua email.
 *
 * <p>Mã chỉ có 10^6 khả năng nên phòng thủ nằm ở các lớp quanh nó: sống ngắn, dùng một lần, tối đa vài lần
 * nhập sai rồi huỷ, cách nhau một khoảng khi xin mã mới, và DB chỉ lưu HMAC-SHA256 có khoá bí mật (kèm id
 * người dùng) nên lộ bảng cũng không dò ngược được.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private static final int CODE_BOUND = 1_000_000;

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final PasswordResetTokenRepository resetTokens;
	private final RefreshTokenService refreshTokens;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetMailer mailer;
	private final SecurityProperties properties;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	/** Mã vừa phát hành, đang chờ được gửi đi. */
	private record IssuedCode(User user, String code, UiLanguage language) {
	}

	/**
	 * Xin mã. Chạy nền và nuốt mọi lỗi: người gọi luôn nhận cùng một phản hồi nên thời gian xử lý cũng
	 * không được lộ email nào có tài khoản. Trong thời gian chờ giữa hai lần xin thì bỏ qua lặng lẽ.
	 */
	@Async(AsyncConfig.AUTH_TASK_EXECUTOR)
	public void requestCode(String email) {
		try {
			IssuedCode issued = transactionTemplate.execute(status -> issueCode(email));
			// Gửi sau khi đã commit: email không được đi nếu mã chưa lưu được.
			if (issued != null) {
				mailer.send(issued.user(), issued.code(), properties.resetCodeTtl(), issued.language());
			}
		} catch (RuntimeException e) {
			log.error("Không xử lý được yêu cầu quên mật khẩu", e);
		}
	}

	private IssuedCode issueCode(String email) {
		User user = users.findByEmail(email).filter(found -> found.getStatus() == AccountStatus.ACTIVE).orElse(null);
		if (user == null) {
			return null;
		}

		Instant now = clock.instant();
		Optional<PasswordResetToken> latest = resetTokens.findFirstByUserIdOrderByCreatedAtDesc(user.getId());
		if (latest.isPresent() && latest.get().getCreatedAt().plus(properties.resetCodeResendCooldown()).isAfter(now)) {
			return null;
		}

		// Mã mới vô hiệu mọi mã cũ còn mở.
		resetTokens.closeOpenByUserId(user.getId(), now);

		String code = generateCode();
		PasswordResetToken token = new PasswordResetToken();
		token.setUser(user);
		token.setTokenHash(hash(user.getId(), code));
		token.setExpiresAt(now.plus(properties.resetCodeTtl()));
		resetTokens.save(token);

		UiLanguage language = settings.findByUserId(user.getId()).map(UserSetting::getLanguage).orElse(UiLanguage.VI);
		return new IssuedCode(user, code, language);
	}

	/**
	 * Đổi mật khẩu bằng mã. {@code noRollbackFor}: lần nhập sai phải được đếm ngay cả khi ném lỗi, nếu không
	 * giao dịch rollback sẽ xoá luôn bộ đếm và kẻ dò mã có vô hạn lần thử.
	 */
	@Transactional(noRollbackFor = ApiException.class)
	public void resetPassword(ResetPasswordRequest request) {
		Instant now = clock.instant();

		User user = users.findByEmail(request.email())
				.filter(found -> found.getStatus() == AccountStatus.ACTIVE)
				.orElseThrow(PasswordResetService::invalidCode);
		PasswordResetToken token = resetTokens.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId())
				.filter(open -> open.getExpiresAt().isAfter(now))
				.filter(open -> open.getAttempts() < properties.resetCodeMaxAttempts())
				.orElseThrow(PasswordResetService::invalidCode);

		if (!TokenHasher.constantTimeEquals(token.getTokenHash(), hash(user.getId(), request.code()))) {
			resetTokens.incrementAttempts(token.getId());
			throw invalidCode();
		}

		resetTokens.closeOpenByUserId(user.getId(), now);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		// Đổi mật khẩu vì nghi bị lộ thì mọi phiên đang đăng nhập phải mất hiệu lực.
		refreshTokens.revokeAll(user.getId());
	}

	private String hash(UUID userId, String code) {
		return TokenHasher.hmacSha256Hex(properties.resetCodeSecret(), userId + ":" + code);
	}

	private static String generateCode() {
		return "%06d".formatted(TokenHasher.randomInt(CODE_BOUND));
	}

	/** Mã sai, hết hạn, hết lượt thử hay email lạ đều cùng một lỗi. */
	private static ApiException invalidCode() {
		return ApiException.field(ErrorCode.INVALID_CODE, "code", "errors.invalidCode");
	}
}
