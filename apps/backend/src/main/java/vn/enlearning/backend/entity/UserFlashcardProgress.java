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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.RecallLevel;

/**
 * Mức nhớ của một người dùng với một thẻ. Frontend chỉ gửi flashcardId và
 * mức nhớ; backend tự tính {@code nextReviewAt} và {@code intervalDays}.
 */
@Getter
@Setter
@Entity
@Table(name = "user_flashcard_progress",
		uniqueConstraints = @UniqueConstraint(name = "uq_user_flashcard_progress", columnNames = { "user_id", "flashcard_id" }),
		indexes = {
				@Index(name = "idx_user_flashcard_progress_due", columnList = "user_id, next_review_at"),
				@Index(name = "idx_user_flashcard_progress_recent", columnList = "user_id, last_reviewed_at"),
				@Index(name = "idx_user_flashcard_progress_flashcard", columnList = "flashcard_id")
		})
public class UserFlashcardProgress extends AuditedEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/** RESTRICT: thẻ đã có người học thì không xoá được. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "flashcard_id", nullable = false)
	private Flashcard flashcard;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RecallLevel recallLevel;

	@Column(nullable = false)
	private int reviewCount = 1;

	@Column(nullable = false)
	private int intervalDays;

	@Column(nullable = false)
	private Instant lastReviewedAt;

	@Column(nullable = false)
	private Instant nextReviewAt;
}
