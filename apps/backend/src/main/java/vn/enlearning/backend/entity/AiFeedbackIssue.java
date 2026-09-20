package vn.enlearning.backend.entity;

import vn.enlearning.backend.entity.enums.IssueCategory;

/**
 * Một lỗi AI phát hiện trong bài viết, kèm cách sửa. Không phải bảng riêng:
 * danh sách lỗi nằm trong cột JSON {@link AiFeedback#getIssues()}, thứ tự mảng là thứ tự hiển thị.
 *
 * @param category   nhóm lỗi
 * @param excerpt    đoạn văn bản có vấn đề, trích từ bài của người học
 * @param problem    mô tả vấn đề
 * @param suggestion cách sửa gợi ý
 */
public record AiFeedbackIssue(IssueCategory category, String excerpt, String problem, String suggestion) {
}
