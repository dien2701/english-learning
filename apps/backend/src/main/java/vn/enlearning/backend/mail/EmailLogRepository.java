package vn.enlearning.backend.mail;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.EmailLog;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {

	/** Đã có dòng nhắc học của người dùng cho ngày (theo múi giờ người dùng) chưa. */
	boolean existsByUserIdAndReminderDate(UUID userId, LocalDate reminderDate);
}
