package vn.enlearning.backend.dashboard.dto;

import java.util.List;

/** Khớp {@code DashboardSummary} ở frontend; {@code continueLearning} là null khi không có bài nào đang dở. */
public record DashboardSummaryResponse(
		String greetingName,
		ContinueLearningResponse continueLearning,
		List<AttendedLessonResponse> attendedLessons) {
}
