package vn.enlearning.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;

/**
 * Thông tin người dùng trả cho frontend, khớp kiểu {@code User} ở {@code types/common.ts}.
 * Liệt kê từng trường thay vì loại bớt khỏi entity: thêm cột nhạy cảm vào {@code User} sau này
 * cũng không thể lọt ra ngoài, và {@code passwordHash} không bao giờ có mặt ở đây.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
		UUID id,
		String email,
		String fullName,
		String phoneNumber,
		String avatarUrl,
		Role role,
		AccountStatus status,
		Instant createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getFullName(),
				user.getPhoneNumber(),
				user.getAvatarUrl(),
				user.getRole(),
				user.getStatus(),
				user.getCreatedAt());
	}
}
