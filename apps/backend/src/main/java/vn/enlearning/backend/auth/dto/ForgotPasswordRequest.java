package vn.enlearning.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.common.ValidationMessageKey;

/** Thiếu hay sai định dạng email đều báo cùng một câu, giống mock. */
@ValidationMessageKey("auth.validation.emailFormat")
public record ForgotPasswordRequest(
		@NotBlank(message = "auth.validation.emailFormat")
		@Size(max = 255, message = "auth.validation.emailFormat")
		@Pattern(regexp = Emails.PATTERN, message = "auth.validation.emailFormat")
		String email) {

	public ForgotPasswordRequest {
		email = Emails.normalize(email);
	}
}
