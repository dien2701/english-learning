package vn.enlearning.backend.content.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface ListeningLessonRepository
		extends JpaRepository<ListeningLesson, UUID>, JpaSpecificationExecutor<ListeningLesson> {

	Optional<ListeningLesson> findByIdAndStatus(UUID id, ContentStatus status);

	/** Nội dung ACTIVE đầu tiên, dùng làm gợi ý bài học trong Chat. */
	Optional<ListeningLesson> findFirstByStatusOrderByCreatedAtAscIdAsc(ContentStatus status);

	long countByTopicId(UUID topicId);

	/** Khoá idempotent của seeder dữ liệu thật (đợt 13.5): bỏ qua bài đã có cùng tiêu đề trong chủ đề. */
	boolean existsByTopicIdAndTitleVi(UUID topicId, String titleVi);

	/** Bài chưa có audio, theo thứ tự tạo (dùng cho lệnh sinh audio seed, đợt 12c). */
	@Query("select l from ListeningLesson l where l.audioUrl is null or l.audioUrl = '' order by l.createdAt, l.id")
	List<ListeningLesson> findAllWithoutAudio();
}
