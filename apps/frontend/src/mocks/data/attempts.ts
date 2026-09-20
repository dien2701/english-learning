import type {
  AnswerSubmission,
  AttemptHistoryItem,
  ExamResult,
  GradedAnswer,
  PracticeResult,
  Question,
  SkillBreakdown,
} from '../../types/practice';
import type { Skill } from '../../types/common';
import type { L10n } from '../../types/l10n';
import type { SpeakingResult } from '../../types/speaking';
import { isTextAnswerCorrect } from './questions';
import { examQuestionSkill } from './exam';

let counter = 1;
export const nextAttemptId = (prefix: string): string =>
  `${prefix}-${Date.now().toString(36)}-${counter++}`;

/** Kho kết quả đã nộp, giữ trong bộ nhớ của phiên chạy hiện tại. */
export const practiceResults = new Map<string, PracticeResult | ExamResult>();
export const speakingResults = new Map<string, SpeakingResult>();

/** Lịch sử làm bài, mới nhất lên đầu. */
export const attemptHistory: AttemptHistoryItem[] = [];

/** Chấm một câu và dựng bản ghi kết quả để hiển thị lại. */
function gradeOne(
  question: Question,
  submission: AnswerSubmission | undefined,
): GradedAnswer {
  if (question.kind === 'SINGLE_CHOICE') {
    const chosen = question.options?.find((o) => o.id === submission?.optionId);
    const correct = question.options?.find((o) => o.id === question.correctOptionId);

    return {
      questionId: question.id,
      order: question.order,
      text: question.text,
      userAnswer: chosen?.text ?? null,
      correctAnswer: correct?.text ?? '',
      isCorrect: Boolean(chosen && chosen.id === question.correctOptionId),
      explanation: question.explanation,
    };
  }

  const typed = submission?.text?.trim() ?? '';
  const expected = question.correctText ?? '';

  return {
    questionId: question.id,
    order: question.order,
    text: question.text,
    userAnswer: typed || null,
    correctAnswer: expected,
    isCorrect: typed !== '' && isTextAnswerCorrect(typed, expected),
    explanation: question.explanation,
  };
}

interface GradeInput {
  lessonId: string;
  lessonTitle: L10n;
  skill: Skill;
  questions: Question[];
  answers: AnswerSubmission[];
  durationSeconds: number;
  transcript?: string;
  attemptPrefix: string;
  detailPathBase: string;
}

/** Chấm toàn bộ bài, lưu kết quả và ghi vào lịch sử. */
export function gradeAttempt(input: GradeInput): PracticeResult {
  const byQuestion = new Map(input.answers.map((a) => [a.questionId, a]));

  const graded = input.questions.map((q) => gradeOne(q, byQuestion.get(q.id)));
  const correctCount = graded.filter((g) => g.isCorrect).length;
  const total = input.questions.length;

  const result: PracticeResult = {
    attemptId: nextAttemptId(input.attemptPrefix),
    lessonId: input.lessonId,
    lessonTitle: input.lessonTitle,
    skill: input.skill,
    score: total === 0 ? 0 : Math.round((correctCount / total) * 100) / 10,
    correctCount,
    wrongCount: total - correctCount,
    totalQuestions: total,
    durationSeconds: input.durationSeconds,
    submittedAt: new Date().toISOString(),
    answers: graded,
    transcript: input.transcript,
  };

  practiceResults.set(result.attemptId, result);

  attemptHistory.unshift({
    attemptId: result.attemptId,
    lessonId: result.lessonId,
    lessonTitle: result.lessonTitle,
    skill: result.skill,
    score: result.score,
    correctCount: result.correctCount,
    totalQuestions: result.totalQuestions,
    submittedAt: result.submittedAt,
    detailPath: `${input.detailPathBase}/${result.attemptId}`,
  });

  return result;
}

/** Chấm bài kiểm tra, có thêm phần tách điểm theo từng kỹ năng. */
export function gradeExamAttempt(input: GradeInput): ExamResult {
  const base = gradeAttempt(input);

  const buckets = new Map<Skill, { correct: number; total: number }>();

  for (const answer of base.answers) {
    const skill = examQuestionSkill[answer.questionId] ?? 'EXAM';
    const bucket = buckets.get(skill) ?? { correct: 0, total: 0 };
    bucket.total += 1;
    if (answer.isCorrect) bucket.correct += 1;
    buckets.set(skill, bucket);
  }

  const breakdown: SkillBreakdown[] = [...buckets.entries()].map(
    ([skill, { correct, total }]) => ({
      skill,
      correctCount: correct,
      totalQuestions: total,
      score: total === 0 ? 0 : Math.round((correct / total) * 100) / 10,
    }),
  );

  const result: ExamResult = { ...base, breakdown };
  practiceResults.set(result.attemptId, result);
  return result;
}
