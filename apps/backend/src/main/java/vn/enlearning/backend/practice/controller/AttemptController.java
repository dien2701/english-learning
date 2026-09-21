package vn.enlearning.backend.practice.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.practice.dto.AttemptHistoryItemResponse;
import vn.enlearning.backend.practice.dto.PracticeResultResponse;
import vn.enlearning.backend.practice.service.PracticeResultService;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class AttemptController {

	private final PracticeResultService results;

	@GetMapping
	ApiResponse<PageResponse<AttemptHistoryItemResponse>> history(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String skill,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(results.history(userId(jwt), skill, page, pageSize));
	}

	@GetMapping("/{attemptId}")
	ApiResponse<PracticeResultResponse> detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID attemptId) {
		return ApiResponse.ok(results.getResult(userId(jwt), attemptId));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
