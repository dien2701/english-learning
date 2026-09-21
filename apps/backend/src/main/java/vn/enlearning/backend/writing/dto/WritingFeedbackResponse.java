package vn.enlearning.backend.writing.dto;

import java.math.BigDecimal;
import java.util.List;

public record WritingFeedbackResponse(BigDecimal overallScore, BigDecimal grammarScore, BigDecimal vocabularyScore,
		BigDecimal expressionScore, String summary, List<WritingIssueResponse> issues) {
}
