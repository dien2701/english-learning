package vn.enlearning.backend.writing.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.SubmissionStatus;

public interface WritingSubmissionRepository extends JpaRepository<WritingSubmission, UUID> {

	/** Trạng thái các bài nộp của người dùng, mới nhất trước; người gọi lấy dòng đầu tiên của mỗi {@code parentId}. */
	interface PromptStatus {
		UUID getParentId();

		SubmissionStatus getStatus();
	}

	@Query("select s.prompt.id as parentId, s.status as status from WritingSubmission s "
			+ "where s.user.id = :userId and s.prompt.id in :ids order by s.createdAt desc, s.id desc")
	List<PromptStatus> promptStatuses(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

	/** Chỉ trả bài của chính người dùng; bài của người khác coi như không tồn tại. */
	@EntityGraph(attributePaths = "prompt")
	Optional<WritingSubmission> findByIdAndUserId(UUID id, UUID userId);

	@EntityGraph(attributePaths = "prompt")
	Page<WritingSubmission> findByUserIdAndSubmittedAtIsNotNull(UUID userId, Pageable pageable);

	/**
	 * Đổi trạng thái có điều kiện, nguyên tử: hai yêu cầu chấm lại cùng lúc thì chỉ một yêu cầu thắng ({@code 1}).
	 * Xoá persistence context để lần đọc kế tiếp thấy trạng thái mới.
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update WritingSubmission s set s.status = :to, s.updatedAt = :now where s.id = :id and s.status = :from")
	int transition(@Param("id") UUID id, @Param("from") SubmissionStatus from, @Param("to") SubmissionStatus to,
			@Param("now") Instant now);
}
