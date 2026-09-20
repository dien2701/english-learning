package vn.enlearning.backend.entity;

import java.util.UUID;

/**
 * Một câu hoặc đoạn người học phải đọc to. Không phải bảng riêng: danh sách nằm
 * trong cột JSON {@link SpeakingLesson#getPrompts()}, thứ tự mảng là thứ tự hiển thị.
 *
 * @param id        định danh ổn định để lượt nói ({@link SpeakingPromptFeedback#promptId()}) tham chiếu
 * @param text      nội dung cần đọc (tiếng Anh)
 * @param phonetic  phiên âm gợi ý cho cả câu, có thể để trống
 * @param meaningVi nghĩa tiếng Việt
 */
public record SpeakingPrompt(UUID id, String text, String phonetic, String meaningVi) {
}
