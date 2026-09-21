package vn.enlearning.backend.speaking.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.speaking.dto.SpeakingDetailResponse;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.dto.SpeakingSummaryResponse;
import vn.enlearning.backend.speaking.service.SpeakingCatalogService;
import vn.enlearning.backend.speaking.service.SpeakingSubmissionService;

@RestController
@RequestMapping("/speaking")
@RequiredArgsConstructor
public class SpeakingController {

	private final SpeakingCatalogService catalog;
	private final SpeakingSubmissionService submissions;

	@GetMapping("/lessons")
	ApiResponse<PageResponse<SpeakingSummaryResponse>> lessons(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID topicId,
			@RequestParam(required = false) String level,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return ApiResponse.ok(catalog.list(userId(jwt), search, topicId, level, status, page, pageSize));
	}

	@GetMapping("/lessons/{id}")
	ApiResponse<SpeakingDetailResponse> lesson(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(catalog.get(userId(jwt), id));
	}

	/**
	 * multipart/form-data: {@code promptIds} (lặp lại) và {@code audio} (lặp lại, cùng thứ tự) là bản ghi của từng
	 * câu; {@code durationSeconds} là tổng thời lượng ghi âm.
	 */
	@PostMapping(value = "/lessons/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ApiResponse<SpeakingResultResponse> submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestParam(value = "promptIds", required = false) List<UUID> promptIds,
			@RequestParam(value = "audio", required = false) List<MultipartFile> audio,
			@RequestParam(value = "durationSeconds", defaultValue = "0") int durationSeconds) {
		return ApiResponse.ok(submissions.submit(userId(jwt), id, promptIds, audio, durationSeconds));
	}

	@GetMapping("/results/{attemptId}")
	ApiResponse<SpeakingResultResponse> result(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID attemptId) {
		return ApiResponse.ok(submissions.getResult(userId(jwt), attemptId));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
