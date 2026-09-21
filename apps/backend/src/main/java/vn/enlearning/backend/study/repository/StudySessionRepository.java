package vn.enlearning.backend.study.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.StudySession;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

	/** Một phiên học rút gọn cho biểu đồ; gom theo ngày ở Service vì múi giờ do người dùng chọn. */
	interface SessionSlice {
		Instant getStartedAt();

		int getActiveSeconds();
	}

	/** Phiên có {@code startedAt} trong nửa khoảng [from, to). */
	@Query("select s.startedAt as startedAt, s.activeSeconds as activeSeconds from StudySession s "
			+ "where s.user.id = :userId and s.startedAt >= :from and s.startedAt < :to")
	List<SessionSlice> findSlices(UUID userId, Instant from, Instant to);

	boolean existsByUserId(UUID userId);

	Optional<StudySession> findFirstByUserIdOrderByLastHeartbeatAtDesc(UUID userId);

	@Query("select coalesce(sum(s.activeSeconds), 0) from StudySession s where s.user.id = :userId")
	long totalActiveSeconds(UUID userId);

	long countByStartedAtGreaterThanEqual(Instant since);
}
