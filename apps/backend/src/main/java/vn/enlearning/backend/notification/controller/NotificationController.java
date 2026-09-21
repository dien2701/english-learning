package vn.enlearning.backend.notification.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.notification.dto.NotificationResponse;
import vn.enlearning.backend.notification.service.NotificationService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notifications;

	@GetMapping
	ApiResponse<PageResponse<NotificationResponse>> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(notifications.list(userId(jwt), page, pageSize));
	}

	@GetMapping("/unread-count")
	ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(Map.of("count", notifications.unreadCount(userId(jwt))));
	}

	@PatchMapping("/{id}/read")
	ApiResponse<NotificationResponse> markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(notifications.markRead(userId(jwt), id));
	}

	@PostMapping("/read-all")
	ApiResponse<Map<String, Integer>> markAllRead(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(Map.of("count", notifications.markAllRead(userId(jwt))));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
