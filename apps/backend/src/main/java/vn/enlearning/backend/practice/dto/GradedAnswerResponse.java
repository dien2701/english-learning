package vn.enlearning.backend.practice.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Kết quả một câu sau khi nộp; {@code userAnswer} là null (vẫn có trong JSON) khi bỏ trống. */
public record GradedAnswerResponse(UUID questionId, int order, String text, String userAnswer,
		String correctAnswer, boolean isCorrect, @JsonInclude(JsonInclude.Include.NON_NULL) String explanation) {
}
