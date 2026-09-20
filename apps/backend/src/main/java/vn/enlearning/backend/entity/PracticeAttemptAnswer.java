package vn.enlearning.backend.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Câu trả lời của người dùng cho một câu hỏi trong một lượt làm bài. */
@Getter
@Setter
@Entity
@Table(name = "practice_attempt_answers",
		uniqueConstraints = @UniqueConstraint(name = "uq_practice_attempt_answers", columnNames = { "attempt_id", "question_id" }),
		indexes = {
				@Index(name = "idx_practice_attempt_answers_question", columnList = "question_id"),
				@Index(name = "idx_practice_attempt_answers_option", columnList = "selected_option_id")
		})
public class PracticeAttemptAnswer extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attempt_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private PracticeAttempt attempt;

	/** RESTRICT: câu hỏi đã có người trả lời thì không xoá được. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	/** Với câu trắc nghiệm. Tự về NULL nếu Admin xoá lựa chọn. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "selected_option_id")
	private QuestionOption selectedOption;

	/** Với câu điền từ. */
	@Column(length = 500)
	private String answerText;

	/** Backend chấm khi nộp bài; trống nghĩa là chưa chấm. */
	@Column(name = "is_correct")
	private Boolean correct;
}
