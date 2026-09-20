import { http } from '../shared/api/client';
import type { Page } from '../shared/api/types';
import type {
  DeckDetail,
  DeckSummary,
  RecallSubmission,
  StudyResult,
} from '../types/flashcard';
import type {
  AnswerSubmission,
  AttemptHistoryItem,
  ExamDetail,
  ExamResult,
  ExamSummary,
  ListeningDetail,
  ListeningSummary,
  PracticeResult,
  ReadingDetail,
  ReadingSummary,
  Topic,
} from '../types/practice';
import type {
  WritingPromptDetail,
  WritingPromptSummary,
  WritingSubmission,
} from '../types/writing';
import type { SpeakingDetail, SpeakingResult, SpeakingSummary } from '../types/speaking';

/** Bộ lọc dùng chung cho các trang danh sách. */
export interface ListQuery {
  search?: string;
  topicId?: string;
  level?: string;
  status?: string;
  page?: number;
  pageSize?: number;
}

/** Bỏ các khoá rỗng để đường dẫn không bị nhét tham số thừa. */
function clean(query: ListQuery): Record<string, string | number> {
  const result: Record<string, string | number> = {};
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== '' && value !== null) result[key] = value;
  }
  return result;
}

export const topicService = {
  list: (): Promise<Topic[]> => http.get<Topic[]>('/topics'),
};

/* --- Flashcard ------------------------------------------------------ */

export const flashcardService = {
  listDecks: (query: ListQuery = {}): Promise<Page<DeckSummary>> =>
    http.get<Page<DeckSummary>>('/flashcard/decks', { params: clean(query) }),

  getDeck: (id: string): Promise<DeckDetail> =>
    http.get<DeckDetail>(`/flashcard/decks/${id}`),

  /** Chỉ gửi id thẻ và mức độ nhớ; backend tự tính ngày ôn tiếp theo. */
  saveRecall: (deckId: string, payload: RecallSubmission) =>
    http.post<{ savedAt: string; learnedCards: number; progressPercent: number }>(
      `/flashcard/decks/${deckId}/progress`,
      payload,
    ),

  finishSession: (deckId: string, studiedIds: string[]): Promise<StudyResult> =>
    http.post<StudyResult>(`/flashcard/decks/${deckId}/finish`, { studiedIds }),
};

/* --- Luyện viết ----------------------------------------------------- */

export const writingService = {
  listPrompts: (query: ListQuery = {}): Promise<Page<WritingPromptSummary>> =>
    http.get<Page<WritingPromptSummary>>('/writing/prompts', { params: clean(query) }),

  getPrompt: (id: string): Promise<WritingPromptDetail> =>
    http.get<WritingPromptDetail>(`/writing/prompts/${id}`),

  submit: (promptId: string, content: string): Promise<WritingSubmission> =>
    http.post<WritingSubmission>(`/writing/prompts/${promptId}/submit`, { content }),

  /** Hỏi lại trạng thái bài viết trong lúc AI đang chấm. */
  getSubmission: (id: string): Promise<WritingSubmission> =>
    http.get<WritingSubmission>(`/writing/submissions/${id}`),

  regrade: (id: string): Promise<WritingSubmission> =>
    http.post<WritingSubmission>(`/writing/submissions/${id}/regrade`),

  history: (page = 1): Promise<Page<WritingSubmission>> =>
    http.get<Page<WritingSubmission>>('/writing/submissions', { params: { page } }),
};

/* --- Luyện nghe ----------------------------------------------------- */

export const listeningService = {
  list: (query: ListQuery = {}): Promise<Page<ListeningSummary>> =>
    http.get<Page<ListeningSummary>>('/listening/lessons', { params: clean(query) }),

  get: (id: string): Promise<ListeningDetail> =>
    http.get<ListeningDetail>(`/listening/lessons/${id}`),

  submit: (
    id: string,
    answers: AnswerSubmission[],
    durationSeconds: number,
  ): Promise<PracticeResult> =>
    http.post<PracticeResult>(`/listening/lessons/${id}/submit`, {
      answers,
      durationSeconds,
    }),
};

/* --- Luyện đọc ------------------------------------------------------ */

export const readingService = {
  list: (query: ListQuery = {}): Promise<Page<ReadingSummary>> =>
    http.get<Page<ReadingSummary>>('/reading/lessons', { params: clean(query) }),

  get: (id: string): Promise<ReadingDetail> =>
    http.get<ReadingDetail>(`/reading/lessons/${id}`),

  submit: (
    id: string,
    answers: AnswerSubmission[],
    durationSeconds: number,
  ): Promise<PracticeResult> =>
    http.post<PracticeResult>(`/reading/lessons/${id}/submit`, {
      answers,
      durationSeconds,
    }),
};

/* --- Luyện nói ------------------------------------------------------ */

export const speakingService = {
  list: (query: ListQuery = {}): Promise<Page<SpeakingSummary>> =>
    http.get<Page<SpeakingSummary>>('/speaking/lessons', { params: clean(query) }),

  get: (id: string): Promise<SpeakingDetail> =>
    http.get<SpeakingDetail>(`/speaking/lessons/${id}`),

  submit: (
    id: string,
    recordedPromptIds: string[],
    totalDurationSeconds: number,
  ): Promise<SpeakingResult> =>
    http.post<SpeakingResult>(`/speaking/lessons/${id}/submit`, {
      recordedPromptIds,
      totalDurationSeconds,
    }),

  getResult: (attemptId: string): Promise<SpeakingResult> =>
    http.get<SpeakingResult>(`/speaking/results/${attemptId}`),
};

/* --- Bài kiểm tra --------------------------------------------------- */

export const examService = {
  list: (query: ListQuery = {}): Promise<Page<ExamSummary>> =>
    http.get<Page<ExamSummary>>('/exams', { params: clean(query) }),

  get: (id: string): Promise<ExamDetail> => http.get<ExamDetail>(`/exams/${id}`),

  submit: (
    id: string,
    answers: AnswerSubmission[],
    durationSeconds: number,
  ): Promise<ExamResult> =>
    http.post<ExamResult>(`/exams/${id}/submit`, { answers, durationSeconds }),
};

/* --- Kết quả và lịch sử dùng chung ---------------------------------- */

export const attemptService = {
  get: (attemptId: string): Promise<PracticeResult | ExamResult> =>
    http.get<PracticeResult | ExamResult>(`/attempts/${attemptId}`),

  history: (skill?: string, page = 1): Promise<Page<AttemptHistoryItem>> =>
    http.get<Page<AttemptHistoryItem>>('/attempts', {
      // Bỏ hẳn khoá skill khi không lọc, để backend không nhận chuỗi rỗng.
      params: skill ? { skill, page } : { page },
    }),
};
