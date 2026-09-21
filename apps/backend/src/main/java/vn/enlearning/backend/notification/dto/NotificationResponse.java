package vn.enlearning.backend.notification.dto;

import java.time.Instant;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;

/**
 * Thông báo trong hộp thư của người học, khớp {@code UserNotification} ở frontend. {@code id} là id bản nhận
 * (không phải id thông báo gốc). Admin soạn một ngôn ngữ nên {@code title}/{@code body} lặp lại cho cả VI và EN.
 */
public record NotificationResponse(UUID id, String icon, L10n title, L10n body, String path, Instant createdAt,
		boolean isRead) {
}
