package vn.enlearning.backend.speaking.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.speaking.dto.SpeakingAttemptResponse;
import vn.enlearning.backend.speaking.dto.SpeakingDetailResponse;
import vn.enlearning.backend.speaking.dto.SpeakingPromptAssessmentResponse;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.dto.SpeakingSubmitRequest;
import vn.enlearning.backend.speaking.dto.SpeakingSummaryResponse;
import vn.enlearning.backend.speaking.service.SpeakingAttemptService;
import vn.enlearning.backend.speaking.service.SpeakingCatalogService;
import vn.enlearning.backend.speaking.service.SpeakingSubmissionService;

@RestController
@RequestMapping("/speaking")
@RequiredArgsConstructor
public class SpeakingController {

	private final SpeakingCatalogService catalog;
	private final SpeakingSubmissionService submissions;
	private final SpeakingAttemptService attemptFlow;

	@GetMapping("/lessons")
	ApiResponse<PageResponse<SpeakingSummaryResponse>> lessons(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID topicId,
			@RequestParam(required = false) String level,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "az") String sort,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return ApiResponse.ok(catalog.list(userId(jwt), search, topicId, level, status, sort, page, pageSize));
	}

	@GetMapping("/lessons/{id}")
	ApiResponse<SpeakingDetailResponse> lesson(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(catalog.get(userId(jwt), id));
	}

	/** Mở lượt nói {@code IN_PROGRESS} (dùng lại lượt đang dở của chính người đó nếu có). */
	@PostMapping("/lessons/{id}/attempts")
	ApiResponse<SpeakingAttemptResponse> startAttempt(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(attemptFlow.start(userId(jwt), id));
	}

	/** multipart/form-data: {@code audio} là bản thu một câu; chấm đồng bộ, thu lại thì ghi đè kết quả câu đó. */
	@PostMapping(value = "/attempts/{attemptId}/prompts/{promptId}/assess",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ApiResponse<SpeakingPromptAssessmentResponse> assess(@AuthenticationPrincipal Jwt jwt,
			@PathVariable UUID attemptId, @PathVariable UUID promptId,
			@RequestParam(value = "audio", required = false) MultipartFile audio) {
		return ApiResponse.ok(attemptFlow.assess(userId(jwt), attemptId, promptId, audio));
	}

	/** Nộp cuối: tổng hợp kết quả từng câu thành điểm cả lượt; cần đủ mọi câu đã chấm. */
	@PostMapping("/attempts/{attemptId}/submit")
	ApiResponse<SpeakingResultResponse> submitAttempt(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID attemptId,
			@RequestBody(required = false) SpeakingSubmitRequest body) {
		return ApiResponse.ok(attemptFlow.submit(userId(jwt), attemptId, body == null ? null : body.durationSeconds()));
	}

	@GetMapping("/results/{attemptId}")
	ApiResponse<SpeakingResultResponse> result(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID attemptId) {
		return ApiResponse.ok(submissions.getResult(userId(jwt), attemptId));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
