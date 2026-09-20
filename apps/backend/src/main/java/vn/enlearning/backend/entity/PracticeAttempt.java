package vn.enlearning.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.AttemptStatus;

/**
 * Một lượt làm bài Nghe, Đọc hoặc Kiểm tra (ListeningAttempt, ReadingAttempt,
 * ExamAttempt gộp một bảng). Đúng một trong ba cột {@code listeningLesson},
 * {@code readingLesson}, {@code exam} có giá trị; CHECK ở DB bảo đảm điều đó.
 * Ba FK dùng RESTRICT nên nội dung đã có lượt làm không thể bị xoá.
 */
@Getter
@Setter
@Entity
@Table(name = "practice_attempts", indexes = {
		@Index(name = "idx_practice_attempts_user_submitted", columnList = "user_id, submitted_at"),
		@Index(name = "idx_practice_attempts_user_status", columnList = "user_id, status"),
		@Index(name = "idx_practice_attempts_listening", columnList = "listening_lesson_id, user_id"),
		@Index(name = "idx_practice_attempts_reading", columnList = "reading_lesson_id, user_id"),
		@Index(name = "idx_practice_attempts_exam", columnList = "exam_id, user_id")
})
public class PracticeAttempt extends AuditedEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listening_lesson_id")
	private ListeningLesson listeningLesson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reading_lesson_id")
	private ReadingLesson readingLesson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id")
	private Exam exam;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AttemptStatus status = AttemptStatus.IN_PROGRESS;

	@Column(nullable = false)
	private Instant startedAt;

	private Instant submittedAt;

	private Integer durationSeconds;

	private Integer correctCount;

	private Integer totalQuestions;

	/** Thang 10; trống khi chưa nộp bài. */
	@Column(precision = 3, scale = 1)
	private BigDecimal score;

	@OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PracticeAttemptAnswer> answers = new ArrayList<>();
}
