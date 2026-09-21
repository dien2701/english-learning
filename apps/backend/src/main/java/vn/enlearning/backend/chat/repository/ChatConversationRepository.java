package vn.enlearning.backend.chat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.ChatConversation;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

	/** Chỉ trả hội thoại của chính người dùng và chưa xoá; của người khác coi như không tồn tại. */
	Optional<ChatConversation> findByIdAndUserId(UUID id, UUID userId);

	List<ChatConversation> findByUserIdOrderByLastMessageAtDescIdDesc(UUID userId, Pageable pageable);
}
