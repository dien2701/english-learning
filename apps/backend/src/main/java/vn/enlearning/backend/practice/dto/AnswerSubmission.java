package vn.enlearning.backend.practice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code optionId} cho câu trắc nghiệm, {@code text} cho câu điền từ; để trống nghĩa là bỏ qua câu này. */
public record AnswerSubmission(
		@NotNull(message = "errors.invalidAnswers") UUID questionId,
		UUID optionId,
		@Size(max = 500, message = "errors.invalidAnswers") String text) {
}
