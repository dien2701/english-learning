import React from 'react';
import { useParams } from 'react-router-dom';

import { ButtonLink, IconButton } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useSpeech } from '../../hooks/useSpeech';
import { flashcardService } from '../../services/contentService';

import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import WordImage from '../../components/flashcard/WordImage';

const FlashcardDetailPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  const { speak, isSupported } = useSpeech();

  const { data: deck, isLoading, error, reload } = useApi(
    () => flashcardService.getDeck(id),
    [id],
  );

  if (error) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <ErrorState message={describe(error)} onRetry={reload} />
        </div>
      </div>
    );
  }

  if (isLoading || !deck) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[200px] w-full" />
        <Skeleton className="mt-5 h-[400px] w-full" />
      </div>
    );
  }

  const remaining = deck.totalCards - deck.learnedCards;

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={L(deck.title)}
        description={L(deck.description)}
        backTo={{ label: t('flashcard.allDecks'), to: '/flashcard' }}
      />

      <Card className="mb-5">
        <div className="flex flex-wrap items-center gap-2">
          <LevelChip level={deck.level} />
          <Chip tone="neutral">{L(deck.topicName)}</Chip>
          <Chip tone="neutral" icon="style">
            {deck.totalCards} {t('common.cards')}
          </Chip>
        </div>

        <div className="mt-5 max-w-md">
          <div className="flex items-baseline justify-between gap-2">
            <span className="text-caption text-ink-muted">
              {t('flashcard.learnedOf', {
                done: deck.learnedCards,
                total: deck.totalCards,
              })}
            </span>
            <span className="text-[13px] font-extrabold text-accent">
              {deck.progressPercent}%
            </span>
          </div>
          <div
            role="progressbar"
            aria-valuenow={deck.progressPercent}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-label={t('flashcard.sessionProgress')}
            className="mt-1.5 h-2 overflow-hidden rounded-pill bg-surface-muted"
          >
            <div
              className="h-full rounded-pill bg-brand-500"
              style={{ width: `${deck.progressPercent}%` }}
            />
          </div>
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <ButtonLink
            to={`/flashcard/${deck.id}/study`}
            size="lg"
            icon="arrow_forward"
            iconPosition="end"
          >
            {deck.learnedCards > 0
            ? t('flashcard.continueStudy')
            : t('flashcard.startStudy')}
          </ButtonLink>

          {remaining > 0 && deck.learnedCards > 0 && (
            <span className="inline-flex min-h-[46px] items-center text-[13.5px] text-ink-muted">
              {t('flashcard.cardsLeft', { count: remaining })}
            </span>
          )}
        </div>
      </Card>

      <Card>
        <h2 className="text-card-title text-ink">{t('flashcard.wordList')}</h2>

        <ul className="mt-3 divide-y divide-hairline">
          {deck.cards.map((card) => (
            <li key={card.id} className="flex items-start gap-3 py-3.5">
              <WordImage
                word={card.word}
                src={card.imageUrl}
                size="sm"
                className="h-14 w-14 shrink-0 rounded-md"
              />
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-baseline gap-x-2.5 gap-y-1">
                  <span className="text-[15px] font-extrabold text-ink">
                    {card.word}
                  </span>
                  {card.phonetic && (
                    <span className="text-[13px] text-ink-subtle">{card.phonetic}</span>
                  )}
                  {card.partOfSpeech && (
                    <span className="text-[12px] italic text-ink-subtle">
                      {L(card.partOfSpeech)}
                    </span>
                  )}
                </div>

                <p className="mt-0.5 text-[13.5px] text-ink-muted">{L(card.meaning)}</p>

                {card.example && (
                  <p className="mt-1 text-[12.5px] italic text-ink-subtle">
                    {card.example}
                  </p>
                )}
              </div>

              {card.recallLevel && (
                <Chip
                  tone={
                    card.recallLevel === 'REMEMBERED'
                      ? 'success'
                      : card.recallLevel === 'ALMOST_REMEMBERED'
                        ? 'warning'
                        : 'danger'
                  }
                >
                  {t(`recall.${card.recallLevel}`)}
                </Chip>
              )}

              {isSupported && (
                <IconButton
                  icon="volume_up"
                  label={t('flashcard.pronounce', { word: card.word })}
                  variant="subtle"
                  onClick={() => speak(card.word)}
                  className="shrink-0 bg-transparent hover:text-accent"
                />
              )}
            </li>
          ))}
        </ul>
      </Card>
    </div>
  );
};

export default FlashcardDetailPage;
