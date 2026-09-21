package vn.enlearning.backend.practice.dto;

import java.util.UUID;

/** Phương án của câu trắc nghiệm; cố ý KHÔNG có cờ đúng/sai. */
public record QuestionOptionResponse(UUID id, String text) {
}
