package vn.enlearning.backend.flashcard.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ContentSort;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.UserFlashcardProgress;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.flashcard.dto.CardResponse;
import vn.enlearning.backend.flashcard.dto.DeckDetailResponse;
import vn.enlearning.backend.flashcard.dto.DeckSummaryResponse;
import vn.enlearning.backend.flashcard.dto.FinishRequest;
import vn.enlearning.backend.flashcard.dto.ProgressResponse;
import vn.enlearning.backend.flashcard.dto.RecallRequest;
import vn.enlearning.backend.flashcard.dto.StudyResultResponse;
import vn.enlearning.backend.flashcard.dto.StudyResultResponse.ReviewWord;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository.DeckProgress;

/**
 * Danh sách và học bộ thẻ. Trạng thái bộ thẻ suy ra từ {@code user_flashcard_progress}: chưa có dòng nào là
 * NOT_STARTED, mọi thẻ đều ở mức REMEMBERED là COMPLETED, còn lại là IN_PROGRESS. {@code learnedCards} là số thẻ
 * REMEMBERED. Người học chỉ thấy bộ ACTIVE.
 */
@Service
@RequiredArgsConstructor
public class FlashcardService {

	static final String NOT_STARTED = "NOT_STARTED";
	static final String IN_PROGRESS = "IN_PROGRESS";
	static final String COMPLETED = "COMPLETED";
	private static final Set<String> STATUSES = Set.of(NOT_STARTED, IN_PROGRESS, COMPLETED);

	private final FlashcardDeckRepository decks;
	private final FlashcardRepository cards;
	private final UserFlashcardProgressRepository progress;
	private final UserRepository users;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<DeckSummaryResponse> listDecks(UUID userId, String search, UUID topicId, String level,
			String status, String sort, int page, int pageSize) {
		Level levelFilter = parseLevel(level);
		String statusFilter = parseStatus(status);

		List<FlashcardDeck> found = decks.findAll(filter(search, topicId, levelFilter), ContentSort.resolve(sort));
		List<DeckSummaryResponse> all = summarize(userId, found).stream()
				.filter(d -> statusFilter == null || statusFilter.equals(d.status()))
				.toList();
		return PageResponse.of(all, page, pageSize);
	}

	@Transactional(readOnly = true)
	public DeckDetailResponse getDeck(UUID userId, UUID deckId) {
		FlashcardDeck deck = activeDeck(deckId);
		Map<UUID, RecallLevel> levels = levelsByCard(userId, deckId);
		List<CardResponse> body = cards.findByDeckIdOrderBySortOrderAscIdAsc(deckId).stream()
				.map(c -> toCard(c, levels.get(c.getId())))
				.toList();
		return DeckDetailResponse.of(summarize(userId, List.of(deck)).get(0), body);
	}

	@Transactional
	public ProgressResponse saveRecall(UUID userId, UUID deckId, RecallRequest request) {
		FlashcardDeck deck = activeDeck(deckId);
		Flashcard card = cards.findById(request.flashcardId())
				.filter(c -> c.getDeck().getId().equals(deck.getId()))
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

		Instant now = clock.instant();
		UserFlashcardProgress row = progress.findByUserIdAndFlashcardId(userId, card.getId()).orElse(null);
		int previousInterval = row == null ? 0 : row.getIntervalDays();
		ReviewScheduler.Schedule schedule = ReviewScheduler.next(request.recallLevel(), previousInterval, now);
		if (row == null) {
			row = new UserFlashcardProgress();
			row.setUser(users.getReferenceById(userId));
			row.setFlashcard(card);
			row.setReviewCount(1);
		} else {
			row.setReviewCount(row.getReviewCount() + 1);
		}
		row.setRecallLevel(request.recallLevel());
		row.setIntervalDays(schedule.intervalDays());
		row.setLastReviewedAt(now);
		row.setNextReviewAt(schedule.nextReviewAt());
		progress.saveAndFlush(row);

		DeckSummaryResponse summary = summarize(userId, List.of(deck)).get(0);
		return new ProgressResponse(now, summary.learnedCards(), summary.progressPercent());
	}

	@Transactional(readOnly = true)
	public StudyResultResponse finish(UUID userId, UUID deckId, FinishRequest request) {
		FlashcardDeck deck = activeDeck(deckId);
		List<Flashcard> deckCards = cards.findByDeckIdOrderBySortOrderAscIdAsc(deckId);
		Set<UUID> deckCardIds = deckCards.stream().map(Flashcard::getId).collect(Collectors.toSet());

		Set<UUID> requested = request == null || request.studiedIds() == null ? Set.of()
				: new HashSet<>(request.studiedIds());
		if (!deckCardIds.containsAll(requested)) {
			throw new ApiException(ErrorCode.VALIDATION);
		}

		Map<UUID, RecallLevel> levels = levelsByCard(userId, deckId);
		List<Flashcard> studied = deckCards.stream()
				.filter(c -> levels.containsKey(c.getId()) && (requested.isEmpty() || requested.contains(c.getId())))
				.toList();

		List<ReviewWord> toReview = new ArrayList<>();
		for (Flashcard c : studied) {
			RecallLevel level = levels.get(c.getId());
			if (level != RecallLevel.REMEMBERED) {
				toReview.add(new ReviewWord(c.getId(), c.getWord(), c.getPhonetic(),
						L10n.of(c.getMeaningVi(), c.getMeaningEn()), c.getImageUrl(), level));
			}
		}
		int total = deckCards.size();
		return new StudyResultResponse(deck.getId(), L10n.of(deck.getTitleVi(), deck.getTitleEn()), studied.size(),
				total, percent(studied.size(), total), count(studied, levels, RecallLevel.REMEMBERED),
				count(studied, levels, RecallLevel.ALMOST_REMEMBERED),
				count(studied, levels, RecallLevel.NOT_REMEMBERED), toReview);
	}

