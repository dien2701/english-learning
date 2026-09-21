package vn.enlearning.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.common.ValidationMessageKey;

@ValidationMessageKey("errors.checkLoginInfo")
public record LoginRequest(
		@NotBlank(message = "auth.validation.emailRequired")
		@Size(max = 255, message = "auth.validation.emailFormat")
		@Pattern(regexp = Emails.PATTERN, message = "auth.validation.emailFormat")
		String email,

		@NotBlank(message = "auth.validation.passwordRequired")
		String password) {

	public LoginRequest {
		email = Emails.normalize(email);
	}
}
