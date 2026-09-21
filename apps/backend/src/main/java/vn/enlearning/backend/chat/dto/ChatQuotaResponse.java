package vn.enlearning.backend.chat.dto;

import java.time.Instant;

/** Hạn mức tin nhắn trong ngày; {@code used} là số câu trợ lý đã trả lời thành công, {@code resetAt} là 0h ngày mai. */
public record ChatQuotaResponse(int limit, int used, int remaining, Instant resetAt) {
}
