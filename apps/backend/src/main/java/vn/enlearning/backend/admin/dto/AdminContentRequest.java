package vn.enlearning.backend.admin.dto;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;

/**
 * Body của POST/PUT {@code /admin/content}: kiểu đa hình theo trường {@code skill}
 * (VOCABULARY, LISTENING, READING, WRITING, SPEAKING, EXAM). PUT thay toàn bộ nội dung; phần tử con
 * (thẻ, câu hỏi, câu nói) có {@code id} thì được sửa tại chỗ, không có thì tạo mới, vắng mặt thì bị xoá
 * (bị từ chối nếu nội dung đã có trong lịch sử học). Số vắng mặt lấy mặc định (thời lượng/giới hạn/số từ tối thiểu 0,
 * thời gian gợi ý bài viết 30 phút).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "skill")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AdminContentRequest.Vocabulary.class, name = "VOCABULARY"),
		@JsonSubTypes.Type(value = AdminContentRequest.Listening.class, name = "LISTENING"),
		@JsonSubTypes.Type(value = AdminContentRequest.Reading.class, name = "READING"),
		@JsonSubTypes.Type(value = AdminContentRequest.Writing.class, name = "WRITING"),
		@JsonSubTypes.Type(value = AdminContentRequest.Speaking.class, name = "SPEAKING"),
		@JsonSubTypes.Type(value = AdminContentRequest.Exam.class, name = "EXAM")
})
public sealed interface AdminContentRequest {

	String TITLE_REQUIRED = "errors.field.titleRequired";
	String TITLE_TOO_LONG = "errors.field.titleTooLong";
	String LEVEL_REQUIRED = "errors.field.levelRequired";
	String TOPIC_REQUIRED = "errors.field.topicRequired";
	String ITEMS_REQUIRED = "errors.field.itemsRequired";
	String TEXT_TOO_LONG = "errors.field.textTooLong";
	String NUMBER_INVALID = "errors.field.numberInvalid";

	String titleVi();

	String titleEn();

	Level level();

	/** Đề kiểm tra không thuộc chủ đề nào. */
	default UUID topicId() {
		return null;
	}

	record Vocabulary(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@NotNull(message = TOPIC_REQUIRED) UUID topicId,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionVi,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionEn,
			@Size(max = 500, message = TEXT_TOO_LONG) String coverImageUrl,
			@NotEmpty(message = ITEMS_REQUIRED) List<@Valid @NotNull(message = ITEMS_REQUIRED) CardInput> cards)
			implements AdminContentRequest {
	}

	record Listening(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@NotNull(message = TOPIC_REQUIRED) UUID topicId,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionVi,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionEn,
			@Size(max = 500, message = TEXT_TOO_LONG) String audioUrl,
			@Min(value = 0, message = NUMBER_INVALID) Integer durationSeconds,
			@NotBlank(message = "errors.field.transcriptRequired") String transcript,
			@NotEmpty(message = ITEMS_REQUIRED) List<@Valid @NotNull(message = ITEMS_REQUIRED) QuestionInput> questions)
			implements AdminContentRequest {
	}

	record Reading(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@NotNull(message = TOPIC_REQUIRED) UUID topicId,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionVi,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionEn,
			@Min(value = 0, message = NUMBER_INVALID) Integer timeLimitMinutes,
			@NotEmpty(message = ITEMS_REQUIRED) List<@NotBlank(message = ITEMS_REQUIRED) String> paragraphs,
			@NotEmpty(message = ITEMS_REQUIRED) List<@Valid @NotNull(message = ITEMS_REQUIRED) QuestionInput> questions)
			implements AdminContentRequest {
	}

	record Writing(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@NotNull(message = TOPIC_REQUIRED) UUID topicId,
			@NotBlank(message = "errors.field.instructionsRequired") String instructions,
			@Min(value = 1, message = NUMBER_INVALID) Integer suggestedMinutes,
			@Min(value = 0, message = NUMBER_INVALID) Integer minWords,
			List<@NotBlank(message = ITEMS_REQUIRED) String> hints) implements AdminContentRequest {
	}

	record Speaking(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@NotNull(message = TOPIC_REQUIRED) UUID topicId,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionVi,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionEn,
			@NotEmpty(message = ITEMS_REQUIRED) List<@Valid @NotNull(message = ITEMS_REQUIRED) PromptInput> prompts)
			implements AdminContentRequest {
	}

	record Exam(
			@NotBlank(message = TITLE_REQUIRED) @Size(max = 200, message = TITLE_TOO_LONG) String titleVi,
			@Size(max = 200, message = TITLE_TOO_LONG) String titleEn,
			@NotNull(message = LEVEL_REQUIRED) Level level,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionVi,
			@Size(max = 1000, message = TEXT_TOO_LONG) String descriptionEn,
			@Min(value = 0, message = NUMBER_INVALID) Integer timeLimitMinutes,
			@NotEmpty(message = ITEMS_REQUIRED) List<@Valid @NotNull(message = ITEMS_REQUIRED) QuestionInput> questions)
			implements AdminContentRequest {
	}

	/** Một thẻ từ; từ và câu ví dụ luôn bằng tiếng Anh. */
	record CardInput(
			UUID id,
			@NotBlank(message = "errors.field.wordRequired") @Size(max = 100, message = TEXT_TOO_LONG) String word,
			@Size(max = 100, message = TEXT_TOO_LONG) String phonetic,
			@NotBlank(message = "errors.field.meaningRequired") @Size(max = 500, message = TEXT_TOO_LONG) String meaningVi,
			@Size(max = 500, message = TEXT_TOO_LONG) String meaningEn,
			@Size(max = 50, message = TEXT_TOO_LONG) String partOfSpeechVi,
			@Size(max = 50, message = TEXT_TOO_LONG) String partOfSpeechEn,
			@Size(max = 500, message = TEXT_TOO_LONG) String example,
			@Size(max = 500, message = TEXT_TOO_LONG) String exampleMeaning,
			@Size(max = 500, message = TEXT_TOO_LONG) String imageUrl,
			@Size(max = 500, message = TEXT_TOO_LONG) String audioUrl) {
	}

	/**
	 * Câu hỏi Nghe/Đọc/Kiểm tra. {@code skill} chỉ dùng cho đề kiểm tra (Nghe/Đọc tự gán theo bài).
	 * SINGLE_CHOICE cần {@code options} với đúng một phương án đúng; FILL_BLANK cần {@code acceptedAnswers}.
	 */
	record QuestionInput(
			UUID id,
			Skill skill,
			@NotNull(message = "errors.field.kindRequired") QuestionKind kind,
			@NotBlank(message = "errors.field.contentRequired") String content,
			String explanation,
			List<@Valid @NotNull(message = ITEMS_REQUIRED) OptionInput> options,
			List<@NotBlank(message = ITEMS_REQUIRED) @Size(max = 500, message = TEXT_TOO_LONG) String> acceptedAnswers) {
	}

	record OptionInput(
			@NotBlank(message = "errors.field.contentRequired") @Size(max = 500, message = TEXT_TOO_LONG) String content,
			Boolean correct) {
	}

	/** Một câu người học phải đọc to. */
	record PromptInput(
			UUID id,
			@NotBlank(message = "errors.field.contentRequired") String text,
			String phonetic,
			String meaningVi) {
	}
}
