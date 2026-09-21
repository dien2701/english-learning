package vn.enlearning.backend.entity.enums;

/**
 * Vòng đời một lượt Luyện nói. {@link #IN_PROGRESS}: đang thu và chấm từng câu (kết quả nằm ở
 * {@code speaking_prompt_results}). Nộp cuối tổng hợp thành {@link #GRADED}. {@link #GRADING} và {@link #FAILED}
 * thuộc luồng nộp một lần cũ; âm thanh không được lưu nên không có chấm lại, người học ghi âm lại.
 */
public enum SpeakingAttemptStatus { IN_PROGRESS, GRADING, GRADED, FAILED }
