import React from 'react';
import { useTranslation } from 'react-i18next';
import { useLocation, useParams } from 'react-router-dom';

import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { ScoreRing } from '../../components/practice/ResultSummary';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useLabels } from '../../hooks/useLabels';
import { speakingService } from '../../services/contentService';
import {
  type SpeakingResult,
  type SpeakingScores,
} from '../../types/speaking';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const SpeakingResultPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { speakingScore } = useLabels();
  const { describe } = useApiError();
  const { attemptId = '' } = useParams();
  const location = useLocation();

  const passed = (location.state as { result?: SpeakingResult } | null)?.result;

  const { data, isLoading, error, reload } = useApi(
    () => speakingService.getResult(attemptId),
    [attemptId],
  );

  const result = passed ?? data;

  if (error && !passed) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (!result || (isLoading && !passed)) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Skeleton className="h-[240px] w-full" />
        <Skeleton className="mt-5 h-[360px] w-full" />
      </div>
    );
  }

  const scoreRows = (Object.keys(result.scores) as Array<keyof SpeakingScores>).map(
    (key) => ({ key, label: speakingScore(key), value: result.scores[key] }),
  );

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
      <PageHeader
        title={t('speaking.resultTitle')}
        description={L(result.lessonTitle)}
        backTo={{ label: t('speaking.allLessons'), to: '/speaking' }}
      />

      <Card className="mb-5">
        <div className="flex flex-wrap items-center gap-6">
          <ScoreRing score={result.overallScore} />

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
