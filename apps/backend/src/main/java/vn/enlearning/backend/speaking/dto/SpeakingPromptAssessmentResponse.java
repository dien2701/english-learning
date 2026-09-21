package vn.enlearning.backend.speaking.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.entity.SpeakingWordIssue;

/**
 * Kết quả chấm một câu ngay sau khi thu. {@code transcript} là văn bản AI nghe được (âm thanh gốc không được
 * lưu); {@code wordIssues} là các từ đọc chưa chuẩn kèm cách sửa; {@code tips} là mẹo chung cho câu.
 */
public record SpeakingPromptAssessmentResponse(UUID promptId, String transcript, BigDecimal score,
		List<SpeakingWordIssue> wordIssues, List<String> tips) {
}
