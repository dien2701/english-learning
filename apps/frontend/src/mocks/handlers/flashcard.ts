/** Mock cho module Flashcard (chức năng 3). */

import { fail, get, matches, num, paginate, post, searchable } from '../router';
import { decks, findDeck } from '../data/flashcards';
import { topics } from '../data/topics';
import type {
  DeckDetail,
  DeckSummary,
  RecallSubmission,
  StudyResult,
} from '../../types/flashcard';
import type { Topic } from '../../types/practice';

/** Bỏ mảng thẻ để danh sách không phải tải cả nội dung từng bộ. */
function toSummary(deck: DeckDetail): DeckSummary {
  const { cards: _cards, ...summary } = deck;
  return summary;
}

/* --- GET /topics ---------------------------------------------------- */

get('/topics', (): Topic[] =>
  topics.map((t) => ({
    ...t,
    itemCount: decks.filter((d) => d.topicId === t.id).length,
  })),
);

/* --- GET /flashcard/decks ------------------------------------------- */

get('/flashcard/decks', ({ query }) => {
  const search = query.get('search') ?? '';
  const topicId = query.get('topicId') ?? '';
  const level = query.get('level') ?? '';

  const filtered = decks.filter((deck) => {
    if (topicId && deck.topicId !== topicId) return false;
    if (level && deck.level !== level) return false;
    if (search && !matches(searchable(deck.title, deck.description), search)) return false;
    return true;
  });

  return paginate(filtered.map(toSummary), num(query, 'page', 1), num(query, 'pageSize', 12));
});

/* --- GET /flashcard/decks/:id --------------------------------------- */

get('/flashcard/decks/:id', ({ params }): DeckDetail => {
  const deck = findDeck(params.id);
  if (!deck) fail(404, 'errors.deckNotFound', 'DECK_NOT_FOUND');
  return deck;
});

/* --- POST /flashcard/decks/:id/progress ------------------------------
 * Giao diện chỉ gửi id thẻ và mức độ nhớ, đúng như quy ước trong
 * ARCHITECTURE.md. Việc tính ngày ôn tiếp theo thuộc về backend.
 * -------------------------------------------------------------------- */

post('/flashcard/decks/:id/progress', ({ params, body }) => {
  const deck = findDeck(params.id);
  if (!deck) fail(404, 'errors.deckNotFound', 'DECK_NOT_FOUND');

  const { flashcardId, recallLevel } = (body ?? {}) as Partial<RecallSubmission>;
  if (!flashcardId || !recallLevel) {
    fail(400, 'errors.missingCardInfo', 'VALIDATION');
  }

  const card = deck.cards.find((c) => c.id === flashcardId);
  if (!card) fail(404, 'errors.cardNotFound', 'CARD_NOT_FOUND');

  card.recallLevel = recallLevel;

  // Coi như đã học khi người dùng đánh giá lần đầu.
  const learned = deck.cards.filter((c) => c.recallLevel !== null).length;
  deck.learnedCards = learned;
  deck.progressPercent = Math.round((learned / deck.totalCards) * 100);
  deck.status =
    learned === 0 ? 'NOT_STARTED' : learned >= deck.totalCards ? 'COMPLETED' : 'IN_PROGRESS';

  return {
    savedAt: new Date().toISOString(),
    learnedCards: deck.learnedCards,
    progressPercent: deck.progressPercent,
  };
});

/* --- POST /flashcard/decks/:id/finish -------------------------------- */

post('/flashcard/decks/:id/finish', ({ params, body }): StudyResult => {
  const deck = findDeck(params.id);
  if (!deck) fail(404, 'errors.deckNotFound', 'DECK_NOT_FOUND');

  const { studiedIds } = (body ?? {}) as { studiedIds?: string[] };
  const studied = deck.cards.filter((c) =>
    studiedIds ? studiedIds.includes(c.id) : c.recallLevel !== null,
  );

  const count = (level: string) =>
    studied.filter((c) => c.recallLevel === level).length;

  return {
    deckId: deck.id,
    deckTitle: deck.title,
    studiedCards: studied.length,
    totalCards: deck.totalCards,
    completionPercent:
      deck.totalCards === 0 ? 0 : Math.round((studied.length / deck.totalCards) * 100),
    remembered: count('REMEMBERED'),
    almostRemembered: count('ALMOST_REMEMBERED'),
    notRemembered: count('NOT_REMEMBERED'),
    wordsToReview: studied
      .filter(
        (c) =>
          c.recallLevel === 'NOT_REMEMBERED' || c.recallLevel === 'ALMOST_REMEMBERED',
      )
      .map((c) => ({
        id: c.id,
        word: c.word,
        phonetic: c.phonetic,
        meaning: c.meaning,
        imageUrl: c.imageUrl,
        recallLevel: c.recallLevel!,
      })),
  };
});
