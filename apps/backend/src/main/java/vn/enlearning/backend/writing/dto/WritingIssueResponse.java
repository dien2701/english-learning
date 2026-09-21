package vn.enlearning.backend.writing.dto;

import vn.enlearning.backend.entity.enums.IssueCategory;

/** {@code id} là chỉ số trong danh sách (issues nằm trong cột JSON, không có khoá riêng). */
public record WritingIssueResponse(String id, IssueCategory category, String excerpt, String problem,
		String suggestion) {
}
