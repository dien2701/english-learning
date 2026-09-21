package vn.enlearning.backend.practice.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import vn.enlearning.backend.entity.enums.QuestionKind;

/** Câu hỏi khi đang làm bài: không đáp án, không giải thích. {@code options} chỉ có với câu trắc nghiệm. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(UUID id, QuestionKind kind, int order, String text,
		List<QuestionOptionResponse> options) {
}
