package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, UUID>, JpaSpecificationExecutor<FlashcardDeck> {

	Optional<FlashcardDeck> findByIdAndStatus(UUID id, ContentStatus status);

	/** Nội dung ACTIVE đầu tiên, dùng làm gợi ý bài học trong Chat. */
	Optional<FlashcardDeck> findFirstByStatusOrderByCreatedAtAscIdAsc(ContentStatus status);

	long countByTopicIdAndStatus(UUID topicId, ContentStatus status);
}
