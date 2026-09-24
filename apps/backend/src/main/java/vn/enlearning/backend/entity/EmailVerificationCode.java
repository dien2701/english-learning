package vn.enlearning.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Mã OTP 6 số xác minh email khi đăng ký, dùng một lần. Chưa có tài khoản nên gắn với email; chỉ lưu
 * HMAC-SHA256 (khoá {@code RESET_CODE_SECRET}, băm kèm email).
 */
@Getter
@Setter
@Entity
@Table(name = "email_verification_codes", indexes = {
		@Index(name = "idx_email_verification_codes_email", columnList = "email, used_at"),
		@Index(name = "idx_email_verification_codes_expires_at", columnList = "expires_at")
})
public class EmailVerificationCode extends BaseEntity {

	@Column(nullable = false)
	private String email;

	@Column(nullable = false, length = 64)
	private String codeHash;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant usedAt;

	/** Số lần nhập sai. Tăng bằng câu UPDATE nguyên tử, không sửa qua setter. */
	@Column(nullable = false)
	private int attempts;
}
