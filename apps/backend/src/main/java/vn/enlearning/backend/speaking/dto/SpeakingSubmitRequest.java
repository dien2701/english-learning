package vn.enlearning.backend.speaking.dto;

/** {@code durationSeconds}: tổng thời lượng ghi âm; thiếu thì coi là 0. */
public record SpeakingSubmitRequest(Integer durationSeconds) {
}
