package vn.enlearning.backend.practice.dto;

import java.math.BigDecimal;

import vn.enlearning.backend.entity.enums.Skill;

public record SkillBreakdownResponse(Skill skill, int correctCount, int totalQuestions, BigDecimal score) {
}
