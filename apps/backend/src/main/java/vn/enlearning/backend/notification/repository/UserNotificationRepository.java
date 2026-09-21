package vn.enlearning.backend.notification.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {

	@EntityGraph(attributePaths = "notification")
	Page<UserNotification> findByUserId(UUID userId, Pageable pageable);

	/** Chỉ trả bản nhận của chính người dùng; của người khác coi như không tồn tại. */
	@EntityGraph(attributePaths = "notification")
	Optional<UserNotification> findByIdAndUserId(UUID id, UUID userId);

	long countByUserIdAndReadAtIsNull(UUID userId);

	long countByNotificationId(UUID notificationId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update UserNotification un set un.readAt = :now where un.user.id = :userId and un.readAt is null")
	int markAllRead(@Param("userId") UUID userId, @Param("now") Instant now);
}
