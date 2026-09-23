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

/** Bộ thẻ từ vựng theo chủ đề. */
@Getter
@Setter
@Entity
@Table(name = "flashcard_decks", indexes = {
		@Index(name = "idx_flashcard_decks_filter", columnList = "status, level, topic_id"),
		@Index(name = "idx_flashcard_decks_topic", columnList = "topic_id"),
		@Index(name = "idx_flashcard_decks_title_vi", columnList = "title_vi"),
		@Index(name = "idx_flashcard_decks_title_en", columnList = "title_en"),
		@Index(name = "idx_flashcard_decks_created_by", columnList = "created_by")
})
@SQLDelete(sql = "UPDATE flashcard_decks SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class FlashcardDeck extends ContentEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private Topic topic;

	@Column(length = 1000)
	private String descriptionVi;

	@Column(length = 1000)
	private String descriptionEn;

	@Column(length = 500)
	private String coverImageUrl;

	@Column(length = 500)
	private String coverImageAuthor;

	@Column(length = 500)
	private String coverImageAuthorUrl;
}
