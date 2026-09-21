package vn.enlearning.backend.flashcard.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.UserFlashcardProgress;
import vn.enlearning.backend.entity.enums.RecallLevel;

public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, UUID> {

	/** Tiến độ của một bộ thẻ: số thẻ đã đánh giá và số thẻ đã thuộc. */
	interface DeckProgress {
		UUID getDeckId();

		long getRated();

		long getRemembered();
	}

	long countByUserIdAndRecallLevel(UUID userId, RecallLevel recallLevel);

	Optional<UserFlashcardProgress> findByUserIdAndFlashcardId(UUID userId, UUID flashcardId);

	@Query("select p from UserFlashcardProgress p where p.user.id = :userId and p.flashcard.deck.id = :deckId")
	List<UserFlashcardProgress> findByUserAndDeck(UUID userId, UUID deckId);

	@Query("""
			select p.flashcard.deck.id as deckId, count(p) as rated,
			       count(case when p.recallLevel = :remembered then 1 end) as remembered
			from UserFlashcardProgress p
			where p.user.id = :userId and p.flashcard.deck.id in :deckIds
			group by p.flashcard.deck.id""")
	List<DeckProgress> summarize(UUID userId, Collection<UUID> deckIds, RecallLevel remembered);
}
