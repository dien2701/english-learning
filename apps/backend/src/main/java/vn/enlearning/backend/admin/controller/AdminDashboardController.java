package vn.enlearning.backend.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse;
import vn.enlearning.backend.admin.service.AdminDashboardService;
import vn.enlearning.backend.common.ApiResponse;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

	private final AdminDashboardService dashboard;

	@GetMapping
	ApiResponse<AdminDashboardResponse> get() {
		return ApiResponse.ok(dashboard.get());
	}
}
