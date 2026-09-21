package vn.enlearning.backend.content.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.Flashcard;

public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

	interface DeckCount {
		UUID getDeckId();

		long getTotal();
	}

	List<Flashcard> findByDeckIdOrderBySortOrderAscIdAsc(UUID deckId);

	@Query("select f.deck.id as deckId, count(f) as total from Flashcard f where f.deck.id in :deckIds group by f.deck.id")
	List<DeckCount> countByDeckIds(Collection<UUID> deckIds);
}
