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
  text: string;
  score: number;
  /** Các từ phát âm chưa chuẩn. */
  mispronounced: string[];
  comment: string;
}

export interface SpeakingResult {
  attemptId: string;
  lessonId: string;
  lessonTitle: L10n;
  overallScore: number;
  scores: SpeakingScores;
  /** Những điểm cần cải thiện, viết ngắn gọn. */
  improvements: string[];
  promptFeedback: SpeakingPromptFeedback[];
  submittedAt: string;
}

