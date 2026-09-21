package vn.enlearning.backend.admin.dto;

import java.util.UUID;

import vn.enlearning.backend.entity.enums.ContentStatus;

public record ContentStatusResponse(UUID id, ContentStatus status) {
}
