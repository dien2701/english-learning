package vn.enlearning.backend.chat.dto;

/** Trả cả hai tin để giao diện chỉ cần nối thêm vào danh sách. */
public record SendMessageResponse(ChatMessageResponse userMessage, ChatMessageResponse reply) {
}
