package vn.enlearning.backend.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Kết quả chấm một câu trong lượt nói {@code IN_PROGRESS}. Âm thanh không được lưu: chỉ giữ transcript, điểm
 * (thang 10) và nhận xét. Thu lại câu thì ghi đè dòng cũ.
 */
@Getter
@Setter
@Entity
@Table(name = "speaking_prompt_results", uniqueConstraints = @UniqueConstraint(
		name = "uq_speaking_prompt_results_attempt_prompt", columnNames = { "attempt_id", "prompt_id" }))
public class SpeakingPromptResult extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attempt_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SpeakingAttempt attempt;

	/** Câu trong {@link SpeakingLesson#getPrompts()}. */
	@Column(nullable = false)
	private UUID promptId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String transcript;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal score;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<SpeakingWordIssue> wordIssues = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<String> tips = new ArrayList<>();

	@Column(length = 100)
	private String modelName;
}
