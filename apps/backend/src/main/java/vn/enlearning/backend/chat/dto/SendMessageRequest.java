package vn.enlearning.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
		@NotBlank(message = "errors.field.messageRequired")
		@Size(max = SendMessageRequest.MAX_LENGTH, message = "errors.field.messageTooLong")
		String content) {

	public static final int MAX_LENGTH = 2000;
}
