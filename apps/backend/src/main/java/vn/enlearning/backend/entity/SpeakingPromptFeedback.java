package vn.enlearning.backend.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Nhận xét của AI cho một câu đã đọc. Không phải bảng riêng: danh sách nằm trong
 * cột JSON {@link SpeakingAttempt#getPromptFeedback()}.
 *
 * @param promptId      câu trong {@link SpeakingLesson#getPrompts()}
 * @param transcript    văn bản AI nghe được; âm thanh gốc không được lưu
 * @param score         điểm câu này, thang 10
 * @param mispronounced các từ phát âm chưa chuẩn
 * @param comment       nhận xét ngắn cho người học
 */
public record SpeakingPromptFeedback(UUID promptId, String transcript, BigDecimal score,
		List<String> mispronounced, String comment) {
}
