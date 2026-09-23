import type { Level } from './common';
import type { L10n } from './l10n';

/** Mức độ ghi nhớ người học tự đánh giá sau khi xem một thẻ. */
export type RecallLevel = 'NOT_REMEMBERED' | 'ALMOST_REMEMBERED' | 'REMEMBERED';

export type DeckStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';

/** Một bộ thẻ trong danh sách. */
export interface DeckSummary {
  id: string;
  title: L10n;
  description: L10n;
  coverImageUrl: string | null;
  coverImageAuthor: string | null;
  coverImageAuthorUrl: string | null;
  topicId: string;
  topicName: L10n;
  level: Level;
  totalCards: number;
  learnedCards: number;
  progressPercent: number;
  status: DeckStatus;
}

/**
 * Nội dung đầy đủ của một thẻ từ vựng.
 *
 * `word` và `example` luôn bằng tiếng Anh vì đó chính là thứ đang học.
 * `meaning` có hai bản: tiếng Việt cho người mới, và định nghĩa bằng
 * tiếng Anh cho người học ở chế độ EN.
 */
export interface Flashcard {
  id: string;
  word: string;
  phonetic: string | null;
  meaning: L10n;
  partOfSpeech: L10n | null;
  example: string | null;
  /** Bản dịch câu ví dụ; chỉ hiển thị ở chế độ tiếng Việt. */
  exampleMeaning: string | null;
  /** Ảnh minh hoạ cho từ. Hỏng hoặc thiếu thì giao diện hiện khối dự phòng. */
  imageUrl: string | null;
  imageAuthor: string | null;
  imageAuthorUrl: string | null;
  /** Tệp phát âm; để trống thì dùng giọng đọc của trình duyệt. */
  audioUrl: string | null;
  recallLevel: RecallLevel | null;
}

/** Bộ thẻ kèm toàn bộ thẻ bên trong. */
export interface DeckDetail extends DeckSummary {
  cards: Flashcard[];
}

/** Một lượt đánh giá gửi lên khi học. */
export interface RecallSubmission {
  flashcardId: string;
  recallLevel: RecallLevel;
}

/** Kết quả sau khi kết thúc phiên học. */
export interface StudyResult {
  deckId: string;
  deckTitle: L10n;
  /** Số thẻ đã xem trong phiên này. */
  studiedCards: number;
  totalCards: number;
  completionPercent: number;
  remembered: number;
  almostRemembered: number;
  notRemembered: number;
  /** Các từ nên ôn lại, gồm nhóm chưa nhớ và gần nhớ. */
  wordsToReview: Array<{
    id: string;
    word: string;
    phonetic: string | null;
    meaning: L10n;
    imageUrl: string | null;
    recallLevel: RecallLevel;
  }>;
}
