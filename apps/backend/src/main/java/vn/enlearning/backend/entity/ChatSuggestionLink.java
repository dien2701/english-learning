package vn.enlearning.backend.entity;

import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Liên kết bài học mà trợ lý gợi ý kèm trong câu trả lời. Không phải bảng riêng:
 * danh sách nằm trong cột JSON {@link ChatMessage#getLinks()}, thứ tự mảng là thứ tự hiển thị.
 *
 * @param label tiêu đề hiển thị
 * @param path  đường dẫn trong ứng dụng, đưa thẳng tới đúng nội dung
 * @param skill nhóm nội dung mà liên kết dẫn tới
 */
public record ChatSuggestionLink(String label, String path, StudySkill skill) {
}
