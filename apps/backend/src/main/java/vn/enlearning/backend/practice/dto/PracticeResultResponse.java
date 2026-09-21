package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Skill;

/**
 * Kết quả một lượt Nghe, Đọc hoặc Kiểm tra (chỉ trả sau khi nộp). {@code transcript} chỉ có với bài nghe,
 * {@code breakdown} chỉ có với đề kiểm tra; {@code timedOut} là nộp quá giới hạn thời gian.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PracticeResultResponse(UUID attemptId, UUID lessonId, L10n lessonTitle, Skill skill, BigDecimal score,
		int correctCount, int wrongCount, int totalQuestions, int durationSeconds, Instant submittedAt,
		boolean timedOut, List<GradedAnswerResponse> answers, String transcript,
		List<SkillBreakdownResponse> breakdown) {
}
