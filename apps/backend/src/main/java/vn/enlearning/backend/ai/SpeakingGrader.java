package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.util.List;

import vn.enlearning.backend.entity.SpeakingWordIssue;

/**
 * Cổng chuyển giọng nói thành văn bản và chấm bài nói. Bản giả: {@code FakeSpeakingGrader}; bản thật:
 * {@code GeminiSpeakingGrader} (khi có {@code GEMINI_API_KEY}). Âm thanh chỉ tồn tại trong bộ nhớ suốt lần gọi
 * này, không bao giờ được lưu.
 */
public interface SpeakingGrader {

	/**
	 * Đánh giá phát âm một câu ngay sau khi thu: so bản thu với câu mẫu từng từ.
	 *
	 * @param filename tên tệp người học tải lên (chỉ để chẩn đoán)
	 * @throws AiGradingException khi AI không trả được kết quả hợp lệ
	 */
	PromptAssessment assessPrompt(String promptText, String filename, String contentType, byte[] audio);

	/**
	 * Một lời gọi chỉ văn bản: viết các điểm cần cải thiện cho cả lượt từ kết quả từng câu.
	 *
	 * @throws AiGradingException khi AI không trả được kết quả hợp lệ
	 */
	List<String> writeImprovements(String lessonTitle, List<PromptSummary> prompts);

	/** Kết quả một câu: điểm thang 10 (làm tròn 0.5), từ lỗi, mẹo chung cho câu. */
	record PromptAssessment(String transcript, BigDecimal score, List<SpeakingWordIssue> wordIssues,
			List<String> tips, String modelName) {
	}

	/** Đầu vào của lời gọi viết {@code improvements}: câu mẫu kèm kết quả đã chấm. */
	record PromptSummary(String promptText, PromptAssessment assessment) {
	}
}
