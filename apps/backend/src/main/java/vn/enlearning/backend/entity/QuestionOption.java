package vn.enlearning.backend.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Một lựa chọn của câu trắc nghiệm. */
@Getter
@Setter
@Entity
@Table(name = "question_options",
		uniqueConstraints = @UniqueConstraint(name = "uq_question_options_order", columnNames = { "question_id", "sort_order" }))
public class QuestionOption extends AuditedEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Question question;

	@Column(nullable = false)
	private int sortOrder;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(name = "is_correct", nullable = false)
	private boolean correct;
}
