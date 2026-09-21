package vn.enlearning.backend.entity;

/**
 * Một từ đọc chưa chuẩn trong một câu. Không phải bảng riêng: danh sách nằm trong cột JSON
 * {@link SpeakingPromptResult#getWordIssues()}.
 *
 * @param word    từ trong câu mẫu
 * @param heardAs AI nghe thành gì (rỗng nếu đọc thiếu)
 * @param issue   lỗi cụ thể (âm, trọng âm, âm cuối...)
 * @param tip     cách sửa ngắn bằng tiếng Việt
 */
public record SpeakingWordIssue(String word, String heardAs, String issue, String tip) {
}
