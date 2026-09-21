package vn.enlearning.backend.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.entity.ChatSuggestionLink;
import vn.enlearning.backend.entity.enums.ChatRole;

/** {@code links} và {@code isRefusal} chỉ có ở câu trả lời của trợ lý; {@code links} bỏ qua khi rỗng. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageResponse(UUID id, ChatRole role, String content, Instant createdAt,
		List<ChatSuggestionLink> links, Boolean isRefusal) {
}
