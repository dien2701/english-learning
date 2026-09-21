package vn.enlearning.backend.ai;

import java.util.List;

import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Cổng trợ lý chat. Bản giả: {@code FakeChatAssistant}; bản OpenAI thật gắn ở bước 4.4.
 * Trợ lý chỉ gợi ý NHÓM bài ({@code suggestedSkills}); liên kết tới nội dung cụ thể do backend chọn từ DB,
 * để đường dẫn không bao giờ do mô hình bịa ra.
 */
public interface ChatAssistant {

	/**
	 * @param history các tin trước đó, cũ nhất trước (đã được giới hạn độ dài)
	 * @throws AiUnavailableException khi AI không trả được câu trả lời
	 */
	Reply reply(List<Turn> history, String message);

	record Turn(ChatRole role, String content) {
	}

	/** {@code refusal}: câu hỏi ngoài phạm vi học tiếng Anh. Số token có thể null nếu nguồn không báo. */
	record Reply(String content, List<StudySkill> suggestedSkills, boolean refusal, String modelName,
			Integer promptTokens, Integer completionTokens) {
	}
}
