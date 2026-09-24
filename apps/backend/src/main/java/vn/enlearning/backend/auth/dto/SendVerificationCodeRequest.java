package vn.enlearning.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.common.ValidationMessageKey;

/** Xin mã xác minh email trước khi đăng ký. {@code language}: "en" thì email tiếng Anh, còn lại tiếng Việt. */
@ValidationMessageKey("auth.validation.emailFormat")
public record SendVerificationCodeRequest(
		@NotBlank(message = "auth.validation.emailRequired")
		@Size(max = 255, message = "auth.validation.emailFormat")
		@Pattern(regexp = Emails.PATTERN, message = "auth.validation.emailFormat")
		String email,

		String language) {

	public SendVerificationCodeRequest {
		email = Emails.normalize(email);
	}
}
