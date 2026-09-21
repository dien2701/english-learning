package vn.enlearning.backend.chat.dto;

/** Trả cả hai tin để giao diện chỉ cần nối thêm vào danh sách; {@code remaining} là số lượt còn lại hôm nay. */
public record SendMessageResponse(ChatMessageResponse userMessage, ChatMessageResponse reply, int remaining) {
}
