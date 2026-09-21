package vn.enlearning.backend.profile.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.auth.dto.UserResponse;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;

/** Khớp {@code ProfileData} ở frontend: các trường của {@code User} cộng ba số thống kê. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(
		UUID id,
		String email,
		String fullName,
		String phoneNumber,
		String avatarUrl,
		Role role,
		AccountStatus status,
		Instant createdAt,
		Instant joinedAt,
		long totalMinutes,
		long completedLessons,
		long masteredWords) {

	public static ProfileResponse of(UserResponse u, long totalMinutes, long completedLessons, long masteredWords) {
		return new ProfileResponse(u.id(), u.email(), u.fullName(), u.phoneNumber(), u.avatarUrl(), u.role(),
				u.status(), u.createdAt(), u.createdAt(), totalMinutes, completedLessons, masteredWords);
	}
}
