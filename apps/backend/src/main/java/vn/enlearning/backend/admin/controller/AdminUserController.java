package vn.enlearning.backend.admin.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminUserResponse;
import vn.enlearning.backend.admin.dto.UpdateUserRequest;
import vn.enlearning.backend.admin.service.AdminUserService;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;

/** Chỉ ROLE_ADMIN (khoá ở {@code SecurityConfig}: {@code /admin/**}). */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService users;

	@GetMapping
	ApiResponse<PageResponse<AdminUserResponse>> list(@RequestParam(required = false) String search,
			@RequestParam(required = false) String role, @RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(users.list(search, role, status, page, pageSize));
	}

	@GetMapping("/{id}")
	ApiResponse<AdminUserResponse> detail(@PathVariable UUID id) {
		return ApiResponse.ok(users.get(id));
	}

	/** Khoá/mở khoá ({@code status}) và đổi vai trò ({@code role}); không áp dụng cho chính mình. */
	@PatchMapping("/{id}")
	ApiResponse<AdminUserResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request) {
		return ApiResponse.ok(users.update(UUID.fromString(jwt.getSubject()), id, request));
	}
}
