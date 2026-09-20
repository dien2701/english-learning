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

/** Bài Luyện đọc. Câu hỏi nằm ở {@link Question#getReadingLesson()}. */
@Getter
@Setter
@Entity
@Table(name = "reading_lessons", indexes = {
		@Index(name = "idx_reading_lessons_filter", columnList = "status, level, topic_id"),
		@Index(name = "idx_reading_lessons_topic", columnList = "topic_id"),
		@Index(name = "idx_reading_lessons_title_vi", columnList = "title_vi"),
		@Index(name = "idx_reading_lessons_title_en", columnList = "title_en"),
		@Index(name = "idx_reading_lessons_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE reading_lessons SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ReadingLesson extends ContentEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Column(length = 1000)
	private String descriptionVi;

	@Column(length = 1000)
	private String descriptionEn;

	/** Backend tính lại mỗi lần lưu đoạn văn. */
	@Column(nullable = false)
	private int wordCount;

	/** 0 nghĩa là không giới hạn thời gian. */
	@Column(nullable = false)
	private int timeLimitMinutes;

	/** Nội dung bài đọc, mỗi phần tử là một đoạn văn. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<String> paragraphs = new ArrayList<>();
}
