package vn.enlearning.backend.practice.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.common.ValidationMessageKey;

@ValidationMessageKey("errors.missingAnswers")
public record SubmitRequest(
		@NotNull(message = "errors.missingAnswers") @Size(max = 500, message = "errors.invalidAnswers") List<@Valid @NotNull AnswerSubmission> answers,
		@PositiveOrZero(message = "errors.invalidAnswers") Integer durationSeconds) {
}
