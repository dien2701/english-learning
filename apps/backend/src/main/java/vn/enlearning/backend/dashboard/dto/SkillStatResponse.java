package vn.enlearning.backend.dashboard.dto;

import vn.enlearning.backend.entity.enums.Skill;

/** Khớp {@code SkillStat} ở frontend: điểm thang 10, {@code change} là chênh lệch điểm so với 30 ngày trước đó. */
public record SkillStatResponse(Skill skill, double averageScore, long attempts, double change) {
}
