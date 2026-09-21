package vn.enlearning.backend.dashboard.dto;

import java.util.List;

/** Khớp {@code StudyTimeChart} ở frontend. {@code changePercent} là 0 khi kỳ trước chưa học gì. */
public record StudyTimeResponse(
		StudyPeriod period,
		List<StudyTimePointResponse> points,
		long totalMinutes,
		long previousTotalMinutes,
		int changePercent) {
}
