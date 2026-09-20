package vn.enlearning.backend.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Bài Luyện nghe. Câu hỏi nằm ở {@link Question#getListeningLesson()}. */
@Getter
@Setter
@Entity
@Table(name = "listening_lessons", indexes = {
		@Index(name = "idx_listening_lessons_filter", columnList = "status, level, topic_id"),
		@Index(name = "idx_listening_lessons_topic", columnList = "topic_id"),
		@Index(name = "idx_listening_lessons_title_vi", columnList = "title_vi"),
		@Index(name = "idx_listening_lessons_title_en", columnList = "title_en"),
		@Index(name = "idx_listening_lessons_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE listening_lessons SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ListeningLesson extends ContentEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Column(length = 1000)
	private String descriptionVi;

	@Column(length = 1000)
	private String descriptionEn;

	/** Tệp MP3 trên Cloudinary; trống thì đọc transcript bằng TTS. */
	@Column(length = 500)
	private String audioUrl;

	@Column(nullable = false)
	private int durationSeconds;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String transcript;
}
