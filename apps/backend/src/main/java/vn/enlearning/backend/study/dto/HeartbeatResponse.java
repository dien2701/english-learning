package vn.enlearning.backend.study.dto;

import java.util.UUID;

public record HeartbeatResponse(UUID sessionId, int activeSeconds) {
}
