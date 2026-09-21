package vn.enlearning.backend.admin.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminNotificationResponse;
import vn.enlearning.backend.admin.dto.CreateNotificationRequest;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.entity.Notification;
import vn.enlearning.backend.entity.UserNotification;
import vn.enlearning.backend.entity.enums.NotificationAudience;
import vn.enlearning.backend.entity.enums.NotificationStatus;
import vn.enlearning.backend.notification.repository.NotificationRepository;
import vn.enlearning.backend.notification.repository.UserNotificationRepository;

/**
 * Soạn và gửi thông báo. Gửi = chụp danh sách người nhận tại thời điểm bấm gửi và tạo mỗi người một
 * {@code UserNotification}. Nhóm nhận (chỉ tính tài khoản ACTIVE): ALL là mọi người, ACTIVE là người có
 * {@code lastActiveAt} trong 30 ngày qua, INACTIVE là ACTIVE còn lại (kể cả chưa từng hoạt động), ADMIN là quản trị viên.
 * Đã gửi thì không sửa, không gửi lại.
 */
@Service
@RequiredArgsConstructor
public class AdminNotificationService {

	static final Duration ACTIVE_WINDOW = Duration.ofDays(30);

	private final NotificationRepository notifications;
	private final UserNotificationRepository inbox;
	private final UserRepository users;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<AdminNotificationResponse> list(int page, int pageSize) {
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);
		Page<Notification> found = notifications.findAll(
				PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
		return new PageResponse<>(found.map(AdminNotificationService::toResponse).getContent(), current, size,
				found.getTotalElements(), found.getTotalPages());
	}

	@Transactional
	public AdminNotificationResponse create(UUID adminId, CreateNotificationRequest request) {
		Notification notification = new Notification();
		notification.setTitle(request.title().trim());
		notification.setContent(request.content().trim());
		notification.setAudience(request.audience());
		notification.setCreatedBy(users.getReferenceById(adminId));
		notifications.saveAndFlush(notification);
		if (Boolean.TRUE.equals(request.send())) {
			deliver(notification);
		}
		return toResponse(notification);
	}

	@Transactional
	public AdminNotificationResponse send(UUID id) {
		Notification notification = notifications.findForUpdate(id)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		deliver(notification);
		return toResponse(notification);
	}

	private void deliver(Notification notification) {
		if (notification.getStatus() == NotificationStatus.SENT) {
			throw new ApiException(ErrorCode.INVALID_STATE);
		}
		Instant now = clock.instant();
		List<UUID> recipients = recipients(notification.getAudience(), now);
		inbox.saveAll(recipients.stream().map(userId -> {
			UserNotification item = new UserNotification();
			item.setNotification(notification);
			item.setUser(users.getReferenceById(userId));
			return item;
		}).toList());
		notification.setStatus(NotificationStatus.SENT);
		notification.setRecipientCount(recipients.size());
		notification.setSentAt(now);
	}

	private List<UUID> recipients(NotificationAudience audience, Instant now) {
		Instant since = now.minus(ACTIVE_WINDOW);
		return switch (audience) {
			case ALL -> users.findActiveIds();
			case ACTIVE -> users.findRecentlyActiveIds(since);
			case INACTIVE -> users.findDormantIds(since);
			case ADMIN -> users.findActiveAdminIds();
		};
	}

	private static AdminNotificationResponse toResponse(Notification n) {
		return new AdminNotificationResponse(n.getId(), n.getTitle(), n.getContent(), n.getAudience(), n.getStatus(),
				n.getRecipientCount(), n.getCreatedAt(), n.getSentAt());
	}
}
