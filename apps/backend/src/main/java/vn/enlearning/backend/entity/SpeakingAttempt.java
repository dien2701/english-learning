package vn.enlearning.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

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
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;

/**
 * Một lần nộp bài Luyện nói kèm kết quả AI chấm. Âm thanh không được lưu: backend
 * chuyển thành văn bản, chấm rồi bỏ âm thanh, DB chỉ giữ transcript, điểm và nhận
 * xét. Các điểm để NULL khi chưa chấm xong ({@code GRADING}) hoặc chấm hỏng
 * ({@code FAILED}); CHECK ở DB buộc đủ điểm khi {@code GRADED}. Điểm thang 10.
 */
@Getter
@Setter
@Entity
@Table(name = "speaking_attempts", indexes = {
		@Index(name = "idx_speaking_attempts_user", columnList = "user_id, submitted_at"),
		@Index(name = "idx_speaking_attempts_user_lesson", columnList = "user_id, lesson_id, status"),
		@Index(name = "idx_speaking_attempts_lesson", columnList = "lesson_id"),
		@Index(name = "idx_speaking_attempts_status", columnList = "status, updated_at")
})
public class SpeakingAttempt extends AuditedEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/** RESTRICT: bài đã có lượt nói thì không xoá được. */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "lesson_id", nullable = false)
	private SpeakingLesson lesson;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SpeakingAttemptStatus status = SpeakingAttemptStatus.GRADING;

	@Column(nullable = false)
	private Instant startedAt;

	@Column(nullable = false)
	private Instant submittedAt;

	@Column(nullable = false)
	private int durationSeconds;

	@Column(precision = 3, scale = 1)
	private BigDecimal overallScore;

	@Column(precision = 3, scale = 1)
	private BigDecimal pronunciationScore;

	@Column(precision = 3, scale = 1)
	private BigDecimal vocabularyScore;

	@Column(precision = 3, scale = 1)
	private BigDecimal grammarScore;

	@Column(precision = 3, scale = 1)
	private BigDecimal fluencyScore;

	/** Mức độ bám sát chủ đề. */
	@Column(precision = 3, scale = 1)
	private BigDecimal relevanceScore;

	/** Những điểm cần cải thiện, viết ngắn gọn, lưu dạng JSON. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<String> improvements = new ArrayList<>();

	/** Nhận xét cho từng câu đã đọc, lưu dạng JSON. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<SpeakingPromptFeedback> promptFeedback = new ArrayList<>();

	/** Mô hình AI đã chấm, phục vụ truy vết. */
	@Column(length = 100)
	private String modelName;
}
