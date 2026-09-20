package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Bản nhận của một người dùng cho một thông báo, kèm trạng thái đã đọc. */
@Getter
@Setter
@Entity
@Table(name = "user_notifications",
		uniqueConstraints = @UniqueConstraint(name = "uq_user_notifications", columnNames = { "notification_id", "user_id" }),
		indexes = @Index(name = "idx_user_notifications_inbox", columnList = "user_id, read_at, created_at"))
public class UserNotification extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "notification_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Notification notification;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/** Trống nghĩa là chưa đọc. */
	private Instant readAt;
}
