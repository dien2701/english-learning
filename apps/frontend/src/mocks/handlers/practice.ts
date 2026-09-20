/**
 * Mock cho Luyện nghe, Luyện đọc và Bài kiểm tra (chức năng 5, 6, 8).
 *
 * Ba module này dùng chung một khuôn: chọn bài → làm bài có câu hỏi →
 * nộp → chấm → xem kết quả, nên gom vào một file để logic chấm chỉ nằm
 * ở đúng một chỗ.
 */

import { fail, get, matches, num, paginate, post, searchable } from '../router';
import { withoutAnswers } from '../data/questions';
import { findListening, listeningLessons, listeningTranscripts } from '../data/listening';
import { findReading, readingLessons } from '../data/reading';
import { exams, findExam } from '../data/exam';
import {
  attemptHistory,
  gradeAttempt,
  gradeExamAttempt,
  practiceResults,
} from '../data/attempts';
import type { AnswerSubmission } from '../../types/practice';

interface SubmitBody {
  answers?: AnswerSubmission[];
  durationSeconds?: number;
}

function readSubmission(body: unknown): { answers: AnswerSubmission[]; duration: number } {
  const { answers, durationSeconds } = (body ?? {}) as SubmitBody;
  if (!Array.isArray(answers)) {
    fail(400, 'errors.missingAnswers', 'VALIDATION');
  }
  return { answers, duration: Math.max(0, durationSeconds ?? 0) };
}

/* ===================================================================
 * Luyện nghe
 * ================================================================= */

get('/listening/lessons', ({ query }) => {
  const search = query.get('search') ?? '';
  const topicId = query.get('topicId') ?? '';
  const level = query.get('level') ?? '';
  const status = query.get('status') ?? '';

  const filtered = listeningLessons.filter((lesson) => {
    if (topicId && lesson.topicId !== topicId) return false;
    if (level && lesson.level !== level) return false;
    if (status === 'COMPLETED' && !lesson.isCompleted) return false;
    if (status === 'NOT_COMPLETED' && lesson.isCompleted) return false;
    if (search && !matches(searchable(lesson.title, lesson.description), search)) return false;
    return true;
  });

  const summaries = filtered.map(({ questions: _q, audioUrl: _a, ...rest }) => rest);
  return paginate(summaries, num(query, 'page', 1), num(query, 'pageSize', 12));
});

get('/listening/lessons/:id', ({ params }) => {
  const lesson = findListening(params.id);
  if (!lesson) fail(404, 'errors.listeningNotFound', 'LESSON_NOT_FOUND');

  // Chưa nộp bài thì không gửi đáp án và cũng không gửi transcript.
  return { ...lesson, questions: withoutAnswers(lesson.questions) };
});

post('/listening/lessons/:id/submit', ({ params, body }) => {
  const lesson = findListening(params.id);
  if (!lesson) fail(404, 'errors.listeningNotFound', 'LESSON_NOT_FOUND');

  const { answers, duration } = readSubmission(body);

  const result = gradeAttempt({
    lessonId: lesson.id,
    lessonTitle: lesson.title,
    skill: 'LISTENING',
    questions: lesson.questions,
    answers,
    durationSeconds: duration,
    transcript: listeningTranscripts[lesson.id],
    attemptPrefix: 'lsa',
    detailPathBase: '/listening/result',
  });

  lesson.isCompleted = true;
  lesson.lastScore = result.score;

  return result;
});

/* ===================================================================
 * Luyện đọc
 * ================================================================= */

get('/reading/lessons', ({ query }) => {
  const search = query.get('search') ?? '';
  const topicId = query.get('topicId') ?? '';
  const level = query.get('level') ?? '';

  const filtered = readingLessons.filter((lesson) => {
    if (topicId && lesson.topicId !== topicId) return false;
    if (level && lesson.level !== level) return false;
    if (search && !matches(searchable(lesson.title, lesson.description), search)) return false;
    return true;
  });

  const summaries = filtered.map(({ questions: _q, passage: _p, ...rest }) => rest);
  return paginate(summaries, num(query, 'page', 1), num(query, 'pageSize', 12));
});

get('/reading/lessons/:id', ({ params }) => {
  const lesson = findReading(params.id);
  if (!lesson) fail(404, 'errors.readingNotFound', 'LESSON_NOT_FOUND');
  return { ...lesson, questions: withoutAnswers(lesson.questions) };
});

post('/reading/lessons/:id/submit', ({ params, body }) => {
  const lesson = findReading(params.id);
  if (!lesson) fail(404, 'errors.readingNotFound', 'LESSON_NOT_FOUND');

  const { answers, duration } = readSubmission(body);

  const result = gradeAttempt({
    lessonId: lesson.id,
    lessonTitle: lesson.title,
    skill: 'READING',
    questions: lesson.questions,
    answers,
    durationSeconds: duration,
    attemptPrefix: 'rda',
    detailPathBase: '/reading/result',
  });

  lesson.isCompleted = true;
  lesson.lastScore = result.score;

  return result;
});

/* ===================================================================
 * Bài kiểm tra
 * ================================================================= */

get('/exams', ({ query }) => {
  const search = query.get('search') ?? '';
  const level = query.get('level') ?? '';
  const status = query.get('status') ?? '';

  const filtered = exams.filter((exam) => {
    if (level && exam.level !== level) return false;
    if (status && exam.status !== status) return false;
    if (search && !matches(searchable(exam.title, exam.description), search)) return false;
    return true;
  });

  const summaries = filtered.map(({ questions: _q, ...rest }) => rest);
  return paginate(summaries, num(query, 'page', 1), num(query, 'pageSize', 12));
});

get('/exams/:id', ({ params }) => {
  const exam = findExam(params.id);
  if (!exam) fail(404, 'errors.examNotFound', 'EXAM_NOT_FOUND');
  return { ...exam, questions: withoutAnswers(exam.questions) };
});

post('/exams/:id/submit', ({ params, body }) => {
  const exam = findExam(params.id);
  if (!exam) fail(404, 'errors.examNotFound', 'EXAM_NOT_FOUND');

  const { answers, duration } = readSubmission(body);

  const result = gradeExamAttempt({
    lessonId: exam.id,
    lessonTitle: exam.title,
    skill: 'EXAM',
    questions: exam.questions,
    answers,
    durationSeconds: duration,
    attemptPrefix: 'exa',
    detailPathBase: '/exam/result',
  });

  exam.status = 'COMPLETED';
  exam.lastScore = result.score;

  return result;
});

/* ===================================================================
 * Kết quả và lịch sử, dùng chung cho cả ba module
 * ================================================================= */

get('/attempts/:attemptId', ({ params }) => {
  const result = practiceResults.get(params.attemptId);
  if (!result) {
    fail(
      404,
      'errors.attemptNotFound',
      'ATTEMPT_NOT_FOUND',
    );
  }
  return result;
});

get('/attempts', ({ query }) => {
  const skill = query.get('skill') ?? '';
  const filtered = skill
    ? attemptHistory.filter((a) => a.skill === skill)
    : attemptHistory;

  return paginate(filtered, num(query, 'page', 1), num(query, 'pageSize', 10));
});
