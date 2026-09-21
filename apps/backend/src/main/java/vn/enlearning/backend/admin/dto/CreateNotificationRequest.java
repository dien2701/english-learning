package vn.enlearning.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.entity.enums.NotificationAudience;

/** {@code send} true: lưu và gửi ngay; vắng hoặc false: lưu nháp. */
public record CreateNotificationRequest(
		@NotBlank(message = "errors.field.titleRequired") @Size(max = 200, message = "errors.field.titleTooLong") String title,
		@NotBlank(message = "errors.field.contentRequired") @Size(max = 5000, message = "errors.field.textTooLong") String content,
		@NotNull(message = "errors.field.audienceRequired") NotificationAudience audience,
		Boolean send) {
}
