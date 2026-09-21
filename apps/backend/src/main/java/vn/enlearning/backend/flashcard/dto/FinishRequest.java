package vn.enlearning.backend.flashcard.dto;

import java.util.List;
import java.util.UUID;

/** {@code studiedIds} trống hoặc thiếu thì lấy mọi thẻ của bộ mà người dùng đã đánh giá. */
public record FinishRequest(List<UUID> studiedIds) {
}
