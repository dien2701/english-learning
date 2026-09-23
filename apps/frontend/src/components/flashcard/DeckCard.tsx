import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { Chip, LevelChip } from '../ui/Chip';
import PhotoCredit from '../ui/PhotoCredit';
import SafeImage from '../ui/SafeImage';
import type { DeckSummary } from '../../types/flashcard';
import { useLanguage } from '../../hooks/useLanguage';

/** Khoá dịch cho ba trạng thái học của một bộ từ. */
const STATUS_KEY: Record<DeckSummary['status'], string> = {
  NOT_STARTED: 'flashcard.notStarted',
  IN_PROGRESS: 'flashcard.inProgress',
  COMPLETED: 'flashcard.completed',
};

/** Thẻ hiển thị một bộ từ trong danh sách. */
const DeckCard: React.FC<{ deck: DeckSummary }> = ({ deck }) => {
  const { t } = useTranslation();
  const { L } = useLanguage();

  return (
  <Link
    to={`/flashcard/${deck.id}`}
    className="group flex flex-col overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm transition-shadow duration-200 hover:shadow-md"
  >
    <div className="relative aspect-[16/9] w-full overflow-hidden bg-surface-muted">
      <SafeImage
        src={deck.coverImageUrl}
        alt=""
        className="h-full w-full object-cover"
      />
      <span className="absolute left-3 top-3">
        <LevelChip level={deck.level} />
      </span>
      <PhotoCredit
        author={deck.coverImageAuthor}
        authorUrl={deck.coverImageAuthorUrl}
        className="absolute bottom-2 right-3 text-white/85 drop-shadow"
      />
    </div>

    <div className="flex flex-1 flex-col p-4">
      <div className="flex items-center gap-2">
        <Chip tone="neutral">{L(deck.topicName)}</Chip>
        {deck.status !== 'NOT_STARTED' && (
          <Chip tone={deck.status === 'COMPLETED' ? 'success' : 'brand'}>
            {t(STATUS_KEY[deck.status])}
          </Chip>
        )}
      </div>

      <h3 className="mt-2.5 text-[15.5px] font-extrabold leading-snug text-ink">
        {L(deck.title)}
      </h3>
      <p className="mt-1 line-clamp-2 text-[13px] text-ink-muted">
        {L(deck.description)}
      </p>

      <div className="mt-auto pt-4">
        <div className="flex items-baseline justify-between gap-2">
          <span className="text-caption text-ink-muted">
            {t('flashcard.learnedOf', {
              done: deck.learnedCards,
              total: deck.totalCards,
            })}
          </span>
          <span className="text-[12.5px] font-extrabold text-accent">
            {deck.progressPercent}%
          </span>
        </div>

        <div
          role="progressbar"
          aria-valuenow={deck.progressPercent}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={t('dashboard.progressOf', { title: L(deck.title) })}
          className="mt-1.5 h-1.5 overflow-hidden rounded-pill bg-surface-muted"
        >
          <div
            className="h-full rounded-pill bg-brand-500"
            style={{ width: `${deck.progressPercent}%` }}
          />
        </div>
      </div>
    </div>
  </Link>
  );
};

export default DeckCard;
