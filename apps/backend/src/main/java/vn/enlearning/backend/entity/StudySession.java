package vn.enlearning.backend.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Một phiên học liên tục, nguồn số liệu của biểu đồ thời gian học ở Dashboard.
 * Frontend gửi heartbeat khoảng 30 giây một lần khi tab đang mở; backend cộng vào
 * {@code activeSeconds} nhưng chỉ tính tối đa 60 giây cho mỗi heartbeat, và nếu im
 * lặng quá 2 phút thì phiên kết thúc, heartbeat sau đó mở phiên mới. Thời gian của
 * phiên tính vào ngày của {@code startedAt}; khi gom theo ngày phải đổi sang múi giờ
 * ứng dụng (Asia/Ho_Chi_Minh), không gom theo ngày UTC.
 */
@Getter
@Setter
@Entity
@Table(name = "study_sessions", indexes = {
		@Index(name = "idx_study_sessions_user_started", columnList = "user_id, started_at"),
		@Index(name = "idx_study_sessions_user_heartbeat", columnList = "user_id, last_heartbeat_at")
})
public class StudySession extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StudySkill skill;

	/**
	 * Bộ thẻ, đề viết, bài nghe/đọc/nói hoặc đề kiểm tra tuỳ theo {@code skill}
	 * (CHAT: hội thoại hoặc NULL). Không có FK vì trỏ tới nhiều bảng.
	 */
	private UUID refId;

	@Column(nullable = false)
	private Instant startedAt;

	@Column(nullable = false)
	private Instant lastHeartbeatAt;

	@Column(nullable = false)
	private int activeSeconds;
}
