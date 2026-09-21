package vn.enlearning.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameConversationRequest(
		@NotBlank(message = "errors.field.titleRequired")
		@Size(max = 200, message = "errors.field.titleTooLong")
		String title) {
}
