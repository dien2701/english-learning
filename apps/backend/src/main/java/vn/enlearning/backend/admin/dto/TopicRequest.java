package vn.enlearning.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicRequest(
		@NotBlank(message = "errors.field.nameRequired")
		@Size(max = 100, message = "errors.field.nameTooLong")
		String nameVi,
		@Size(max = 100, message = "errors.field.nameTooLong")
		String nameEn) {
}
