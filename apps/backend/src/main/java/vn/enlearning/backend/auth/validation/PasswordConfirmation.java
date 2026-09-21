package vn.enlearning.backend.auth.validation;

/** Request có cặp mật khẩu và ô nhập lại, để {@link PasswordsMatch} kiểm chung. */
public interface PasswordConfirmation {

	String password();

	String confirmPassword();
}
