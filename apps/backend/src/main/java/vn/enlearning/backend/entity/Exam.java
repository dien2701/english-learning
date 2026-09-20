package vn.enlearning.backend.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Đề kiểm tra tổng hợp nhiều kỹ năng. Câu hỏi nằm ở
 * {@link Question#getExam()}; các kỹ năng bao phủ suy ra từ câu hỏi.
 */
@Getter
@Setter
@Entity
@Table(name = "exams", indexes = {
		@Index(name = "idx_exams_filter", columnList = "status, level"),
		@Index(name = "idx_exams_title_vi", columnList = "title_vi"),
		@Index(name = "idx_exams_title_en", columnList = "title_en"),
		@Index(name = "idx_exams_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE exams SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Exam extends ContentEntity {

	@Column(length = 1000)
	private String descriptionVi;

	@Column(length = 1000)
	private String descriptionEn;

	/** 0 nghĩa là không giới hạn thời gian. */
	@Column(nullable = false)
	private int timeLimitMinutes;
}
