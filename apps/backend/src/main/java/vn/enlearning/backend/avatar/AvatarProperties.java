package vn.enlearning.backend.avatar;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình ảnh đại diện đọc từ {@code app.avatar.*}, mặc định dùng chung tài khoản Cloudinary với audio.
 *
 * @param cloudinaryUrl     {@code cloudinary://<api_key>:<api_secret>@<cloud_name>}; để trống thì lưu cục bộ
 * @param cloudinaryBaseUrl gốc REST của Cloudinary (đổi được khi test)
 * @param cloudinaryTimeout thời gian tối đa cho một lần tải lên/xoá ở Cloudinary
 * @param localDir          thư mục lưu ảnh khi chưa có Cloudinary, phục vụ qua {@code /media/avatars/**}
 */
@Validated
@ConfigurationProperties(prefix = "app.avatar")
public record AvatarProperties(
		@DefaultValue("") String cloudinaryUrl,
		@NotBlank @DefaultValue("https://api.cloudinary.com") String cloudinaryBaseUrl,
		@NotNull @DefaultValue("60s") Duration cloudinaryTimeout,
		@NotBlank @DefaultValue("uploads/avatars") String localDir) {
}
