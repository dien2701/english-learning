package vn.enlearning.backend.dashboard.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.dashboard.dto.DashboardSummaryResponse;
import vn.enlearning.backend.dashboard.dto.StatisticsOverviewResponse;
import vn.enlearning.backend.dashboard.dto.StudyPeriod;
import vn.enlearning.backend.dashboard.dto.StudyTimeResponse;
import vn.enlearning.backend.dashboard.service.DashboardService;
import vn.enlearning.backend.dashboard.service.StatisticsService;
import vn.enlearning.backend.dashboard.service.StudyTimeService;

/** Dashboard và Thống kê; mọi số liệu chỉ của người đang đăng nhập. */
@RestController
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboard;
	private final StudyTimeService studyTime;
	private final StatisticsService statistics;

	@GetMapping("/dashboard/summary")
	ApiResponse<DashboardSummaryResponse> summary(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String status) {
		return ApiResponse.ok(dashboard.summary(userId(jwt), status));
	}

	@GetMapping("/dashboard/study-time")
	ApiResponse<StudyTimeResponse> studyTimeChart(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String period) {
		return ApiResponse.ok(studyTime.chart(userId(jwt), StudyPeriod.parse(period)));
	}

	@GetMapping("/statistics/overview")
	ApiResponse<StatisticsOverviewResponse> overview(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(statistics.overview(userId(jwt)));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
