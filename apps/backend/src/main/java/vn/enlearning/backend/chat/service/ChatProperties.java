package vn.enlearning.backend.chat.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

/** @param dailyLimit số tin trợ lý tối đa trả lời cho một người mỗi ngày (tính theo Asia/Ho_Chi_Minh) */
@Validated
@ConfigurationProperties(prefix = "app.chat")
public record ChatProperties(@Min(1) @DefaultValue("30") int dailyLimit) {
}
