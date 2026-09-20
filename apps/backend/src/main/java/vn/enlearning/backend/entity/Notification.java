package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.NotificationAudience;
import vn.enlearning.backend.entity.enums.NotificationStatus;

/** Thông báo Admin soạn và gửi tới một nhóm người nhận. */
@Getter
@Setter
@Entity
@Table(name = "notifications", indexes = {
		@Index(name = "idx_notifications_status_sent", columnList = "status, sent_at"),
		@Index(name = "idx_notifications_audience", columnList = "audience"),
		@Index(name = "idx_notifications_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE notifications SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Notification extends SoftDeletableEntity {

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationAudience audience = NotificationAudience.ALL;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationStatus status = NotificationStatus.DRAFT;

	/** Chỉ có khi đã gửi. */
	private Integer recipientCount;

	private Instant sentAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
}
