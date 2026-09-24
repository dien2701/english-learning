package vn.enlearning.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.auth.validation.PasswordConfirmation;
import vn.enlearning.backend.auth.validation.PasswordsMatch;
import vn.enlearning.backend.auth.validation.ValidPassword;
import vn.enlearning.backend.common.ValidationMessageKey;

@PasswordsMatch
@ValidationMessageKey("errors.checkRegisterInfo")
public record RegisterRequest(
		@NotBlank(message = "auth.validation.nameRequired")
		@Size(min = 2, message = "auth.validation.nameShort")
		// Không lặp @Size: javac 21 sinh annotation trùng cho thành phần record khi container mang TYPE_USE.
		@Pattern(regexp = "(?s).{0,100}", message = "errors.badRequest")
		String fullName,

		@NotBlank(message = "auth.validation.emailRequired")
		@Size(max = 255, message = "auth.validation.emailFormat")
		@Pattern(regexp = Emails.PATTERN, message = "auth.validation.emailFormat")
		String email,

		@NotBlank(message = "auth.validation.passwordRequired")
		@ValidPassword
		String password,

		String confirmPassword,

		@NotBlank(message = "auth.validation.codeRequired")
		@Size(max = 32, message = "errors.invalidCode")
		String code) implements PasswordConfirmation {

	public RegisterRequest {
		fullName = fullName == null ? null : fullName.trim();
		email = Emails.normalize(email);
		code = code == null ? null : code.trim();
	}
}
