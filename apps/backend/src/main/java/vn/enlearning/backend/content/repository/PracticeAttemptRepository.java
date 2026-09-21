package vn.enlearning.backend.content.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.enums.AttemptStatus;

public interface PracticeAttemptRepository extends JpaRepository<PracticeAttempt, UUID> {

	long countByUserIdAndStatus(UUID userId, AttemptStatus status);

	/** Điểm các lượt đã nộp, mới nhất trước; người gọi lấy dòng đầu tiên của mỗi {@code parentId}. */
	interface ParentScore {
		UUID getParentId();

		BigDecimal getScore();
	}

	String COMPLETED = "vn.enlearning.backend.entity.enums.AttemptStatus.COMPLETED";

	@Query("select a.listeningLesson.id as parentId, a.score as score from PracticeAttempt a "
			+ "where a.user.id = :userId and a.status = " + COMPLETED + " and a.listeningLesson.id in :ids "
			+ "order by a.submittedAt desc, a.id desc")
	List<ParentScore> listeningScores(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

	@Query("select a.readingLesson.id as parentId, a.score as score from PracticeAttempt a "
			+ "where a.user.id = :userId and a.status = " + COMPLETED + " and a.readingLesson.id in :ids "
			+ "order by a.submittedAt desc, a.id desc")
	List<ParentScore> readingScores(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

	@Query("select a.exam.id as parentId, a.score as score from PracticeAttempt a "
			+ "where a.user.id = :userId and a.status = " + COMPLETED + " and a.exam.id in :ids "
			+ "order by a.submittedAt desc, a.id desc")
	List<ParentScore> examScores(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

	/** Chỉ trả lượt của chính người dùng; lượt của người khác coi như không tồn tại. */
	@EntityGraph(attributePaths = { "listeningLesson", "readingLesson", "exam" })
	Optional<PracticeAttempt> findByIdAndUserId(UUID id, UUID userId);

	@EntityGraph(attributePaths = { "listeningLesson", "readingLesson", "exam" })
	Page<PracticeAttempt> findByUserIdAndStatus(UUID userId, AttemptStatus status, Pageable pageable);

	@EntityGraph(attributePaths = { "listeningLesson", "readingLesson", "exam" })
	Page<PracticeAttempt> findByUserIdAndStatusAndListeningLessonIsNotNull(UUID userId, AttemptStatus status,
			Pageable pageable);

	@EntityGraph(attributePaths = { "listeningLesson", "readingLesson", "exam" })
	Page<PracticeAttempt> findByUserIdAndStatusAndReadingLessonIsNotNull(UUID userId, AttemptStatus status,
			Pageable pageable);

	@EntityGraph(attributePaths = { "listeningLesson", "readingLesson", "exam" })
	Page<PracticeAttempt> findByUserIdAndStatusAndExamIsNotNull(UUID userId, AttemptStatus status,
			Pageable pageable);
}
