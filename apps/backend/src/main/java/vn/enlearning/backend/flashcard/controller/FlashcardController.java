package vn.enlearning.backend.flashcard.controller;

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
import vn.enlearning.backend.flashcard.dto.DeckDetailResponse;
import vn.enlearning.backend.flashcard.dto.DeckSummaryResponse;
import vn.enlearning.backend.flashcard.dto.FinishRequest;
import vn.enlearning.backend.flashcard.dto.ProgressResponse;
import vn.enlearning.backend.flashcard.dto.RecallRequest;
import vn.enlearning.backend.flashcard.dto.StudyResultResponse;
import vn.enlearning.backend.flashcard.service.FlashcardService;

@RestController
@RequestMapping("/flashcard/decks")
@RequiredArgsConstructor
public class FlashcardController {

	private final FlashcardService flashcardService;

	@GetMapping
	ApiResponse<PageResponse<DeckSummaryResponse>> list(@AuthenticationPrincipal Jwt jwt,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID topicId,
			@RequestParam(required = false) String level,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "12") int pageSize) {
		return ApiResponse.ok(flashcardService.listDecks(userId(jwt), search, topicId, level, status, page, pageSize));
	}

	@GetMapping("/{id}")
	ApiResponse<DeckDetailResponse> detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(flashcardService.getDeck(userId(jwt), id));
	}

	@PostMapping("/{id}/progress")
	ApiResponse<ProgressResponse> saveRecall(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody RecallRequest request) {
		return ApiResponse.ok(flashcardService.saveRecall(userId(jwt), id, request));
	}

	@PostMapping("/{id}/finish")
	ApiResponse<StudyResultResponse> finish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@RequestBody(required = false) FinishRequest request) {
		return ApiResponse.ok(flashcardService.finish(userId(jwt), id, request));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
