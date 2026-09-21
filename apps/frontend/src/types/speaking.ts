import type { Level } from './common';
import type { L10n } from './l10n';

export interface SpeakingSummary {
  id: string;
  title: L10n;
  description: L10n;
  topicId: string;
  topicName: L10n;
  level: Level;
  /** Số câu cần đọc thành tiếng. */
  promptCount: number;
  isCompleted: boolean;
  lastScore?: number;
}

/** Một câu hoặc đoạn người học phải đọc to. */
export interface SpeakingPrompt {
  id: string;
  order: number;
  text: string;
  /** Phiên âm gợi ý cho cả câu, có thể bỏ trống. */
  phonetic: string | null;
  /** Nghĩa tiếng Việt; chỉ hiện ở chế độ tiếng Việt. */
  meaning: string;
}

export interface SpeakingDetail extends SpeakingSummary {
  prompts: SpeakingPrompt[];
}

/** Điểm từng mặt do AI chấm, tất cả trên thang 10. */
export interface SpeakingScores {
  pronunciation: number;
  vocabulary: number;
  grammar: number;
  fluency: number;
  /** Mức độ bám sát chủ đề. */
  relevance: number;
}

/** Nhận xét cho một câu cụ thể. */
export interface SpeakingPromptFeedback {
  promptId: string;
  /** Câu đề bài. */
  text: string;
  /** Văn bản AI nghe được (âm thanh gốc không được lưu). */
  transcript: string;
  score: number;
  /** Các từ phát âm chưa chuẩn. */
  mispronounced: string[];
  comment: string;
}

/** Một từ đọc chưa chuẩn trong câu mẫu. */
export interface SpeakingWordIssue {
  word: string;
  /** AI nghe thành gì; rỗng nếu đọc thiếu. */
  heardAs: string;
  /** Lỗi cụ thể (âm, trọng âm, âm cuối...). */
  issue: string;
  /** Cách sửa ngắn. */
  tip: string;
}

/** Kết quả chấm một câu ngay sau khi thu (âm thanh gốc không được lưu). */
export interface SpeakingPromptAssessment {
  promptId: string;
  transcript: string;
  score: number;
  wordIssues: SpeakingWordIssue[];
  tips: string[];
}

/** Lượt nói đang thu; `results` là các câu đã chấm (có sẵn khi mở lại lượt dở). */
export interface SpeakingAttempt {
  attemptId: string;
  lessonId: string;
  status: SpeakingStatus;
  results: SpeakingPromptAssessment[];
}

/**
 * IN_PROGRESS: đang thu và chấm từng câu. GRADING: AI đang chấm. FAILED: AI lỗi, không chấm lại được (không lưu âm thanh)
 * nên người học ghi âm lại.
 */
export type SpeakingStatus = 'IN_PROGRESS' | 'GRADING' | 'GRADED' | 'FAILED';

export interface SpeakingResult {
  attemptId: string;
  lessonId: string;
  lessonTitle: L10n;
  status: SpeakingStatus;
  /** Điểm chỉ có khi status là GRADED. */
  overallScore?: number;
  scores?: SpeakingScores;
  /** Những điểm cần cải thiện, viết ngắn gọn. */
  improvements: string[];
  promptFeedback: SpeakingPromptFeedback[];
  submittedAt: string;
}

