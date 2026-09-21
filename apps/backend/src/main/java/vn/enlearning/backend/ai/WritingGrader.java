package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.util.List;

import vn.enlearning.backend.entity.AiFeedbackIssue;

/** Cổng chấm bài viết. Bản giả: {@code FakeWritingGrader}; bản Gemini: {@code GeminiWritingGrader}. */
public interface WritingGrader {

	/** @throws AiGradingException khi AI không trả được kết quả hợp lệ */
	Grade grade(Request request);

	/** @param instructions yêu cầu đề bài (tiếng Anh) */
	record Request(String instructions, int minWords, String content, int wordCount) {
	}

	/** Điểm thang 10. {@code modelName} được lưu để truy vết. */
	record Grade(BigDecimal overallScore, BigDecimal grammarScore, BigDecimal vocabularyScore,
			BigDecimal expressionScore, String summary, List<AiFeedbackIssue> issues, String modelName) {
	}
}
