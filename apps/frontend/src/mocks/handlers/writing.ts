/** Mock cho module Luyện viết và chấm bằng AI (chức năng 4). */

import { fail, get, matches, num, paginate, post } from '../router';
import {
  findWritingPrompt,
  generateFeedback,
  writingPrompts,
  writingSubmissions,
} from '../data/writing';
import type { WritingSubmission } from '../../types/writing';

/* --- GET /writing/prompts ------------------------------------------- */

get('/writing/prompts', ({ query }) => {
  const search = query.get('search') ?? '';
  const topicId = query.get('topicId') ?? '';
  const level = query.get('level') ?? '';

  const filtered = writingPrompts.filter((prompt) => {
    if (topicId && prompt.topicId !== topicId) return false;
    if (level && prompt.level !== level) return false;
    if (search && !matches(prompt.title, search)) return false;
    return true;
  });

  const summaries = filtered.map(({ prompt: _p, hints: _h, ...rest }) => rest);
  return paginate(summaries, num(query, 'page', 1), num(query, 'pageSize', 12));
});

/* --- GET /writing/prompts/:id --------------------------------------- */

get('/writing/prompts/:id', ({ params }) => {
  const prompt = findWritingPrompt(params.id);
  if (!prompt) fail(404, 'errors.promptNotFound', 'PROMPT_NOT_FOUND');
  return prompt;
});

/* --- POST /writing/prompts/:id/submit -------------------------------
 * Bài viết được lưu trước, sau đó mới gọi AI. Nếu AI không trả kết quả,
 * bài vẫn còn và chuyển sang trạng thái cần chấm lại — không làm mất bài
 * của người học.
 * ------------------------------------------------------------------- */

post('/writing/prompts/:id/submit', ({ params, body }): WritingSubmission => {
  const prompt = findWritingPrompt(params.id);
  if (!prompt) fail(404, 'errors.promptNotFound', 'PROMPT_NOT_FOUND');

  const { content } = (body ?? {}) as { content?: string };
  if (!content?.trim()) {
    fail(400, 'errors.emptyEssay', 'VALIDATION', {
      content: 'errors.field.essayRequired',
    });
  }

  const wordCount = content.trim().split(/\s+/).filter(Boolean).length;

  const submission: WritingSubmission = {
    id: `ws-${Date.now().toString(36)}`,
    promptId: prompt.id,
    promptTitle: prompt.title,
    content,
    wordCount,
    status: 'GRADING',
    submittedAt: new Date().toISOString(),
  };

  writingSubmissions.unshift(submission);

  return submission;
});

/* --- GET /writing/submissions/:id ------------------------------------
 * Giao diện hỏi lại endpoint này cho tới khi AI chấm xong. Ở đây mô phỏng
 * bằng cách trả về GRADING trong vài giây đầu rồi mới trả kết quả.
 * ------------------------------------------------------------------- */

const GRADING_MS = 3500;

get('/writing/submissions/:id', ({ params }): WritingSubmission => {
  const submission = writingSubmissions.find((s) => s.id === params.id);
  if (!submission) fail(404, 'errors.submissionNotFound', 'SUBMISSION_NOT_FOUND');

  if (submission.status !== 'GRADING') return submission;

  const elapsed = Date.now() - new Date(submission.submittedAt).getTime();
  if (elapsed < GRADING_MS) return submission;

  const prompt = findWritingPrompt(submission.promptId);
  submission.feedback = generateFeedback(
    submission.content,
    prompt?.minWords ?? 100,
  );
  submission.status = 'GRADED';

  // Cập nhật lại trạng thái của đề bài để danh sách hiển thị đúng.
  if (prompt) {
    prompt.status = 'GRADED';
    prompt.lastScore = submission.feedback.overallScore;
  }

  return submission;
});

/* --- POST /writing/submissions/:id/regrade --------------------------- */

post('/writing/submissions/:id/regrade', ({ params }): WritingSubmission => {
  const submission = writingSubmissions.find((s) => s.id === params.id);
  if (!submission) fail(404, 'errors.submissionNotFound', 'SUBMISSION_NOT_FOUND');

  submission.status = 'GRADING';
  submission.submittedAt = new Date().toISOString();
  submission.feedback = undefined;

  return submission;
});

/* --- GET /writing/submissions ---------------------------------------- */

get('/writing/submissions', ({ query }) =>
  paginate(writingSubmissions, num(query, 'page', 1), num(query, 'pageSize', 10)),
);
