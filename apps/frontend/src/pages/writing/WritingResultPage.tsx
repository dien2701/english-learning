import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useParams } from 'react-router-dom';

import { Button, ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { ScoreRing } from '../../components/practice/ResultSummary';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApiError } from '../../hooks/useApiError';
import { useLabels } from '../../hooks/useLabels';
import { writingService } from '../../services/contentService';
import { ApiError } from '../../shared/api/types';
import type { WritingIssue, WritingSubmission } from '../../types/writing';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const CATEGORY_TONE: Record<WritingIssue['category'], 'danger' | 'warning' | 'brand'> = {
  GRAMMAR: 'danger',
  VOCABULARY: 'warning',
  EXPRESSION: 'brand',
};

/** Màn hình chờ trong lúc AI chấm bài. */
const GradingState: React.FC = () => {
  const { t } = useTranslation();

  return (
    <Card>
      <div
        role="status"
        aria-live="polite"
        className="flex flex-col items-center gap-4 py-10 text-center"
      >
        <span className="h-12 w-12 animate-spin rounded-pill border-[3px] border-accent-line border-t-brand-600" />
        <div>
          <p className="text-[16px] font-extrabold text-ink">
            {t('writing.grading')}
          </p>
          <p className="mt-1 max-w-sm text-body text-ink-muted">
            {t('writing.gradingHint')}
          </p>
        </div>
      </div>
    </Card>
  );
};

const WritingResultPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { issueCategory } = useLabels();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  const location = useLocation();

  const submissionId = (location.state as { submissionId?: string } | null)
    ?.submissionId;

  const [submission, setSubmission] = useState<WritingSubmission | null>(null);
  const [error, setError] = useState<ApiError | null>(null);

  // Hỏi lại trạng thái cho tới khi AI chấm xong.
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    if (!submissionId) return;

    let active = true;

    const poll = async () => {
      try {
        const next = await writingService.getSubmission(submissionId);
        if (!active) return;

        setSubmission(next);

        if (next.status === 'GRADING') {
          timerRef.current = window.setTimeout(poll, 1200);
        }
      } catch (pollError) {
        if (!active) return;
        setError(
          pollError instanceof ApiError
            ? pollError
            : new ApiError(0, 'errors.loadFailed', 'UNKNOWN', undefined, {
                messageKey: 'errors.loadFailed',
              }),
        );
      }
    };

    void poll();

    return () => {
      active = false;
      if (timerRef.current !== null) window.clearTimeout(timerRef.current);
    };
  }, [submissionId]);

  if (!submissionId) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Card>
          <EmptyBlock
            icon="edit_note"
            title={t('writing.noSubmissionTitle')}
            message={t('writing.noSubmissionHint')}
            action={
              <ButtonLink to={`/writing/${id}`}>
                {t('writing.backToPrompt')}
              </ButtonLink>
            }
          />
        </Card>
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState
            message={describe(error)}
            onRetry={() => window.location.reload()}
          />
        </Card>
      </div>
    );
  }

  if (!submission) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[400px] w-full" />
      </div>
    );
  }

  const feedback = submission.feedback;

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('writing.resultTitle')}
        description={L(submission.promptTitle)}
        backTo={{ label: t('writing.allPrompts'), to: '/writing' }}
      />

      {submission.status === 'GRADING' && <GradingState />}

      {submission.status === 'NEEDS_RETRY' && (
        <Card>
          <div role="alert" className="flex flex-col items-center gap-4 py-8 text-center">
            <span className="grid h-12 w-12 place-items-center rounded-pill bg-warning-bg text-warning">
              <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
                sync_problem
              </span>
            </span>
            <div>
              <p className="text-[16px] font-extrabold text-ink">
                {t('writing.needsRetryTitle')}
              </p>
              <p className="mt-1 max-w-md text-body text-ink-muted">
                {t('writing.needsRetryHint')}
              </p>
            </div>
            <Button
              icon="refresh"
              onClick={() => writingService.regrade(submission.id)}
            >
              {t('writing.regrade')}
            </Button>
          </div>
        </Card>
      )}

      {submission.status === 'GRADED' && feedback && (
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
          <div className="flex flex-col gap-5 lg:col-span-5">
            {/* Điểm tổng và điểm thành phần */}
            <Card>
              <div className="flex flex-wrap items-center gap-5">
                <ScoreRing score={feedback.overallScore} />

                <dl className="min-w-0 flex-1 space-y-2.5">
                  {[
                    { label: t('writing.grammar'), value: feedback.grammarScore },
                    {
                      label: t('writing.vocabulary'),
                      value: feedback.vocabularyScore,
                    },
                    {
                      label: t('writing.expression'),
                      value: feedback.expressionScore,
                    },
                  ].map((row) => (
                    <div key={row.label}>
                      <div className="flex items-baseline justify-between gap-2">
                        <dt className="text-[13px] text-ink-muted">{row.label}</dt>
                        <dd className="text-[13px] font-extrabold text-ink">
                          {formatScore(row.value)}
                        </dd>
                      </div>
                      <div
                        className="mt-1 h-1.5 overflow-hidden rounded-pill bg-surface-muted"
                        role="progressbar"
                        aria-valuenow={row.value}
                        aria-valuemin={0}
                        aria-valuemax={10}
                        aria-label={row.label}
                      >
                        <div
                          className="h-full rounded-pill bg-brand-500"
                          style={{ width: `${(row.value / 10) * 100}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </dl>
              </div>

              <div className="mt-5 rounded-md bg-surface-muted p-4">
                <h2 className="text-[13px] font-bold uppercase tracking-wide text-ink-subtle">
                  {t('writing.summary')}
                </h2>
                <p className="mt-1.5 text-[14px] leading-relaxed text-ink">
                  {feedback.summary}
                </p>
              </div>
            </Card>

            {/* Bài viết gốc */}
            <Card>
              <h2 className="text-card-title text-ink">{t('writing.yourEssay')}</h2>
              <p className="mt-0.5 text-caption text-ink-muted">
                {t('writing.wordCount', { count: submission.wordCount })}
              </p>
              <p className="mt-3 whitespace-pre-wrap text-[14px] leading-relaxed text-ink">
                {submission.content}
              </p>
            </Card>
          </div>

          {/* Danh sách lỗi và cách sửa */}
          <div className="lg:col-span-7">
            <Card>
              <h2 className="text-card-title text-ink">{t('writing.issues')}</h2>
              <p className="mt-0.5 text-caption text-ink-muted">
                {t('writing.issueCount', { count: feedback.issues.length })}
              </p>

              <ul className="mt-4 flex flex-col gap-3">
                {feedback.issues.map((issue) => (
                  <li
                    key={issue.id}
                    className="rounded-md border border-hairline p-4"
                  >
                    <Chip tone={CATEGORY_TONE[issue.category]}>
                      {issueCategory(issue.category)}
                    </Chip>

                    <p className="mt-2.5 border-l-2 border-danger pl-3 text-[14px] italic text-ink-muted">
                      {issue.excerpt}
                    </p>

                    <p className="mt-2.5 text-[13.5px] text-ink-muted">
                      {issue.problem}
                    </p>

                    <div className="mt-2.5 rounded-md bg-success-bg p-3">
                      <p className="text-[12px] font-bold uppercase tracking-wide text-success-fg">
                        {t('writing.shouldBe')}
                      </p>
                      <p className="mt-1 text-[14px] text-success-fg">
                        {issue.suggestion}
                      </p>
                    </div>
                  </li>
                ))}
              </ul>

              <div className="mt-5 flex flex-wrap gap-3">
                <ButtonLink to={`/writing/${id}`}>
                  {t('writing.rewrite')}
                </ButtonLink>
                <ButtonLink to="/writing/history" variant="subtle">
                  {t('writing.viewHistory')}
                </ButtonLink>
              </div>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
};

export default WritingResultPage;
