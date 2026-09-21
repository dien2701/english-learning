package vn.enlearning.backend.chat.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

	interface ConversationCount {
		UUID getConversationId();

		long getTotal();
	}

	List<ChatMessage> findByConversationIdOrderByCreatedAtAscIdAsc(UUID conversationId);

	/** Mới nhất trước; dùng {@link Pageable} để lấy N tin cuối làm ngữ cảnh. */
	List<ChatMessage> findByConversationIdOrderByCreatedAtDescIdDesc(UUID conversationId, Pageable pageable);

	Optional<ChatMessage> findFirstByConversationIdOrderByCreatedAtDescIdDesc(UUID conversationId);

	long countByConversationId(UUID conversationId);

	@Query("select m.conversation.id as conversationId, count(m) as total from ChatMessage m "
			+ "where m.conversation.id in :ids group by m.conversation.id")
	List<ConversationCount> counts(@Param("ids") Collection<UUID> ids);
}
