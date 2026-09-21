package vn.enlearning.backend.speaking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.speaking.dto.SpeakingPromptFeedbackResponse;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.dto.SpeakingScoresResponse;

/** Đổi lượt nói thành phản hồi. Cần {@code lesson} đã nạp sẵn. Điểm chỉ lộ ra khi GRADED. */
@Component
class SpeakingResultMapper {

	SpeakingResultResponse toResponse(SpeakingAttempt attempt) {
		SpeakingLesson lesson = attempt.getLesson();
		L10n title = L10n.of(lesson.getTitleVi(), lesson.getTitleEn());
		if (attempt.getStatus() != SpeakingAttemptStatus.GRADED) {
			return new SpeakingResultResponse(attempt.getId(), lesson.getId(), title, attempt.getStatus(), null, null,
					List.of(), List.of(), attempt.getSubmittedAt());
		}
		Map<UUID, String> texts = new HashMap<>();
		for (SpeakingPrompt p : lesson.getPrompts()) {
			texts.put(p.id(), p.text());
		}
		List<SpeakingPromptFeedbackResponse> feedback = attempt.getPromptFeedback().stream()
				.map(f -> new SpeakingPromptFeedbackResponse(f.promptId(), texts.getOrDefault(f.promptId(), ""),
						f.transcript(), f.score(), f.mispronounced() == null ? List.of() : f.mispronounced(),
						f.comment()))
				.toList();
		return new SpeakingResultResponse(attempt.getId(), lesson.getId(), title, attempt.getStatus(),
				attempt.getOverallScore(),
				new SpeakingScoresResponse(attempt.getPronunciationScore(), attempt.getVocabularyScore(),
						attempt.getGrammarScore(), attempt.getFluencyScore(), attempt.getRelevanceScore()),
				List.copyOf(attempt.getImprovements()), feedback, attempt.getSubmittedAt());
	}
}
