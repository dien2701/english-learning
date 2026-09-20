package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.SubmissionStatus;

/**
 * Bài viết của người học. Luôn lưu bài trước, gọi AI sau; nếu AI không trả
 * kết quả thì bài chuyển {@link SubmissionStatus#NEEDS_RETRY} để chấm lại.
 */
@Getter
@Setter
@Entity
@Table(name = "writing_submissions", indexes = {
		@Index(name = "idx_writing_submissions_user", columnList = "user_id, submitted_at"),
		@Index(name = "idx_writing_submissions_user_prompt", columnList = "user_id, prompt_id, status"),
		@Index(name = "idx_writing_submissions_prompt", columnList = "prompt_id"),
		@Index(name = "idx_writing_submissions_status", columnList = "status, updated_at")
})
public class WritingSubmission extends AuditedEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/** RESTRICT: đề đã có bài nộp thì không xoá được. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "prompt_id", nullable = false)
	private WritingPrompt prompt;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private int wordCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubmissionStatus status = SubmissionStatus.DRAFT;

	/** Trống khi còn là bản nháp. */
	private Instant submittedAt;
}
