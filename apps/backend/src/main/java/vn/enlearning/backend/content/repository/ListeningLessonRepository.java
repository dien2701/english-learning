package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface ListeningLessonRepository
		extends JpaRepository<ListeningLesson, UUID>, JpaSpecificationExecutor<ListeningLesson> {

	Optional<ListeningLesson> findByIdAndStatus(UUID id, ContentStatus status);

	/** Nội dung ACTIVE đầu tiên, dùng làm gợi ý bài học trong Chat. */
	Optional<ListeningLesson> findFirstByStatusOrderByCreatedAtAscIdAsc(ContentStatus status);
}
