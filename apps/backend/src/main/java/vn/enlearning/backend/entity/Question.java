package vn.enlearning.backend.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;

/**
 * Câu hỏi dùng chung cho Luyện nghe, Luyện đọc và Bài kiểm tra.
 *
 * <p>Mỗi câu thuộc đúng một trong ba cha: {@code listeningLesson},
 * {@code readingLesson} hoặc {@code exam}. MySQL không cho CHECK trên cột
 * có FK CASCADE nên tầng Service phải bảo đảm quy tắc "đúng một cha".
 */
@Getter
@Setter
@Entity
@Table(name = "questions", indexes = {
		@Index(name = "idx_questions_listening", columnList = "listening_lesson_id, sort_order"),
		@Index(name = "idx_questions_reading", columnList = "reading_lesson_id, sort_order"),
		@Index(name = "idx_questions_exam", columnList = "exam_id, sort_order"),
		@Index(name = "idx_questions_skill", columnList = "skill")
})
@SQLDelete(sql = "UPDATE questions SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Question extends SoftDeletableEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listening_lesson_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ListeningLesson listeningLesson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reading_lesson_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ReadingLesson readingLesson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Exam exam;

	/** Kỹ năng của câu hỏi, dùng tách điểm theo kỹ năng trong bài kiểm tra. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Skill skill;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionKind kind;

	@Column(nullable = false)
	private int sortOrder;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	/** Chỉ được trả về sau khi người dùng nộp bài. */
	@Column(columnDefinition = "TEXT")
	private String explanation;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder")
	private List<QuestionOption> options = new ArrayList<>();

	/** Đáp án chấp nhận được của câu điền từ; Service so khớp không phân biệt hoa/thường. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<String> acceptedAnswers = new ArrayList<>();
}
