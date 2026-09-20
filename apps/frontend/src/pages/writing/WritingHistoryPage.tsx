import React from 'react';
import { useTranslation } from 'react-i18next';

import { ButtonLink } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { writingService } from '../../services/contentService';
import type { WritingStatus } from '../../types/writing';
import { formatScore } from '../../utils/format';
import { useLanguage } from '../../hooks/useLanguage';

const STATUS_TONE: Record<WritingStatus, 'neutral' | 'brand' | 'success' | 'danger'> = {
  NOT_STARTED: 'neutral',
  DRAFT: 'neutral',
  GRADING: 'brand',
  GRADED: 'success',
  NEEDS_RETRY: 'danger',
};

const WritingHistoryPage: React.FC = () => {
  const { t } = useTranslation();
  const { writingStatus } = useLabels();
  const { date } = useFormat();
  const { describe } = useApiError();
  const { L } = useLanguage();
  const { data, isLoading, error, reload } = useApi(
    () => writingService.history(),
    [],
  );

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('writing.history')}
        description={t('writing.historySubtitle')}
        backTo={{ label: t('writing.title'), to: '/writing' }}
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <Skeleton className="h-[320px] w-full" />
      ) : data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="history"
            title={t('writing.historyEmptyTitle')}
            message={t('writing.historyEmptyHint')}
            action={
              <ButtonLink to="/writing">
                {t('writing.pickPrompt')}
              </ButtonLink>
            }
          />
        </Card>
      ) : (
        <Card>
          <ul className="divide-y divide-hairline">
            {data.items.map((submission) => (
              <li key={submission.id} className="py-4 first:pt-0 last:pb-0">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <h2 className="text-[15px] font-extrabold text-ink">
                      {L(submission.promptTitle)}
                    </h2>

                    <div className="mt-1.5 flex flex-wrap items-center gap-2">
                      <Chip tone={STATUS_TONE[submission.status]}>
                        {writingStatus(submission.status)}
                      </Chip>
                      <span className="text-caption text-ink-subtle">
                        {date(submission.submittedAt)} ·{' '}
                      {t('writing.wordCount', { count: submission.wordCount })}
                      </span>
                    </div>

                    <p className="mt-2 line-clamp-2 text-[13px] text-ink-muted">
                      {submission.content}
                    </p>
                  </div>

                  <div className="flex shrink-0 items-center gap-4">
                    {submission.feedback && (
                      <div className="text-right">
                        <p className="text-[20px] font-extrabold leading-none text-ink">
                          {formatScore(submission.feedback.overallScore)}
                        </p>
                        <p className="mt-0.5 text-[11px] text-ink-subtle">
                      {t('common.points')}
                    </p>
                      </div>
                    )}

                    <ButtonLink
                      to={`/writing/${submission.promptId}/result`}
                      variant="subtle"
                      size="sm"
                      state={{ submissionId: submission.id }}
                    >
                      {t('writing.viewFeedback')}
                    </ButtonLink>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default WritingHistoryPage;
