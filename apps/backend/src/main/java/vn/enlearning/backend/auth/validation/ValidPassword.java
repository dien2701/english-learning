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

/** Mật khẩu đủ dài và không vượt giới hạn BCrypt; chỉ kiểm khi có giá trị, phần bắt buộc để {@code @NotBlank}. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Constraint(validatedBy = ValidPassword.Validator.class)
public @interface ValidPassword {

	String message() default "auth.validation.passwordMin";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	class Validator implements ConstraintValidator<ValidPassword, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			if (value == null) {
				return true;
			}
			String violation = PasswordPolicy.violationKey(value);
			if (violation == null) {
				return true;
			}
			// Mỗi lỗi có khoá dịch riêng, nên thay thông báo mặc định của annotation.
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(violation).addConstraintViolation();
			return false;
		}
	}
}
