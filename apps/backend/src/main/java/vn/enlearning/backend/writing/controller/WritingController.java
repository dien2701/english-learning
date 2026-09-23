package vn.enlearning.backend.writing.controller;

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
import vn.enlearning.backend.writing.dto.SubmitWritingRequest;
import vn.enlearning.backend.writing.dto.WritingPromptDetailResponse;
import vn.enlearning.backend.writing.dto.WritingPromptSummaryResponse;
import vn.enlearning.backend.writing.dto.WritingSubmissionResponse;
import vn.enlearning.backend.writing.service.WritingCatalogService;
import vn.enlearning.backend.writing.service.WritingSubmissionService;

@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
public class WritingController {

	private final WritingCatalogService catalog;
	private final WritingSubmissionService submissions;

	@GetMapping("/prompts")
	ApiResponse<PageResponse<WritingPromptSummaryResponse>> prompts(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID topicId,
			@RequestParam(required = false) String level,
			@RequestParam(defaultValue = "az") String sort,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return ApiResponse.ok(catalog.list(userId(jwt), search, topicId, level, sort, page, pageSize));
	}

	@GetMapping("/prompts/{id}")
	ApiResponse<WritingPromptDetailResponse> prompt(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(catalog.get(userId(jwt), id));
	}

	@PostMapping("/prompts/{id}/submit")
	ApiResponse<WritingSubmissionResponse> submit(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody SubmitWritingRequest request) {
		return ApiResponse.ok(submissions.submit(userId(jwt), id, request.content()));
	}

	@GetMapping("/submissions")
	ApiResponse<PageResponse<WritingSubmissionResponse>> history(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(submissions.history(userId(jwt), page, pageSize));
	}

	@GetMapping("/submissions/{id}")
	ApiResponse<WritingSubmissionResponse> submission(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(submissions.get(userId(jwt), id));
	}

	@PostMapping("/submissions/{id}/regrade")
	ApiResponse<WritingSubmissionResponse> regrade(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(submissions.regrade(userId(jwt), id));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
