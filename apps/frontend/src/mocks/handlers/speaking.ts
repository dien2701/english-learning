/** Mock cho module Luyện nói (chức năng 7). */

import { fail, get, matches, num, paginate, post, searchable } from '../router';
import {
  findSpeaking,
  generateSpeakingResult,
  speakingLessons,
} from '../data/speaking';
import { nextAttemptId, speakingResults } from '../data/attempts';
import type { SpeakingResult } from '../../types/speaking';

get('/speaking/lessons', ({ query }) => {
  const search = query.get('search') ?? '';
  const topicId = query.get('topicId') ?? '';
  const level = query.get('level') ?? '';

  const filtered = speakingLessons.filter((lesson) => {
    if (topicId && lesson.topicId !== topicId) return false;
    if (level && lesson.level !== level) return false;
    if (search && !matches(searchable(lesson.title, lesson.description), search)) return false;
    return true;
  });

  const summaries = filtered.map(({ prompts: _p, ...rest }) => rest);
  return paginate(summaries, num(query, 'page', 1), num(query, 'pageSize', 12));
});

get('/speaking/lessons/:id', ({ params }) => {
  const lesson = findSpeaking(params.id);
  if (!lesson) fail(404, 'errors.speakingNotFound', 'LESSON_NOT_FOUND');
  return lesson;
});

/* --- POST /speaking/lessons/:id/submit -------------------------------
 * Bản ghi âm không được gửi lên trong bản chạy thử: giao diện chỉ báo
 * những câu nào đã thu và tổng thời lượng. Khi nối backend thật, đây là
 * chỗ sẽ gửi tệp âm thanh lên Cloudinary rồi chuyển cho AI phân tích.
 * ------------------------------------------------------------------- */

interface SubmitBody {
  recordedPromptIds?: string[];
  totalDurationSeconds?: number;
}

post('/speaking/lessons/:id/submit', ({ params, body }): SpeakingResult => {
  const lesson = findSpeaking(params.id);
  if (!lesson) fail(404, 'errors.speakingNotFound', 'LESSON_NOT_FOUND');

  const { recordedPromptIds, totalDurationSeconds } = (body ?? {}) as SubmitBody;

  if (!Array.isArray(recordedPromptIds) || recordedPromptIds.length === 0) {
    fail(400, 'errors.noRecording', 'NO_RECORDING');
  }

  const generated = generateSpeakingResult(
    lesson,
    recordedPromptIds,
    Math.max(0, totalDurationSeconds ?? 0),
  );

  const result: SpeakingResult = {
    ...generated,
    attemptId: nextAttemptId('spa'),
    submittedAt: new Date().toISOString(),
  };

  speakingResults.set(result.attemptId, result);

  lesson.isCompleted = true;
  lesson.lastScore = result.overallScore;

  return result;
});

get('/speaking/results/:attemptId', ({ params }): SpeakingResult => {
  const result = speakingResults.get(params.attemptId);
  if (!result) {
    fail(
      404,
      'errors.attemptNotFound',
      'ATTEMPT_NOT_FOUND',
    );
  }
  return result;
});
