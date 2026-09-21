package vn.enlearning.backend.writing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitWritingRequest(
		@NotBlank(message = "errors.field.essayRequired")
		@Size(max = SubmitWritingRequest.MAX_LENGTH, message = "errors.field.essayTooLong")
		String content) {

	public static final int MAX_LENGTH = 20000;
}
