package vn.enlearning.backend.notification.service;

import java.time.Clock;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.entity.Notification;
import vn.enlearning.backend.entity.UserNotification;
import vn.enlearning.backend.notification.dto.NotificationResponse;
import vn.enlearning.backend.notification.repository.UserNotificationRepository;

/** Hộp thư thông báo của người học: mọi truy vấn đều ràng theo {@code userId}. */
@Service
@RequiredArgsConstructor
public class NotificationService {

	private static final String ICON = "campaign";
	private static final String PATH = "/notifications";

	private final UserNotificationRepository inbox;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<NotificationResponse> list(UUID userId, int page, int pageSize) {
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);
		var found = inbox.findByUserId(userId,
				PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
		return new PageResponse<>(found.map(NotificationService::toResponse).getContent(), current, size,
				found.getTotalElements(), found.getTotalPages());
	}

	@Transactional(readOnly = true)
	public long unreadCount(UUID userId) {
		return inbox.countByUserIdAndReadAtIsNull(userId);
	}

	@Transactional
	public NotificationResponse markRead(UUID userId, UUID id) {
		UserNotification item = inbox.findByIdAndUserId(id, userId)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		if (item.getReadAt() == null) {
			item.setReadAt(clock.instant());
		}
		return toResponse(item);
	}

	/** Số thông báo vừa được đánh dấu. */
	@Transactional
	public int markAllRead(UUID userId) {
		return inbox.markAllRead(userId, clock.instant());
	}

	private static NotificationResponse toResponse(UserNotification item) {
		Notification n = item.getNotification();
		return new NotificationResponse(item.getId(), ICON, L10n.of(n.getTitle(), null), L10n.of(n.getContent(), null),
				PATH, item.getCreatedAt(), item.getReadAt() != null);
	}
}
