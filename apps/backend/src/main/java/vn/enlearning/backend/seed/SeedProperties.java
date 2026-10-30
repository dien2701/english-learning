package vn.enlearning.backend.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình nạp dữ liệu mẫu đọc từ {@code app.seed.*} (chỉ có tác dụng ở profile {@code dev}).
 *
 * @param mode {@code if-empty}: chỉ nạp phần còn thiếu; {@code reset-demo}: xoá dữ liệu có đánh dấu demo
 *             (email đuôi {@link DemoUserSeedService#EMAIL_SUFFIX}) rồi nạp lại. Không đụng người dùng thật.
 * @param historyEmail nếu có, XOÁ lịch sử học của tài khoản này rồi nạp lại lịch sử mẫu mỗi lần khởi động.
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(@NotNull @DefaultValue("if-empty") Mode mode, String historyEmail) {

	public enum Mode { IF_EMPTY, RESET_DEMO }
}
