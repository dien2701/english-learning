package vn.enlearning.backend.practice.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.practice.dto.ExamDetailResponse;
import vn.enlearning.backend.practice.dto.ExamSummaryResponse;
import vn.enlearning.backend.practice.dto.PracticeResultResponse;
import vn.enlearning.backend.practice.dto.SubmitRequest;
import vn.enlearning.backend.practice.service.PracticeCatalogService;
import vn.enlearning.backend.practice.service.PracticeResultService;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

	private final PracticeCatalogService catalog;
	private final PracticeResultService results;

	@GetMapping
	ApiResponse<PageResponse<ExamSummaryResponse>> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String level,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return ApiResponse.ok(catalog.listExams(userId(jwt), search, level, status, page, pageSize));
	}

	@GetMapping("/{id}")
	ApiResponse<ExamDetailResponse> detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(catalog.getExam(userId(jwt), id));
	}

	@PostMapping("/{id}/submit")
	ApiResponse<PracticeResultResponse> submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody SubmitRequest request) {
		return ApiResponse.ok(results.submitExam(userId(jwt), id, request));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
