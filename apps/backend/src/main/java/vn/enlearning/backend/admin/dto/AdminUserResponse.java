package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;

/** Người dùng trong bảng quản lý, khớp {@code AdminUser} ở frontend. Không có {@code passwordHash}. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminUserResponse(UUID id, String fullName, String email, Role role, AccountStatus status,
		Instant createdAt, Instant lastActiveAt, long completedLessons) {
}
