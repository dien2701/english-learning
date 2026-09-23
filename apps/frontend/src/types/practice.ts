import type { Level, Skill } from './common';
import type { L10n } from './l10n';

/** Chủ đề nội dung, dùng chung cho mọi kỹ năng. */
export interface Topic {
  id: string;
  name: L10n;
  /** Số nội dung đang thuộc chủ đề này. */
  itemCount?: number;
  imageUrl?: string | null;
  /** Ảnh từ Unsplash: tên và link tác giả để ghi công. */
  imageAuthor?: string | null;
  imageAuthorUrl?: string | null;
}

export type QuestionKind = 'SINGLE_CHOICE' | 'FILL_BLANK';

export interface QuestionOption {
  id: string;
  text: string;
}

/**
 * Câu hỏi dùng chung cho Nghe, Đọc và Bài kiểm tra.
 *
 * Khi đang làm bài, `correctOptionId`, `correctText` và `explanation` luôn
 * là undefined — backend không trả đáp án trước lúc nộp. Sau khi nộp thì
 * các trường này mới có giá trị.
 */
export interface Question {
  id: string;
  kind: QuestionKind;
  order: number;
  text: string;
  /** Chỉ có với câu trắc nghiệm. */
  options?: QuestionOption[];
  correctOptionId?: string;
  correctText?: string;
  explanation?: string;
}

/** Câu trả lời người dùng gửi lên. */
export interface AnswerSubmission {
  questionId: string;
  /** Với câu trắc nghiệm. */
  optionId?: string;
  /** Với câu điền từ. */
  text?: string;
}

/** Kết quả chấm của một câu. */
export interface GradedAnswer {
  questionId: string;
  order: number;
  text: string;
  /** Đáp án người dùng đã chọn hoặc đã nhập, hiển thị lại cho dễ đối chiếu. */
  userAnswer: string | null;
  correctAnswer: string;
  isCorrect: boolean;
  explanation?: string;
}

/** Kết quả chung cho bài nghe, bài đọc và bài kiểm tra. */
export interface PracticeResult {
  attemptId: string;
  lessonId: string;
  lessonTitle: L10n;
  skill: Skill;
  /** Điểm trên thang 10. */
  score: number;
  correctCount: number;
  wrongCount: number;
  totalQuestions: number;
  /** Thời gian làm bài, tính bằng giây. */
  durationSeconds: number;
  submittedAt: string;
  /** Nộp quá giới hạn thời gian (backend vẫn nhận và chấm). */
  timedOut?: boolean;
  answers: GradedAnswer[];
  /** Lời thoại, chỉ có với bài nghe. */
  transcript?: string;
}

/* -------------------------------------------------------------------
 * Bài nghe
 * ----------------------------------------------------------------- */

export interface ListeningSummary {
  id: string;
  title: L10n;
  description: L10n;
  topicId: string;
  topicName: L10n;
  level: Level;
  /** Thời lượng audio, tính bằng giây. */
  durationSeconds: number;
  questionCount: number;
  isCompleted: boolean;
  /** Điểm lần làm gần nhất, nếu đã từng làm. */
  lastScore?: number;
  imageUrl?: string | null;
  imageAuthor?: string | null;
  imageAuthorUrl?: string | null;
}

export interface ListeningDetail extends ListeningSummary {
  /** Trống khi bài chưa có tệp mp3; trình phát đọc `speechText` bằng giọng trình duyệt. */
  audioUrl?: string | null;
  /** Transcript, chỉ có khi `audioUrl` trống. */
  speechText?: string | null;
  questions: Question[];
}

/* -------------------------------------------------------------------
 * Bài đọc
 * ----------------------------------------------------------------- */

export interface ReadingSummary {
  id: string;
  title: L10n;
  description: L10n;
  topicId: string;
  topicName: L10n;
  level: Level;
  wordCount: number;
  questionCount: number;
  /** Thời gian làm bài cho phép, tính bằng phút. 0 nghĩa là không giới hạn. */
  timeLimitMinutes: number;
  isCompleted: boolean;
  lastScore?: number;
  imageUrl?: string | null;
  imageAuthor?: string | null;
  imageAuthorUrl?: string | null;
}

export interface ReadingDetail extends ReadingSummary {
  /** Nội dung bài đọc, mỗi phần tử là một đoạn văn. */
  passage: string[];
  questions: Question[];
}

/* -------------------------------------------------------------------
 * Bài kiểm tra
 * ----------------------------------------------------------------- */

export type ExamStatus = 'NOT_TAKEN' | 'IN_PROGRESS' | 'COMPLETED';

export interface ExamSummary {
  id: string;
  title: L10n;
  description: L10n;
  /** Các kỹ năng bài kiểm tra bao phủ. */
  skills: Skill[];
  level: Level;
  questionCount: number;
  timeLimitMinutes: number;
  status: ExamStatus;
  lastScore?: number;
}

export interface ExamDetail extends ExamSummary {
  questions: Question[];
}

/** Điểm tách theo từng kỹ năng trong bài kiểm tra. */
export interface SkillBreakdown {
  skill: Skill;
  correctCount: number;
  totalQuestions: number;
  score: number;
}

export interface ExamResult extends PracticeResult {
  breakdown: SkillBreakdown[];
}

/** Một lần làm bài trong lịch sử. */
export interface AttemptHistoryItem {
  attemptId: string;
  lessonId: string;
  lessonTitle: L10n;
  skill: Skill;
  score: number;
  correctCount: number;
  totalQuestions: number;
  submittedAt: string;
  detailPath: string;
}
