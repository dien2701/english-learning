package vn.enlearning.backend.chat.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.ChatAssistant;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.chat.dto.ChatConversationDetailResponse;
import vn.enlearning.backend.chat.dto.ChatConversationResponse;
import vn.enlearning.backend.chat.dto.ChatMessageResponse;
import vn.enlearning.backend.chat.dto.SendMessageResponse;
import vn.enlearning.backend.chat.repository.ChatConversationRepository;
import vn.enlearning.backend.chat.repository.ChatMessageRepository;
import vn.enlearning.backend.chat.repository.ChatMessageRepository.ConversationCount;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.ChatConversation;
import vn.enlearning.backend.entity.ChatMessage;
import vn.enlearning.backend.entity.enums.ChatRole;

/**
 * Hội thoại và tin nhắn của người học; hội thoại của người khác hoặc đã xoá là 404. Gửi tin nhắn luôn lưu
 * tin của người học TRƯỚC (commit), gọi AI sau (không giữ giao dịch), rồi lưu câu trả lời. AI lỗi thì tin của
 * người học vẫn còn và API trả 503 để giao diện cho thử lại.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	static final String NEW_TITLE = "Hội thoại mới";
	static final int TITLE_MAX = 48;
	static final int PREVIEW_MAX = 80;
	static final int LIST_LIMIT = 50;
	/** Ngữ cảnh gửi lên AI: tối đa số tin, số ký tự mỗi tin và tổng ký tự (bỏ tin cũ nhất trước). */
	static final int CONTEXT_MESSAGES = 10;
	static final int CONTEXT_CHARS_PER_MESSAGE = 1000;
	static final int CONTEXT_CHARS_TOTAL = 6000;

	private final ChatConversationRepository conversations;
	private final ChatMessageRepository messages;
	private final UserRepository users;
	private final ChatAssistant assistant;
	private final ChatLinkResolver linkResolver;
	private final TransactionTemplate tx;
	private final Clock clock;

	@Transactional
	public ChatConversationDetailResponse create(UUID userId) {
		ChatConversation conversation = new ChatConversation();
		conversation.setUser(users.getReferenceById(userId));
		conversation.setTitle(NEW_TITLE);
		conversation.setLastMessageAt(clock.instant());
		conversations.saveAndFlush(conversation);
		return new ChatConversationDetailResponse(conversation.getId(), conversation.getTitle(), "",
				conversation.getLastMessageAt(), 0, List.of());
	}

	@Transactional(readOnly = true)
	public List<ChatConversationResponse> list(UUID userId) {
		List<ChatConversation> found = conversations.findByUserIdOrderByLastMessageAtDescIdDesc(userId,
				PageRequest.of(0, LIST_LIMIT));
		if (found.isEmpty()) {
			return List.of();
		}
		Map<UUID, Long> counts = new HashMap<>();
		for (ConversationCount row : messages.counts(found.stream().map(ChatConversation::getId).toList())) {
			counts.put(row.getConversationId(), row.getTotal());
		}
		return found.stream().map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), preview(c.getId()),
				c.getLastMessageAt(), counts.getOrDefault(c.getId(), 0L))).toList();
	}

	@Transactional(readOnly = true)
	public ChatConversationDetailResponse get(UUID userId, UUID id) {
		ChatConversation conversation = find(userId, id);
		List<ChatMessage> all = messages.findByConversationIdOrderByCreatedAtAscIdAsc(id);
		String preview = all.isEmpty() ? "" : truncate(all.get(all.size() - 1).getContent(), PREVIEW_MAX);
		return new ChatConversationDetailResponse(id, conversation.getTitle(), preview,
				conversation.getLastMessageAt(), all.size(), all.stream().map(ChatService::toResponse).toList());
	}

	@Transactional
	public ChatConversationResponse rename(UUID userId, UUID id, String title) {
		ChatConversation conversation = find(userId, id);
		conversation.setTitle(title.strip());
		return new ChatConversationResponse(id, conversation.getTitle(), preview(id), conversation.getLastMessageAt(),
				messages.countByConversationId(id));
	}

	/** Xoá mềm; tin nhắn giữ lại để đối soát chi phí AI. */
	@Transactional
	public void delete(UUID userId, UUID id) {
		conversations.delete(find(userId, id));
	}

	/** Cố ý KHÔNG mở giao dịch bao ngoài: tin của người học phải commit trước khi gọi AI (có thể chậm). */
	public SendMessageResponse send(UUID userId, UUID id, String content) {
		find(userId, id);
		String text = content.strip();

		Prepared prepared = tx.execute(status -> {
			ChatConversation conversation = conversations.findById(id)
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
			List<ChatMessage> recent = messages.findByConversationIdOrderByCreatedAtDescIdDesc(id,
					PageRequest.of(0, CONTEXT_MESSAGES));
			ChatMessage userMessage = newMessage(conversation, ChatRole.USER, text);
			messages.saveAndFlush(userMessage);
			if (recent.isEmpty() && NEW_TITLE.equals(conversation.getTitle())) {
				conversation.setTitle(truncate(text, TITLE_MAX) + (text.length() > TITLE_MAX ? "…" : ""));
			}
			conversation.setLastMessageAt(userMessage.getCreatedAt());
			return new Prepared(userMessage, context(recent));
		});

		ChatAssistant.Reply reply;
		try {
			reply = assistant.reply(prepared.history(), text);
			if (reply == null || reply.content() == null || reply.content().isBlank()) {
				throw new IllegalStateException("Câu trả lời rỗng");
			}
		} catch (RuntimeException e) {
			log.warn("Trợ lý AI lỗi ở hội thoại {}: {}", id, e.toString());
			throw new ApiException(ErrorCode.AI_UNAVAILABLE);
		}

		ChatMessage saved = tx.execute(status -> {
			ChatConversation conversation = conversations.findById(id)
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
			ChatMessage message = newMessage(conversation, ChatRole.ASSISTANT, reply.content());
			message.setRefusal(reply.refusal());
			message.setLinks(reply.refusal() ? new ArrayList<>()
					: new ArrayList<>(linkResolver.resolve(reply.suggestedSkills() == null ? List.of()
							: reply.suggestedSkills())));
			message.setModelName(reply.modelName());
			message.setPromptTokens(reply.promptTokens());
			message.setCompletionTokens(reply.completionTokens());
			messages.saveAndFlush(message);
			conversation.setLastMessageAt(message.getCreatedAt());
			return message;
		});
		return new SendMessageResponse(toResponse(prepared.userMessage()), toResponse(saved));
	}

	private record Prepared(ChatMessage userMessage, List<ChatAssistant.Turn> history) {
	}

	private ChatConversation find(UUID userId, UUID id) {
		return conversations.findByIdAndUserId(id, userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private String preview(UUID conversationId) {
		return messages.findFirstByConversationIdOrderByCreatedAtDescIdDesc(conversationId)
				.map(m -> truncate(m.getContent(), PREVIEW_MAX)).orElse("");
	}

	private static ChatMessage newMessage(ChatConversation conversation, ChatRole role, String content) {
		ChatMessage message = new ChatMessage();
		message.setConversation(conversation);
		message.setRole(role);
		message.setContent(content);
		return message;
	}

	/** {@code recent} mới nhất trước; trả về cũ nhất trước, đã cắt theo giới hạn ngữ cảnh. */
	static List<ChatAssistant.Turn> context(List<ChatMessage> recent) {
		List<ChatAssistant.Turn> turns = new ArrayList<>();
		int total = 0;
		for (ChatMessage m : recent) {
			String text = truncate(m.getContent(), CONTEXT_CHARS_PER_MESSAGE);
			if (total + text.length() > CONTEXT_CHARS_TOTAL) {
				break;
			}
			total += text.length();
			turns.add(0, new ChatAssistant.Turn(m.getRole(), text));
		}
		return turns;
	}

	private static String truncate(String text, int max) {
		return text.length() <= max ? text : text.substring(0, max);
	}

	private static ChatMessageResponse toResponse(ChatMessage m) {
		boolean assistant = m.getRole() == ChatRole.ASSISTANT;
		return new ChatMessageResponse(m.getId(), m.getRole(), m.getContent(), m.getCreatedAt(),
				assistant && !m.getLinks().isEmpty() ? List.copyOf(m.getLinks()) : null,
				assistant ? m.isRefusal() : null);
	}
}
