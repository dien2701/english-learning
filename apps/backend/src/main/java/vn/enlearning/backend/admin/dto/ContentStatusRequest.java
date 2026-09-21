package vn.enlearning.backend.admin.dto;

import jakarta.validation.constraints.NotNull;
import vn.enlearning.backend.entity.enums.ContentStatus;

public record ContentStatusRequest(@NotNull(message = "errors.field.statusRequired") ContentStatus status) {
}
