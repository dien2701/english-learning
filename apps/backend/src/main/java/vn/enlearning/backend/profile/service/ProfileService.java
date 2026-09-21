package vn.enlearning.backend.profile.service;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.dto.Emails;
import vn.enlearning.backend.auth.dto.UserResponse;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.auth.service.ClientInfo;
import vn.enlearning.backend.auth.service.RefreshTokenService;
import vn.enlearning.backend.auth.validation.PasswordPolicy;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.entity.enums.UiLanguage;
import vn.enlearning.backend.entity.enums.UiTheme;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.profile.dto.ChangePasswordRequest;
import vn.enlearning.backend.profile.dto.ProfileResponse;
import vn.enlearning.backend.profile.dto.SettingsRequest;
import vn.enlearning.backend.profile.dto.SettingsResponse;
import vn.enlearning.backend.profile.dto.UpdateProfileRequest;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/** Hồ sơ, đổi mật khẩu và cài đặt cá nhân của người đang đăng nhập. */
@Service
@RequiredArgsConstructor
public class ProfileService {

	private static final int MAX_NAME = 100;
	private static final int MAX_PHONE = 20;
	private static final int MAX_AVATAR = 500;
	private static final int MIN_GOAL_MINUTES = 5;
	private static final int MAX_GOAL_MINUTES = 600;
	private static final String BAD_REQUEST_KEY = "errors.badRequest";

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final StudySessionRepository studySessions;
	private final PracticeAttemptRepository attempts;
	private final UserFlashcardProgressRepository flashcardProgress;
	private final RefreshTokenService refreshTokens;
	private final PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public ProfileResponse get(UUID userId) {
		User user = activeUser(userId);
		return ProfileResponse.of(UserResponse.from(user),
				studySessions.totalActiveSeconds(userId) / 60,
				attempts.countByUserIdAndStatus(userId, AttemptStatus.COMPLETED),
				flashcardProgress.countByUserIdAndRecallLevel(userId, RecallLevel.REMEMBERED));
	}

	@Transactional
	public UserResponse update(UUID userId, UpdateProfileRequest request) {
		User user = activeUser(userId);

		if (request.fullName() != null) {
			String name = request.fullName().trim();
			if (name.isEmpty()) {
				throw ApiException.field(ErrorCode.VALIDATION, "fullName", "auth.validation.nameRequired");
			}
			if (name.length() > MAX_NAME) {
				throw ApiException.field(ErrorCode.VALIDATION, "fullName", BAD_REQUEST_KEY);
			}
			user.setFullName(name);
		}
		if (request.email() != null) {
			String email = Emails.normalize(request.email());
			if (!Emails.isValid(email)) {
				throw ApiException.field(ErrorCode.VALIDATION, "email", "auth.validation.emailFormat");
			}
			if (!email.equals(user.getEmail())) {
				if (users.existsByEmailIncludingDeleted(email)) {
					throw emailTaken();
				}
				user.setEmail(email);
			}
		}
		if (request.phoneNumber() != null) {
			user.setPhoneNumber(blankToNull(request.phoneNumber(), "phoneNumber", MAX_PHONE));
		}
		if (request.avatarUrl() != null) {
			user.setAvatarUrl(blankToNull(request.avatarUrl(), "avatarUrl", MAX_AVATAR));
		}

		try {
			users.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			// Hai request cùng đổi sang một email: unique index chặn request đến sau.
			throw emailTaken();
		}
		return UserResponse.from(user);
	}

	/**
	 * Đổi mật khẩu rồi thu hồi mọi refresh token cũ, kể cả của thiết bị đang gọi (cookie refresh chỉ gửi kèm
	 * {@code /auth/*} nên không nhận ra phiên hiện tại). Trả về refresh token mới cho chính thiết bị này.
	 */
	@Transactional
	public String changePassword(UUID userId, ChangePasswordRequest request, ClientInfo client) {
		User user = activeUser(userId);
		boolean matches = !PasswordPolicy.exceedsBcryptLimit(request.currentPassword())
				&& passwordEncoder.matches(request.currentPassword(), user.getPasswordHash());
		if (!matches) {
			throw ApiException.field(ErrorCode.WRONG_PASSWORD, "currentPassword", "errors.wrongPassword");
		}
		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		users.saveAndFlush(user);
		refreshTokens.revokeAll(userId);
		return refreshTokens.issue(user, client);
	}

	@Transactional
	public SettingsResponse getSettings(UUID userId) {
		return SettingsResponse.from(settingsOf(userId));
	}

	@Transactional
	public SettingsResponse updateSettings(UUID userId, SettingsRequest request) {
		UserSetting setting = settingsOf(userId);

		if (request.language() != null) {
			setting.setLanguage(parseEnum(UiLanguage.class, request.language(), "language"));
		}
		if (request.theme() != null) {
			setting.setTheme(parseEnum(UiTheme.class, request.theme(), "theme"));
		}
		if (request.emailReminders() != null) {
			setting.setEmailReminders(request.emailReminders());
		}
		if (request.reminderTime() != null) {
			try {
				setting.setReminderTime(LocalTime.parse(request.reminderTime().trim()));
			} catch (DateTimeParseException e) {
				throw ApiException.field(ErrorCode.VALIDATION, "reminderTime", BAD_REQUEST_KEY);
			}
		}
		if (request.dailyGoalMinutes() != null) {
			int goal = request.dailyGoalMinutes();
			if (goal < MIN_GOAL_MINUTES || goal > MAX_GOAL_MINUTES) {
				throw ApiException.field(ErrorCode.VALIDATION, "dailyGoalMinutes", BAD_REQUEST_KEY);
			}
			setting.setDailyGoalMinutes(goal);
		}
		if (request.timeZone() != null) {
			try {
				setting.setTimeZone(ZoneId.of(request.timeZone().trim()).getId());
			} catch (DateTimeException e) {
				throw ApiException.field(ErrorCode.VALIDATION, "timeZone", BAD_REQUEST_KEY);
			}
		}
		return SettingsResponse.from(settings.saveAndFlush(setting));
	}

	// --- nội bộ ---------------------------------------------------------------------------------

	private User activeUser(UUID userId) {
		return users.findById(userId)
				.filter(found -> !found.isDeleted())
				.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
	}

	/** Tài khoản tạo bằng đường khác (seed cũ) có thể chưa có dòng cài đặt: tạo với giá trị mặc định. */
	private UserSetting settingsOf(UUID userId) {
		return settings.findByUserId(userId).orElseGet(() -> {
			UserSetting created = new UserSetting();
			created.setUser(users.getReferenceById(userId));
			return settings.saveAndFlush(created);
		});
	}

	private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String field) {
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw ApiException.field(ErrorCode.VALIDATION, field, BAD_REQUEST_KEY);
		}
	}

	private static String blankToNull(String value, String field, int max) {
		String trimmed = value.trim();
		if (trimmed.length() > max) {
			throw ApiException.field(ErrorCode.VALIDATION, field, BAD_REQUEST_KEY);
		}
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static ApiException emailTaken() {
		return ApiException.field(ErrorCode.EMAIL_TAKEN, "email", "errors.emailTaken");
	}
}
