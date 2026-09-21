package vn.enlearning.backend.study.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.StudySession;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

	Optional<StudySession> findFirstByUserIdOrderByLastHeartbeatAtDesc(UUID userId);

	@Query("select coalesce(sum(s.activeSeconds), 0) from StudySession s where s.user.id = :userId")
	long totalActiveSeconds(UUID userId);
}
