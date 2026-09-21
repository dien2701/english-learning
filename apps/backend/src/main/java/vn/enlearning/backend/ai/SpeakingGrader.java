package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.entity.SpeakingPromptFeedback;

/**
 * Cổng chuyển giọng nói thành văn bản và chấm bài nói. Bản giả: {@code FakeSpeakingGrader}; bản OpenAI thật
 * gắn ở bước 4.4. Âm thanh chỉ tồn tại trong bộ nhớ suốt lần gọi này, không bao giờ được lưu.
 */
public interface SpeakingGrader {

	/** @throws AiGradingException khi AI không trả được kết quả hợp lệ */
	Grade grade(Request request);

	/** Bản ghi cho một câu: {@code filename} là tên tệp người học tải lên (chỉ để chẩn đoán). */
	record PromptAudio(UUID promptId, String promptText, String filename, String contentType, byte[] audio) {
	}

	record Request(String lessonTitle, List<PromptAudio> answers) {
	}

	/** Điểm thang 10; {@code promptFeedback} mỗi câu đã đọc một phần tử. */
	record Grade(BigDecimal overallScore, BigDecimal pronunciationScore, BigDecimal vocabularyScore,
			BigDecimal grammarScore, BigDecimal fluencyScore, BigDecimal relevanceScore, List<String> improvements,
			List<SpeakingPromptFeedback> promptFeedback, String modelName) {
	}
}
