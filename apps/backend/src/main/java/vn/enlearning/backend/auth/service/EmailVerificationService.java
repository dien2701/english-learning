package vn.enlearning.backend.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.EmailVerificationCodeRepository;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.RateLimitedException;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.EmailVerificationCode;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.mail.EmailVerificationMailer;

/**
 * Xác minh email bằng OTP 6 số trước khi tạo tài khoản. Cùng luật với mã đặt lại mật khẩu: sống ngắn,
 * dùng một lần, giới hạn lần sai, chờ giữa hai lần gửi, DB chỉ lưu HMAC.
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

	private static final int CODE_BOUND = 1_000_000;

	private final UserRepository users;
	private final EmailVerificationCodeRepository codes;
	private final EmailVerificationMailer mailer;
	private final SecurityProperties properties;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;

	/** Gửi mã về email. Email đã có tài khoản thì báo trùng (form đăng ký vốn đã kiểm tra điều này). */
	public void sendCode(String email, String language) {
		String code = transactionTemplate.execute(status -> issueCode(email));
		UiLanguage uiLanguage = "en".equalsIgnoreCase(language) ? UiLanguage.EN : UiLanguage.VI;
		// Gửi sau khi commit: email không được đi nếu mã chưa lưu được.
		if (!mailer.send(email, code, properties.resetCodeTtl(), uiLanguage)) {
			throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}

	private String issueCode(String email) {
		if (users.existsByEmailIncludingDeleted(email)) {
			throw ApiException.field(ErrorCode.EMAIL_TAKEN, "email", "errors.emailTaken");
		}

		Instant now = clock.instant();
		Optional<EmailVerificationCode> latest = codes.findFirstByEmailOrderByCreatedAtDesc(email);
		if (latest.isPresent()) {
			Instant allowedAt = latest.get().getCreatedAt().plus(properties.resetCodeResendCooldown());
			if (allowedAt.isAfter(now)) {
				throw new RateLimitedException(Math.max(1, Duration.between(now, allowedAt).toSeconds()));
			}
		}

		codes.closeOpenByEmail(email, now);

		String code = "%06d".formatted(TokenHasher.randomInt(CODE_BOUND));
		EmailVerificationCode entity = new EmailVerificationCode();
		entity.setEmail(email);
		entity.setCodeHash(hash(email, code));
		entity.setExpiresAt(now.plus(properties.resetCodeTtl()));
		codes.save(entity);
		return code;
	}

	/**
	 * Kiểm và tiêu mã trong giao dịch đăng ký (rollback cùng nó). Riêng lần nhập sai được đếm ở giao dịch tách
	 * riêng để không bị rollback theo lỗi.
	 */
	@Transactional
	public void consume(String email, String code) {
		Instant now = clock.instant();
		EmailVerificationCode open = codes.findFirstByEmailAndUsedAtIsNullOrderByCreatedAtDesc(email)
				.filter(found -> found.getExpiresAt().isAfter(now))
				.filter(found -> found.getAttempts() < properties.resetCodeMaxAttempts())
				.orElseThrow(EmailVerificationService::invalidCode);

		if (code == null || !TokenHasher.constantTimeEquals(open.getCodeHash(), hash(email, code))) {
			TransactionTemplate separate = new TransactionTemplate(transactionTemplate.getTransactionManager());
			separate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			separate.executeWithoutResult(status -> codes.incrementAttempts(open.getId()));
			throw invalidCode();
		}
		codes.closeOpenByEmail(email, now);
	}

	private String hash(String email, String code) {
		return TokenHasher.hmacSha256Hex(properties.resetCodeSecret(), "verify:" + email + ":" + code);
	}

	private static ApiException invalidCode() {
		return ApiException.field(ErrorCode.INVALID_CODE, "code", "errors.invalidCode");
	}
}
