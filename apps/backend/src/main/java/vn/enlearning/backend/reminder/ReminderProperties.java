package vn.enlearning.backend.reminder;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Cấu hình nhắc học đọc từ {@code app.reminder.*}.
 *
 * @param catchUp người dùng chỉ được nhắc trong khoảng này kể từ giờ đặt; trễ hơn (server tắt, hoặc vừa bật
 *                nhắc sau giờ đặt) thì bỏ qua ngày đó
 */
@Validated
@ConfigurationProperties(prefix = "app.reminder")
public record ReminderProperties(@NotNull @DefaultValue("1h") Duration catchUp) {
}
