package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Token đặt lại mật khẩu dùng một lần; chỉ lưu bản băm SHA-256. */
@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens",
		uniqueConstraints = @UniqueConstraint(name = "uq_password_reset_tokens_hash", columnNames = "token_hash"),
		indexes = {
				@Index(name = "idx_password_reset_tokens_user", columnList = "user_id, used_at"),
				@Index(name = "idx_password_reset_tokens_expires_at", columnList = "expires_at")
		})
public class PasswordResetToken extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@Column(nullable = false, length = 64)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant usedAt;
}
