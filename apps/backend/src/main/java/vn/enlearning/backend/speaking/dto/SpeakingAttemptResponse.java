package vn.enlearning.backend.speaking.dto;

import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;

/** Lượt nói đang thu: {@code results} là các câu đã chấm (rỗng khi mới bắt đầu, có sẵn khi dùng lại lượt cũ). */
public record SpeakingAttemptResponse(UUID attemptId, UUID lessonId, SpeakingAttemptStatus status,
		List<SpeakingPromptAssessmentResponse> results) {
}
