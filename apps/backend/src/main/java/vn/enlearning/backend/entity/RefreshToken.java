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

/** Refresh token của phiên đăng nhập; chỉ lưu bản băm SHA-256. */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens",
		uniqueConstraints = @UniqueConstraint(name = "uq_refresh_tokens_hash", columnNames = "token_hash"),
		indexes = {
				@Index(name = "idx_refresh_tokens_user", columnList = "user_id, revoked_at"),
				@Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
		})
public class RefreshToken extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@Column(nullable = false, length = 64)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant revokedAt;

	private String userAgent;

	@Column(length = 45)
	private String ipAddress;
}
