package vn.enlearning.backend.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Như {@link ChatConversationResponse}, thêm toàn bộ tin nhắn theo thứ tự thời gian. */
public record ChatConversationDetailResponse(UUID id, String title, String preview, Instant updatedAt,
		long messageCount, List<ChatMessageResponse> messages) {
}
