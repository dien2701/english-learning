package vn.enlearning.backend.audio.dto;

import java.util.UUID;

import vn.enlearning.backend.entity.enums.AudioSource;

/** Trạng thái audio của một bài nghe sau khi sinh, tải lên hoặc xoá ({@code audioUrl} và {@code audioSource} null khi chưa có). */
public record ListeningAudioResponse(UUID id, String audioUrl, AudioSource audioSource, int durationSeconds) {
}
