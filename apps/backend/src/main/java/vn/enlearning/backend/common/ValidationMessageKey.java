package vn.enlearning.backend.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gắn lên DTO để chọn câu thông báo chung khi Bean Validation thất bại (ví dụ
 * {@code errors.checkLoginInfo}); không gắn thì dùng {@code errors.checkInfo}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValidationMessageKey {

	String value();
}
