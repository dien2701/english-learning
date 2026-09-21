package vn.enlearning.backend.profile.dto;

import jakarta.validation.constraints.NotBlank;
import vn.enlearning.backend.auth.validation.ValidPassword;
import vn.enlearning.backend.common.ValidationMessageKey;

@ValidationMessageKey("errors.checkInfo")
public record ChangePasswordRequest(
		@NotBlank(message = "errors.wrongPassword") String currentPassword,
		@NotBlank(message = "auth.validation.newPasswordRequired") @ValidPassword String newPassword) {
}
