package vn.enlearning.backend.admin.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminNotificationResponse;
import vn.enlearning.backend.admin.dto.CreateNotificationRequest;
import vn.enlearning.backend.admin.service.AdminNotificationService;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

	private final AdminNotificationService notifications;

	@GetMapping
	ApiResponse<PageResponse<AdminNotificationResponse>> list(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(notifications.list(page, pageSize));
	}

	/** {@code send: true} lưu và gửi ngay, ngược lại lưu nháp. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<AdminNotificationResponse> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateNotificationRequest request) {
		return ApiResponse.ok(notifications.create(UUID.fromString(jwt.getSubject()), request));
	}

	/** 409 nếu đã gửi. */
	@PostMapping("/{id}/send")
	ApiResponse<AdminNotificationResponse> send(@PathVariable UUID id) {
		return ApiResponse.ok(notifications.send(id));
	}
}
