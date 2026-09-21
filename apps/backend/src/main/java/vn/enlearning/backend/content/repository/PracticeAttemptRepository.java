package vn.enlearning.backend.content.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.enums.AttemptStatus;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, UUID> {

	long countByUserIdAndStatus(UUID userId, AttemptStatus status);
}
