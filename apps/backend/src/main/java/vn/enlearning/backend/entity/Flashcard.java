package vn.enlearning.backend.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

/** Một từ vựng trong bộ thẻ. Từ và câu ví dụ luôn bằng tiếng Anh. */
@Getter
@Setter
@Entity
@Table(name = "flashcards", indexes = {
		@Index(name = "idx_flashcards_deck_order", columnList = "deck_id, sort_order"),
		@Index(name = "idx_flashcards_word", columnList = "word")
})
@SQLDelete(sql = "UPDATE flashcards SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Flashcard extends SoftDeletableEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "deck_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private FlashcardDeck deck;

	@Column(nullable = false, length = 100)
	private String word;

	@Column(length = 100)
	private String phonetic;

	@Column(nullable = false, length = 500)
	private String meaningVi;

	@Column(length = 500)
	private String meaningEn;

	@Column(length = 50)
	private String partOfSpeechVi;

	@Column(length = 50)
	private String partOfSpeechEn;

	@Column(length = 500)
	private String example;

	/** Bản dịch câu ví dụ, chỉ hiện ở chế độ tiếng Việt. */
	@Column(length = 500)
	private String exampleMeaning;

	@Column(length = 500)
	private String imageUrl;

	@Column(length = 500)
	private String imageAuthor;

	@Column(length = 500)
	private String imageAuthorUrl;

	/** Trống thì giao diện dùng giọng đọc của trình duyệt. */
	@Column(length = 500)
	private String audioUrl;

	@Column(nullable = false)
	private int sortOrder;
}
