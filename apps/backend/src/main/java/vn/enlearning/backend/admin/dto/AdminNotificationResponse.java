package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.entity.enums.NotificationAudience;
import vn.enlearning.backend.entity.enums.NotificationStatus;

/** {@code recipientCount} và {@code sentAt} chỉ có khi đã gửi. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminNotificationResponse(UUID id, String title, String content, NotificationAudience audience,
		NotificationStatus status, Integer recipientCount, Instant createdAt, Instant sentAt) {
}
