package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface ReadingLessonRepository
		extends JpaRepository<ReadingLesson, UUID>, JpaSpecificationExecutor<ReadingLesson> {

	Optional<ReadingLesson> findByIdAndStatus(UUID id, ContentStatus status);

	/** Nội dung ACTIVE đầu tiên, dùng làm gợi ý bài học trong Chat. */
	Optional<ReadingLesson> findFirstByStatusOrderByCreatedAtAscIdAsc(ContentStatus status);

	long countByTopicId(UUID topicId);

	/** Khoá idempotent của seeder dữ liệu thật (đợt 13.7): bỏ qua bài đã có cùng tiêu đề trong chủ đề. */
	Optional<ReadingLesson> findByTopicIdAndTitleVi(UUID topicId, String titleVi);

	long countByStatus(vn.enlearning.backend.entity.enums.ContentStatus status);
}
