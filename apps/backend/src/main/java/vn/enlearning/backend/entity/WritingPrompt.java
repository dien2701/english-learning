package vn.enlearning.backend.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Đề bài Luyện viết. */
@Getter
@Setter
@Entity
@Table(name = "writing_prompts", indexes = {
		@Index(name = "idx_writing_prompts_filter", columnList = "status, level, topic_id"),
		@Index(name = "idx_writing_prompts_topic", columnList = "topic_id"),
		@Index(name = "idx_writing_prompts_title_vi", columnList = "title_vi"),
		@Index(name = "idx_writing_prompts_title_en", columnList = "title_en"),
		@Index(name = "idx_writing_prompts_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE writing_prompts SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WritingPrompt extends ContentEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Column(length = 500)
	private String imageUrl;

	@Column(length = 500)
	private String imageAuthor;

	@Column(length = 500)
	private String imageAuthorUrl;

	/** Yêu cầu đầy đủ của đề bài, bằng tiếng Anh. */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String instructions;

	@Column(nullable = false)
	private int suggestedMinutes = 30;

	@Column(nullable = false)
	private int minWords;

	/** Vài gạch đầu dòng gợi ý hướng triển khai. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<String> hints = new ArrayList<>();
}
