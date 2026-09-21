import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useParams } from 'react-router-dom';

import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { ScoreRing } from '../../components/practice/ResultSummary';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApiError } from '../../hooks/useApiError';
import { useLabels } from '../../hooks/useLabels';
import { speakingService } from '../../services/contentService';
import { ApiError } from '../../shared/api/types';
import {
  type SpeakingResult,
  type SpeakingScores,
} from '../../types/speaking';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const POLL_MS = 1200;

/** Màn hình chờ trong lúc AI nghe và chấm. */
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
          <p className="text-[16px] font-extrabold text-ink">{t('speaking.grading')}</p>
          <p className="mt-1 max-w-sm text-body text-ink-muted">
            {t('speaking.gradingHint')}
          </p>
        </div>
      </div>
    </Card>
  );
};

const SpeakingResultPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { speakingScore } = useLabels();
  const { describe } = useApiError();
  const { attemptId = '' } = useParams();
  const location = useLocation();

  const passed = (location.state as { result?: SpeakingResult } | null)?.result;

  const [result, setResult] = useState<SpeakingResult | null>(passed ?? null);
  const [error, setError] = useState<ApiError | null>(null);
  const [nonce, setNonce] = useState(0);

  /* Hỏi lại tới khi AI chấm xong. Kết quả truyền sang từ trang nộp bài đã
     xong (hiếm) thì khỏi hỏi lại. */
  useEffect(() => {
    if (passed && passed.status !== 'GRADING' && nonce === 0) return;

    let active = true;
    let timer: number | undefined;

    const poll = async () => {
      try {
        const next = await speakingService.getResult(attemptId);
        if (!active) return;
        setResult(next);
        setError(null);
        if (next.status === 'GRADING') timer = window.setTimeout(poll, POLL_MS);
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
      if (timer !== undefined) window.clearTimeout(timer);
    };
  }, [attemptId, passed, nonce]);

  if (error) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Card flush>
          <ErrorState
            message={describe(error)}
            onRetry={() => {
              setError(null);
              setNonce((n) => n + 1);
            }}
          />
        </Card>
      </div>
    );
  }

  if (!result) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Skeleton className="h-[240px] w-full" />
        <Skeleton className="mt-5 h-[360px] w-full" />
      </div>
    );
  }

  const header = (
    <PageHeader
      title={t('speaking.resultTitle')}
      description={L(result.lessonTitle)}
      backTo={{ label: t('speaking.allLessons'), to: '/speaking' }}
    />
  );

  if (result.status === 'GRADING') {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        {header}
        <GradingState />
      </div>
    );
  }

  const { scores, overallScore } = result;

  if (result.status === 'FAILED' || !scores || overallScore === undefined) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        {header}
        <Card>
          <div role="alert" className="flex flex-col items-center gap-4 py-8 text-center">
            <span className="grid h-12 w-12 place-items-center rounded-pill bg-warning-bg text-warning">
              <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
                sync_problem
              </span>
            </span>
            <div>
              <p className="text-[16px] font-extrabold text-ink">
                {t('speaking.failedTitle')}
              </p>
              <p className="mt-1 max-w-md text-body text-ink-muted">
                {t('speaking.failedHint')}
              </p>
            </div>
            <ButtonLink to={`/speaking/${result.lessonId}`} icon="mic">
              {t('speaking.practiceAgain')}
            </ButtonLink>
          </div>
        </Card>
      </div>
    );
  }

  const scoreRows = (Object.keys(scores) as Array<keyof SpeakingScores>).map((key) => ({
    key,
    label: speakingScore(key),
    value: scores[key],
  }));

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
      {header}

      <Card className="mb-5">
        <div className="flex flex-wrap items-center gap-6">
          <ScoreRing score={overallScore} />

          <dl className="min-w-0 flex-1 space-y-3">
            {scoreRows.map((row) => (
              <div key={row.key}>
                <div className="flex items-baseline justify-between gap-2">
                  <dt className="text-[13px] text-ink-muted">{row.label}</dt>
                  <dd className="text-[13px] font-extrabold text-ink">
                    {formatScore(row.value)}
                  </dd>
                </div>
                <div
                  role="progressbar"
                  aria-valuenow={row.value}
                  aria-valuemin={0}
                  aria-valuemax={10}
                  aria-label={row.label}
                  className="mt-1 h-1.5 overflow-hidden rounded-pill bg-surface-muted"
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
      </Card>

      <Card className="mb-5">
        <h2 className="text-card-title text-ink">{t('speaking.improvements')}</h2>
        <ul className="mt-3 flex flex-col gap-2.5">
          {result.improvements.map((item) => (
            <li key={item} className="flex gap-2.5 text-[14px] text-ink-muted">
              <span
                aria-hidden="true"
                className="material-symbols-outlined mt-0.5 shrink-0 text-[18px] text-warning"
              >
                tips_and_updates
              </span>
              {item}
            </li>
          ))}
        </ul>
      </Card>

      <Card>
        <h2 className="text-card-title text-ink">{t('speaking.perSentence')}</h2>

        <ul className="mt-3 divide-y divide-hairline">
          {result.promptFeedback.map((item) => (
            <li key={item.promptId} className="py-4 first:pt-0 last:pb-0">
              <div className="flex items-start justify-between gap-3">
                <p className="min-w-0 flex-1 text-[14.5px] font-semibold text-ink">
                  {item.text}
                </p>
                <span className="shrink-0 text-[16px] font-extrabold text-ink">
                  {formatScore(item.score)}
                </span>
              </div>

              {item.transcript && (
                <p className="mt-1.5 text-[13.5px] text-ink-muted">
                  <span className="text-ink-subtle">{t('speaking.transcript')}</span>{' '}
                  {item.transcript}
                </p>
              )}

              <p className="mt-1.5 text-[13.5px] text-ink-muted">{item.comment}</p>

              {item.mispronounced.length > 0 && (
                <div className="mt-2 flex flex-wrap items-center gap-2">
                  <span className="text-caption text-ink-subtle">
                    {t('speaking.mispronounced')}
                  </span>
                  {item.mispronounced.map((word) => (
                    <Chip key={word} tone="warning">
                      {word}
                    </Chip>
                  ))}
                </div>
              )}
            </li>
          ))}
        </ul>

        <div className="mt-5 flex flex-wrap gap-3">
          <ButtonLink to={`/speaking/${result.lessonId}`}>
            {t('speaking.practiceAgain')}
          </ButtonLink>
          <ButtonLink to="/speaking" variant="subtle">
            {t('speaking.pickAnother')}
          </ButtonLink>
        </div>
      </Card>
    </div>
  );
};

export default SpeakingResultPage;
