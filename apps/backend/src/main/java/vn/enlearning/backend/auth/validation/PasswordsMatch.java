package vn.enlearning.backend.auth.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Ô nhập lại phải trùng mật khẩu. Giống mock: chỉ kiểm khi client có gửi {@code confirmPassword}.
 * Lỗi gắn vào ô {@code confirmPassword} để frontend đổ đúng chỗ.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = PasswordsMatch.Validator.class)
public @interface PasswordsMatch {

	String message() default "auth.validation.confirmMismatch";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	class Validator implements ConstraintValidator<PasswordsMatch, PasswordConfirmation> {

		@Override
		public boolean isValid(PasswordConfirmation value, ConstraintValidatorContext context) {
			if (value == null || value.confirmPassword() == null || value.confirmPassword().equals(value.password())) {
				return true;
			}
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("confirmPassword")
					.addConstraintViolation();
			return false;
		}
	}
}
