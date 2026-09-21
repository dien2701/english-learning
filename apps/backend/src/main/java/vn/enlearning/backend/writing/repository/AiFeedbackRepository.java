package vn.enlearning.backend.writing.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.AiFeedback;

public interface AiFeedbackRepository extends JpaRepository<AiFeedback, UUID> {

	Optional<AiFeedback> findBySubmissionId(UUID submissionId);

	List<AiFeedback> findBySubmissionIdIn(Collection<UUID> submissionIds);

	/** Điểm tổng các bài đã chấm của người dùng, mới nhất trước; người gọi lấy dòng đầu tiên của mỗi {@code parentId}. */
	interface PromptScore {
		UUID getParentId();

		BigDecimal getScore();
	}

	@Query("select f.submission.prompt.id as parentId, f.overallScore as score from AiFeedback f "
			+ "where f.submission.user.id = :userId and f.submission.prompt.id in :ids "
			+ "order by f.submission.submittedAt desc, f.id desc")
	List<PromptScore> promptScores(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);
}
