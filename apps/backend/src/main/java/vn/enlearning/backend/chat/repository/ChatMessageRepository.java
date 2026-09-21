package vn.enlearning.backend.chat.repository;

import java.time.Instant;
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

	/**
	 * Số câu trợ lý đã trả lời một người trong khoảng [from, to), tính cả hội thoại đã xoá mềm (native để không
	 * dính {@code @SQLRestriction}, tránh xoá hội thoại để lách hạn mức).
	 */
	@Query(value = "select count(*) from chat_messages m join chat_conversations c on c.id = m.conversation_id "
			+ "where c.user_id = :userId and m.role = 'ASSISTANT' and m.created_at >= :from and m.created_at < :to",
			nativeQuery = true)
	int countAssistantReplies(@Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

	@Query("select m.conversation.id as conversationId, count(m) as total from ChatMessage m "
			+ "where m.conversation.id in :ids group by m.conversation.id")
	List<ConversationCount> counts(@Param("ids") Collection<UUID> ids);
}
