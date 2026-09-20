package vn.enlearning.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.EmailType;

/**
 * Nhật ký email. Giữ lại sau khi xoá người dùng ({@code user} về NULL) để
 * đối soát. {@code reminderDate} chỉ đặt cho email nhắc học; ràng buộc
 * UNIQUE (user_id, reminder_date) chặn gửi trùng trong cùng ngày.
 */
@Getter
@Setter
@Entity
@Table(name = "email_logs",
		uniqueConstraints = @UniqueConstraint(name = "uq_email_logs_daily_reminder", columnNames = { "user_id", "reminder_date" }),
		indexes = {
				@Index(name = "idx_email_logs_status", columnList = "status, created_at"),
				@Index(name = "idx_email_logs_user_type", columnList = "user_id, type, created_at")
		})
public class EmailLog extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailType type;

	@Column(nullable = false)
	private String recipientEmail;

	@Column(nullable = false)
	private String subject;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailStatus status = EmailStatus.PENDING;

	@Column(length = 500)
	private String errorMessage;

	private LocalDate reminderDate;

	private Instant sentAt;
}
