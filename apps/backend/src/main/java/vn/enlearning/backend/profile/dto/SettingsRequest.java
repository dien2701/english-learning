package vn.enlearning.backend.profile.dto;

/** Mọi trường tuỳ chọn; null là giữ nguyên. {@code reminderTime} dạng HH:mm, {@code timeZone} là mã IANA. */
public record SettingsRequest(
		String language,
		String theme,
		Boolean emailReminders,
		String reminderTime,
		Integer dailyGoalMinutes,
		String timeZone) {
}
