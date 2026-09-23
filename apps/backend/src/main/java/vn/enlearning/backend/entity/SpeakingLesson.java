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

/** Bài Luyện nói: một danh sách câu để đọc to, không dùng ngân hàng câu hỏi. */
@Getter
@Setter
@Entity
@Table(name = "speaking_lessons", indexes = {
		@Index(name = "idx_speaking_lessons_filter", columnList = "status, level, topic_id"),
		@Index(name = "idx_speaking_lessons_topic", columnList = "topic_id"),
		@Index(name = "idx_speaking_lessons_title_vi", columnList = "title_vi"),
		@Index(name = "idx_speaking_lessons_title_en", columnList = "title_en"),
		@Index(name = "idx_speaking_lessons_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE speaking_lessons SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SpeakingLesson extends ContentEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Column(length = 1000)
	private String descriptionVi;

	@Column(length = 1000)
	private String descriptionEn;

	@Column(length = 500)
	private String imageUrl;

	@Column(length = 500)
	private String imageAuthor;

	@Column(length = 500)
	private String imageAuthorUrl;

	/** Các câu cần đọc, lưu dạng JSON; thứ tự mảng là thứ tự hiển thị. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<SpeakingPrompt> prompts = new ArrayList<>();
}
