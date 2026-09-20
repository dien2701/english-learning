import type { Level } from './common';
import type { L10n } from './l10n';

export type WritingStatus =
  | 'NOT_STARTED'
  | 'DRAFT'
  /** Đã nộp, AI đang chấm. */
  | 'GRADING'
  | 'GRADED'
  /** AI không trả kết quả — bài vẫn được giữ để chấm lại. */
  | 'NEEDS_RETRY';

export interface WritingPromptSummary {
  id: string;
  title: L10n;
  topicId: string;
  topicName: L10n;
  level: Level;
  /** Thời lượng gợi ý, tính bằng phút. */
  suggestedMinutes: number;
  minWords: number;
  status: WritingStatus;
  lastScore?: number;
}

export interface WritingPromptDetail extends WritingPromptSummary {
  /** Yêu cầu đầy đủ của đề bài. */
  prompt: string;
  /** Vài gạch đầu dòng gợi ý hướng triển khai. */
  hints: string[];
}

/** Một lỗi AI phát hiện trong bài viết. */
export interface WritingIssue {
  id: string;
  category: 'GRAMMAR' | 'VOCABULARY' | 'EXPRESSION';
  /** Đoạn văn bản có vấn đề, trích từ bài của người học. */
  excerpt: string;
  /** Giải thích lỗi. */
  problem: string;
  /** Cách viết lại cho đúng. */
  suggestion: string;
}

/** Phản hồi AI trả về cho một bài viết. */
export interface WritingFeedback {
  /** Điểm tổng trên thang 10. */
  overallScore: number;
  grammarScore: number;
  vocabularyScore: number;
  expressionScore: number;
  /** Nhận xét tổng quan, viết cho người học đọc. */
  summary: string;
  issues: WritingIssue[];
}

export interface WritingSubmission {
  id: string;
  promptId: string;
  promptTitle: L10n;
  content: string;
  wordCount: number;
  status: WritingStatus;
  submittedAt: string;
  /** Chỉ có khi status là GRADED. */
  feedback?: WritingFeedback;
}
