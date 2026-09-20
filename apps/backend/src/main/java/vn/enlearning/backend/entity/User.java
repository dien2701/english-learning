package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;

/** Tài khoản người học và quản trị viên. Xoá mềm để giữ nguyên lịch sử. */
@Getter
@Setter
@Entity
@Table(name = "users",
		uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
		indexes = {
				@Index(name = "idx_users_role_status", columnList = "role, status"),
				@Index(name = "idx_users_full_name", columnList = "full_name"),
				@Index(name = "idx_users_created_at", columnList = "created_at"),
				@Index(name = "idx_users_last_active_at", columnList = "last_active_at")
		})
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends SoftDeletableEntity {

	@Column(nullable = false)
	private String email;

	/** Băm BCrypt cost 12. Tuyệt đối không đưa trường này vào DTO trả về. */
	@Column(nullable = false, length = 100)
	private String passwordHash;

	@Column(nullable = false, length = 100)
	private String fullName;

	@Column(length = 20)
	private String phoneNumber;

	@Column(length = 500)
	private String avatarUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role = Role.USER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountStatus status = AccountStatus.ACTIVE;

	private Instant lastActiveAt;
}
