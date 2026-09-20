package vn.enlearning.backend.entity.enums;

/**
 * Vòng đời một lượt Luyện nói. Âm thanh không được lưu nên không có trạng thái
 * chấm lại: AI lỗi thì lượt là {@link #FAILED} và người học ghi âm lại.
 */
public enum SpeakingAttemptStatus { GRADING, GRADED, FAILED }
