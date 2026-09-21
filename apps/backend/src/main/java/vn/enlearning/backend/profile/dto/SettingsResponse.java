package vn.enlearning.backend.profile.dto;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import vn.enlearning.backend.entity.UserSetting;

/** Khớp {@code UserSettings} ở frontend: language/theme viết thường, giờ nhắc dạng HH:mm. */
public record SettingsResponse(
		String language,
		String theme,
		boolean emailReminders,
		String reminderTime,
		int dailyGoalMinutes,
		String timeZone) {

	private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

	public static SettingsResponse from(UserSetting s) {
		return new SettingsResponse(s.getLanguage().name().toLowerCase(Locale.ROOT),
				s.getTheme().name().toLowerCase(Locale.ROOT), s.isEmailReminders(),
				s.getReminderTime().format(HH_MM), s.getDailyGoalMinutes(), s.getTimeZone());
	}
}
