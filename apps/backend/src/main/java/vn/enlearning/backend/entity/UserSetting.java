package vn.enlearning.backend.entity;

import java.time.LocalTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.entity.enums.UiTheme;

/** Cài đặt cá nhân, quan hệ 1-1 với User. */
@Getter
@Setter
@Entity
@Table(name = "user_settings",
		uniqueConstraints = @UniqueConstraint(name = "uq_user_settings_user", columnNames = "user_id"),
		indexes = @Index(name = "idx_user_settings_reminder", columnList = "email_reminders, reminder_time"))
public class UserSetting extends AuditedEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UiLanguage language = UiLanguage.VI;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UiTheme theme = UiTheme.LIGHT;

	@Column(nullable = false)
	private boolean emailReminders = true;

	@Column(nullable = false)
	private LocalTime reminderTime = LocalTime.of(20, 0);

	@Column(nullable = false)
	private int dailyGoalMinutes = 30;

	/**
	 * Mã múi giờ IANA; Service kiểm tra bằng {@code ZoneId.of} trước khi lưu. Biểu đồ
	 * thời gian học gom ngày theo múi giờ này thay vì UTC.
	 */
	@Column(nullable = false, length = 50)
	private String timeZone = "Asia/Ho_Chi_Minh";
}
