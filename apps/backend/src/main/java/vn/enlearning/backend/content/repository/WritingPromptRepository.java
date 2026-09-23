package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface WritingPromptRepository
		extends JpaRepository<WritingPrompt, UUID>, JpaSpecificationExecutor<WritingPrompt> {

	Optional<WritingPrompt> findByIdAndStatus(UUID id, ContentStatus status);

	/** Nội dung ACTIVE đầu tiên, dùng làm gợi ý bài học trong Chat. */
	Optional<WritingPrompt> findFirstByStatusOrderByCreatedAtAscIdAsc(ContentStatus status);

	long countByTopicId(UUID topicId);

	/** Khoá idempotent của seeder dữ liệu thật (đợt 13.5): bỏ qua đề đã có cùng tiêu đề trong chủ đề. */
	Optional<WritingPrompt> findByTopicIdAndTitleVi(UUID topicId, String titleVi);
}
