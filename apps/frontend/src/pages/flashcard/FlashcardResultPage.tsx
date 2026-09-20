import React from 'react';
import { useLocation, useParams } from 'react-router-dom';

import { ButtonLink, IconButton } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { EmptyBlock } from '../../components/ui/StateBlocks';
import { useSpeech } from '../../hooks/useSpeech';
import type { StudyResult } from '../../types/flashcard';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import WordImage from '../../components/flashcard/WordImage';

const FlashcardResultPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { id = '' } = useParams();
  const location = useLocation();
  const { speak, isSupported } = useSpeech();

  // Kết quả được truyền qua state khi chuyển trang từ phiên học.
  const result = (location.state as { result?: StudyResult } | null)?.result;

  if (!result) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Card>
          <EmptyBlock
            icon="quiz"
            title={t('flashcard.noResultTitle')}
            message={t('flashcard.noResultHint')}
            action={
              <ButtonLink to={`/flashcard/${id}`}>
                {t('flashcard.backToDeck')}
              </ButtonLink>
            }
          />
        </Card>
      </div>
    );
  }

  const stats = [
    {
      label: t('flashcard.studiedWords'),
      value: result.studiedCards,
      tone: 'text-ink',
    },
    {
      label: t('recall.REMEMBERED'),
      value: result.remembered,
      tone: 'text-success',
    },
    {
      label: t('recall.ALMOST_REMEMBERED'),
      value: result.almostRemembered,
      tone: 'text-warning',
    },
    {
      label: t('recall.NOT_REMEMBERED'),
      value: result.notRemembered,
      tone: 'text-danger',
    },
  ];

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
      <PageHeader
        title={t('flashcard.resultTitle')}
        description={L(result.deckTitle)}
        backTo={{ label: t('flashcard.backToDeck'), to: `/flashcard/${id}` }}
      />

      <Card className="mb-5">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-caption text-ink-muted">
              {t('flashcard.completionRate')}
            </p>
            <p className="mt-1 text-[34px] font-extrabold leading-none tracking-tight text-ink">
              {result.completionPercent}%
            </p>
            <p className="mt-1 text-caption text-ink-subtle">
              {t('flashcard.cardsInDeck', {
                done: result.studiedCards,
                total: result.totalCards,
              })}
            </p>
          </div>

          <dl className="grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-4">
            {stats.map((stat) => (
              <div key={stat.label}>
                <dt className="text-caption text-ink-muted">{stat.label}</dt>
                <dd className={`mt-0.5 text-[22px] font-extrabold ${stat.tone}`}>
                  {stat.value}
                </dd>
              </div>
            ))}
          </dl>
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <ButtonLink to={`/flashcard/${id}/study`}>
            {t('flashcard.studyAgain')}
          </ButtonLink>
          <ButtonLink to="/flashcard" variant="subtle">
            {t('flashcard.pickAnother')}
          </ButtonLink>
        </div>
      </Card>

      <Card>
        <h2 className="text-card-title text-ink">{t('flashcard.reviewTitle')}</h2>
        <p className="mt-0.5 text-caption text-ink-muted">
          {t('flashcard.reviewHint')}
        </p>

        {result.wordsToReview.length === 0 ? (
          <EmptyBlock
            icon="celebration"
            title={t('flashcard.allRememberedTitle')}
            message={t('flashcard.allRememberedHint')}
          />
        ) : (
          <ul className="mt-3 divide-y divide-hairline">
            {result.wordsToReview.map((word) => (
              <li key={word.id} className="flex items-center gap-3 py-3">
                <WordImage
                  word={word.word}
                  src={word.imageUrl}
                  size="sm"
                  className="h-12 w-12 shrink-0 rounded-md"
                />
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-baseline gap-x-2.5">
                    <span className="text-[15px] font-extrabold text-ink">
                      {word.word}
                    </span>
                    {word.phonetic && (
                      <span className="text-[12.5px] text-ink-subtle">
                        {word.phonetic}
                      </span>
                    )}
                  </div>
                  <p className="mt-0.5 text-[13.5px] text-ink-muted">{L(word.meaning)}</p>
                </div>

                <Chip
                  tone={word.recallLevel === 'NOT_REMEMBERED' ? 'danger' : 'warning'}
                >
                  {t(`recall.${word.recallLevel}`)}
                </Chip>

                {isSupported && (
                  <IconButton
                    icon="volume_up"
                    label={t('flashcard.pronounce', { word: word.word })}
                    variant="subtle"
                    onClick={() => speak(word.word)}
                    className="shrink-0 bg-transparent hover:text-accent"
                  />
                )}
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
};

export default FlashcardResultPage;
