package vn.enlearning.backend.mail;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.EmailLog;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
