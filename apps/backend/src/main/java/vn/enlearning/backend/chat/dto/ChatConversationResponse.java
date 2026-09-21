package vn.enlearning.backend.chat.dto;

import java.time.Instant;
import java.util.UUID;

/** {@code preview}: đoạn đầu của tin nhắn cuối (rỗng khi hội thoại chưa có tin nào). */
public record ChatConversationResponse(UUID id, String title, String preview, Instant updatedAt, long messageCount) {
}