	// --- nội bộ ---------------------------------------------------------------------------------

	private FlashcardDeck activeDeck(UUID deckId) {
		return decks.findByIdAndStatus(deckId, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private Map<UUID, RecallLevel> levelsByCard(UUID userId, UUID deckId) {
		return progress.findByUserAndDeck(userId, deckId).stream()
				.collect(Collectors.toMap(p -> p.getFlashcard().getId(), UserFlashcardProgress::getRecallLevel));
	}

	private static Specification<FlashcardDeck> filter(String search, UUID topicId, Level level) {
		return (root, query, cb) -> {
			root.fetch("topic", JoinType.INNER);
			List<Predicate> where = new ArrayList<>();
			where.add(cb.equal(root.get("status"), ContentStatus.ACTIVE));
			if (topicId != null) {
				where.add(cb.equal(root.get("topic").get("id"), topicId));
			}
			if (level != null) {
				where.add(cb.equal(root.get("level"), level));
			}
			if (search != null && !search.isBlank()) {
				String like = "%" + search.trim().toLowerCase(Locale.ROOT)
						.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
				where.add(cb.or(
						cb.like(cb.lower(root.get("titleVi")), like, '\\'),
						cb.like(cb.lower(root.get("titleEn")), like, '\\'),
						cb.like(cb.lower(root.get("descriptionVi")), like, '\\'),
						cb.like(cb.lower(root.get("descriptionEn")), like, '\\')));
			}
			return cb.and(where.toArray(Predicate[]::new));
		};
	}

	private List<DeckSummaryResponse> summarize(UUID userId, List<FlashcardDeck> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		Collection<UUID> ids = found.stream().map(FlashcardDeck::getId).toList();
		Map<UUID, Long> totals = cards.countByDeckIds(ids).stream()
				.collect(Collectors.toMap(FlashcardRepository.DeckCount::getDeckId,
						FlashcardRepository.DeckCount::getTotal));
		Map<UUID, DeckProgress> done = progress.summarize(userId, ids, RecallLevel.REMEMBERED).stream()
				.collect(Collectors.toMap(DeckProgress::getDeckId, Function.identity()));

		return found.stream().map(d -> {
			long total = totals.getOrDefault(d.getId(), 0L);
			DeckProgress p = done.get(d.getId());
			long rated = p == null ? 0 : p.getRated();
			long learned = p == null ? 0 : p.getRemembered();
			String status = rated == 0 ? NOT_STARTED : total > 0 && learned >= total ? COMPLETED : IN_PROGRESS;
			return new DeckSummaryResponse(d.getId(), L10n.of(d.getTitleVi(), d.getTitleEn()),
					L10n.of(d.getDescriptionVi() == null ? "" : d.getDescriptionVi(), d.getDescriptionEn()),
					d.getCoverImageUrl(), d.getCoverImageAuthor(), d.getCoverImageAuthorUrl(), d.getTopic().getId(),
					L10n.of(d.getTopic().getNameVi(), d.getTopic().getNameEn()), d.getLevel(), total, learned,
					percent(learned, total), status);
		}).toList();
	}

	private static CardResponse toCard(Flashcard c, RecallLevel level) {
		L10n partOfSpeech = c.getPartOfSpeechVi() == null ? null
				: L10n.of(c.getPartOfSpeechVi(), c.getPartOfSpeechEn());
		return new CardResponse(c.getId(), c.getWord(), c.getPhonetic(), L10n.of(c.getMeaningVi(), c.getMeaningEn()),
				partOfSpeech, c.getExample(), c.getExampleMeaning(), c.getImageUrl(), c.getImageAuthor(),
				c.getImageAuthorUrl(), c.getAudioUrl(), level);
	}

	private static int count(List<Flashcard> studied, Map<UUID, RecallLevel> levels, RecallLevel level) {
		return (int) studied.stream().filter(c -> levels.get(c.getId()) == level).count();
	}

	private static int percent(long part, long total) {
		return total == 0 ? 0 : (int) Math.round(part * 100.0 / total);
	}

	private static Level parseLevel(String level) {
		if (level == null || level.isBlank()) {
			return null;
		}
		try {
			return Level.valueOf(level.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
	}

	private static String parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		String value = status.trim().toUpperCase(Locale.ROOT);
		if (!STATUSES.contains(value)) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
		return value;
	}
}
