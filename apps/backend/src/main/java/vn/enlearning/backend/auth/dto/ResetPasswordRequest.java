package vn.enlearning.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.auth.validation.PasswordConfirmation;
import vn.enlearning.backend.auth.validation.PasswordsMatch;
import vn.enlearning.backend.auth.validation.ValidPassword;
import vn.enlearning.backend.common.ValidationMessageKey;

/**
 * Đặt lại mật khẩu bằng email và mã OTP 6 số. Mã sai định dạng không bị báo riêng: nó đi qua
 * cùng đường kiểm tra như mã sai và cũng bị tính là một lần thử.
 */
@PasswordsMatch
@ValidationMessageKey("errors.checkInfo")
public record ResetPasswordRequest(
		@NotBlank(message = "auth.validation.emailRequired")
		@Size(max = 255, message = "auth.validation.emailFormat")
		@Pattern(regexp = Emails.PATTERN, message = "auth.validation.emailFormat")
		String email,

		@NotBlank(message = "auth.validation.codeRequired")
		@Size(max = 32, message = "errors.invalidCode")
		String code,

		@NotBlank(message = "auth.validation.newPasswordRequired")
		@ValidPassword
		String password,

		String confirmPassword) implements PasswordConfirmation {

	public ResetPasswordRequest {
		email = Emails.normalize(email);
		code = code == null ? null : code.trim();
	}
}
