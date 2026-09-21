package vn.enlearning.backend.content.repository;

import java.math.BigDecimal;
import java.time.Instant;
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

	/** Số lượt đã chấm điểm và điểm trung bình (thang 10) trong một khoảng; {@code average} là null nếu không có lượt nào. */
	interface SkillAverage {
		long getAttempts();

		Double getAverage();
	}

	String AVERAGE = "select count(a) as attempts, avg(a.score) as average from PracticeAttempt a "
			+ "where a.user.id = :userId and a.status = " + COMPLETED + " and a.score is not null "
			+ "and a.submittedAt >= :from and a.submittedAt < :to and ";

	/** Ba truy vấn cùng dạng, khác cột cha: tránh GROUP BY trên biểu thức CASE (MySQL ONLY_FULL_GROUP_BY). */
	@Query(AVERAGE + "a.listeningLesson is not null")
	SkillAverage listeningAverage(@Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

	@Query(AVERAGE + "a.readingLesson is not null")
	SkillAverage readingAverage(@Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

	@Query(AVERAGE + "a.exam is not null")
	SkillAverage examAverage(@Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

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
