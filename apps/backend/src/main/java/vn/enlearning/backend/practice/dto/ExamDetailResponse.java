package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.Skill;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamDetailResponse(UUID id, L10n title, L10n description, List<Skill> skills, Level level,
		long questionCount, int timeLimitMinutes, String status, BigDecimal lastScore,
		List<QuestionResponse> questions) {
}
