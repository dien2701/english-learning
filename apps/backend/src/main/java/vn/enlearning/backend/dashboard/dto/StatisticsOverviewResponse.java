package vn.enlearning.backend.dashboard.dto;

import java.util.List;

import vn.enlearning.backend.practice.dto.AttemptHistoryItemResponse;

/** Khớp {@code StatisticsOverview} ở frontend. Tổng phút là của tuần hiện tại và tuần liền trước. */
public record StatisticsOverviewResponse(
		long totalMinutes,
		long totalMinutesPrevious,
		List<StudyTimePointResponse> weekPoints,
		List<StudyTimePointResponse> monthPoints,
		List<SkillStatResponse> skills,
		List<AttemptHistoryItemResponse> recentAttempts) {
}
