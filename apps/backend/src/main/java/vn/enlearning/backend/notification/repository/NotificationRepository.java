package vn.enlearning.backend.notification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import vn.enlearning.backend.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	/** Khoá dòng để hai yêu cầu gửi cùng lúc không tạo bản nhận hai lần. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select n from Notification n where n.id = :id")
	Optional<Notification> findForUpdate(@Param("id") UUID id);
}
