package vn.enlearning.backend.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Phản hồi AI cho một bài viết (1-1 với {@link WritingSubmission}); điểm thang 10. */
@Getter
@Setter
@Entity
@Table(name = "ai_feedbacks",
		uniqueConstraints = @UniqueConstraint(name = "uq_ai_feedbacks_submission", columnNames = "submission_id"))
public class AiFeedback extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submission_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private WritingSubmission submission;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal overallScore;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal grammarScore;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal vocabularyScore;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal expressionScore;

	/** Nhận xét tổng quan, viết cho người học đọc. */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String summary;

	/** Mô hình AI đã chấm, phục vụ truy vết. */
	@Column(length = 100)
	private String modelName;

	/** Danh sách lỗi phát hiện, lưu dạng JSON; thứ tự mảng là thứ tự hiển thị. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<AiFeedbackIssue> issues = new ArrayList<>();
}
