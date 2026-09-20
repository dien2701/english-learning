package vn.enlearning.backend.entity.enums;

/** Vòng đời bài nộp cần AI chấm (Luyện viết). Luyện nói có vòng đời riêng: {@link SpeakingAttemptStatus}. */
public enum SubmissionStatus { DRAFT, GRADING, GRADED, NEEDS_RETRY }
