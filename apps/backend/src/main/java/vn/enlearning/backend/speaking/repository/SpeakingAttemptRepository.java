package vn.enlearning.backend.speaking.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;

public interface SpeakingAttemptRepository extends JpaRepository<SpeakingAttempt, UUID> {

	/** Điểm các lượt đã chấm, mới nhất trước; người gọi lấy dòng đầu tiên của mỗi {@code parentId}. */
	interface LessonScore {
		UUID getParentId();

		BigDecimal getScore();
	}

	@Query("select a.lesson.id as parentId, a.overallScore as score from SpeakingAttempt a "
			+ "where a.user.id = :userId and a.status = vn.enlearning.backend.entity.enums.SpeakingAttemptStatus.GRADED "
			+ "and a.lesson.id in :ids order by a.submittedAt desc, a.id desc")
	List<LessonScore> gradedScores(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

	/** Chỉ trả lượt của chính người dùng; lượt của người khác coi như không tồn tại. */
	@EntityGraph(attributePaths = "lesson")
	Optional<SpeakingAttempt> findByIdAndUserId(UUID id, UUID userId);

	/** Đổi trạng thái có điều kiện, nguyên tử; xoá persistence context để lần đọc kế tiếp thấy trạng thái mới. */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update SpeakingAttempt a set a.status = :to, a.updatedAt = :now where a.id = :id and a.status = :from")
	int transition(@Param("id") UUID id, @Param("from") SpeakingAttemptStatus from,
			@Param("to") SpeakingAttemptStatus to, @Param("now") Instant now);

	/** Lượt đang thu của người dùng ở bài này, mới nhất trước; dùng lại thay vì tạo lượt mới. */
	Optional<SpeakingAttempt> findFirstByUserIdAndLessonIdAndStatusOrderByCreatedAtDescIdDesc(UUID userId,
			UUID lessonId, SpeakingAttemptStatus status);

	/** Làm mới {@code updatedAt} để job dọn dẹp không xoá lượt vẫn đang được dùng. */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update SpeakingAttempt a set a.updatedAt = :now where a.id = :id")
	int touch(@Param("id") UUID id, @Param("now") Instant now);

	/** Lượt IN_PROGRESS bị bỏ dở: dòng kết quả từng câu bị xoá theo khoá ngoại ON DELETE CASCADE. */
	@Modifying
	@Query("delete from SpeakingAttempt a where a.status = vn.enlearning.backend.entity.enums.SpeakingAttemptStatus.IN_PROGRESS "
			+ "and a.updatedAt < :cutoff")
	int deleteStaleInProgress(@Param("cutoff") Instant cutoff);

	@Query("select distinct a.lesson.id from SpeakingAttempt a where a.lesson.id in :ids")
	List<UUID> usedLessonIds(@Param("ids") Collection<UUID> ids);

	@Query("select a.user.id as id, count(a) as total from SpeakingAttempt a "
			+ "where a.status = vn.enlearning.backend.entity.enums.SpeakingAttemptStatus.GRADED and a.user.id in :ids "
			+ "group by a.user.id")
	List<vn.enlearning.backend.common.IdCount> gradedByUsers(@Param("ids") Collection<UUID> ids);
}
