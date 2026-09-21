import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { App, Modal } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';

import { Button, IconButton } from '../../components/ui/Button';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useSpeech } from '../../hooks/useSpeech';
import { useStudyHeartbeat } from '../../hooks/useStudyHeartbeat';
import { flashcardService } from '../../services/contentService';
import type { Flashcard, RecallLevel } from '../../types/flashcard';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';
import WordImage from '../../components/flashcard/WordImage';

const RECALL_BUTTONS: Array<{
  level: RecallLevel;
  icon: string;
  className: string;
}> = [
  {
    level: 'NOT_REMEMBERED',
    icon: 'close',
    className: 'bg-danger-bg text-danger-fg hover:bg-danger-bg/80',
  },
  {
    level: 'ALMOST_REMEMBERED',
    icon: 'change_history',
    className: 'bg-warning-bg text-warning-fg hover:bg-warning-bg/80',
  },
  {
    level: 'REMEMBERED',
    icon: 'check',
    className: 'bg-success-bg text-success-fg hover:bg-success-bg/80',
  },
];

const FlashcardStudyPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  useStudyHeartbeat('VOCABULARY', id);
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { speak, stop, isSupported } = useSpeech();

  const { data: deck, isLoading, error, reload } = useApi(
    () => flashcardService.getDeck(id),
    [id],
  );

  const [index, setIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [studiedIds, setStudiedIds] = useState<string[]>([]);
  const [isSaving, setIsSaving] = useState(false);

  const cards: Flashcard[] = useMemo(() => deck?.cards ?? [], [deck]);
  const card = cards[index];

  const finish = useCallback(
    async (ids: string[]) => {
      stop();
      try {
        const result = await flashcardService.finishSession(id, ids);
        navigate(`/flashcard/${id}/result`, { state: { result }, replace: true });
      } catch {
        message.error(t('flashcard.finishError'));
      }
    },
    [id, navigate, message, stop, t],
  );

  const handleRate = async (level: RecallLevel) => {
    if (!card || isSaving) return;

    setIsSaving(true);
    try {
      // Chỉ gửi id thẻ và mức độ nhớ; ngày ôn tiếp theo do backend tính.
      await flashcardService.saveRecall(id, {
        flashcardId: card.id,
        recallLevel: level,
      });

      const nextStudied = studiedIds.includes(card.id)
        ? studiedIds
        : [...studiedIds, card.id];
      setStudiedIds(nextStudied);

      if (index + 1 >= cards.length) {
        await finish(nextStudied);
        return;
      }

      setIndex((i) => i + 1);
      setIsFlipped(false);
    } catch {
      message.error(t('flashcard.saveError'));
    } finally {
      setIsSaving(false);
    }
  };

  /** Rời phiên giữa chừng: tiến độ từng thẻ đã lưu nên không mất gì. */
  const confirmExit = () => {
    Modal.confirm({
      title: t('flashcard.exitTitle'),
      content: t('flashcard.exitBody'),
      okText: t('flashcard.exitConfirm'),
      cancelText: t('flashcard.exitCancel'),
      onOk: () => {
        stop();
        navigate(`/flashcard/${id}`);
      },
    });
  };

  /* Phím tắt: Space lật thẻ, 1/2/3 đánh giá, mũi tên để chuyển thẻ. */
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement;
      if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA') return;

      if (event.code === 'Space') {
        event.preventDefault();
        setIsFlipped((v) => !v);
        return;
      }

      if (!isFlipped) return;

      if (event.key === '1') void handleRate('NOT_REMEMBERED');
      if (event.key === '2') void handleRate('ALMOST_REMEMBERED');
      if (event.key === '3') void handleRate('REMEMBERED');
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  });

  if (error) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6">
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <ErrorState message={describe(error)} onRetry={reload} />
        </div>
      </div>
    );
  }

  if (isLoading || !deck || !card) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6">
        <Skeleton className="h-[420px] w-full" />
      </div>
    );
  }

  const progress = Math.round(((index + 1) / cards.length) * 100);

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
      {/* Thanh tiến độ phiên học */}
      <div className="mb-5 flex items-center gap-4">
        <IconButton
          icon="close"
          label={t('flashcard.exitAria')}
          variant="subtle"
          onClick={confirmExit}
          className="shrink-0 bg-transparent"
        />

        <div className="min-w-0 flex-1">
          <div className="flex items-baseline justify-between gap-2">
            <span className="truncate text-[13px] font-bold text-ink">
              {L(deck.title)}
            </span>
            <span className="shrink-0 text-[12.5px] tabular-nums text-ink-muted">
              {index + 1}/{cards.length}
            </span>
          </div>
          <div
            role="progressbar"
            aria-valuenow={progress}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-label={t('flashcard.sessionProgress')}
            className="mt-1.5 h-1.5 overflow-hidden rounded-pill bg-surface-muted"
          >
            <div
              className="h-full rounded-pill bg-brand-500 transition-[width] duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      </div>

      {/* Thẻ từ vựng */}
      <button
        type="button"
        onClick={() => setIsFlipped((v) => !v)}
        aria-pressed={isFlipped}
        className="flex min-h-[320px] w-full flex-col items-center justify-center gap-4 rounded-xl border border-hairline bg-surface p-8 text-center shadow-md transition-shadow duration-200 hover:shadow-lg"
      >
        {!isFlipped ? (
          <>
            <WordImage
              word={card.word}
              src={card.imageUrl}
              className="h-40 w-full max-w-sm rounded-lg"
            />
            <p className="text-[34px] font-extrabold tracking-tight text-ink sm:text-[42px]">
              {card.word}
            </p>
            {card.phonetic && (
              <p className="text-[16px] text-ink-muted">{card.phonetic}</p>
            )}
            <p className="mt-2 text-caption text-ink-subtle">
              {t('flashcard.flipHint')}
            </p>
          </>
        ) : (
          <>
            <WordImage
              word={card.word}
              src={card.imageUrl}
              size="sm"
              className="h-24 w-32 rounded-md"
            />
            <p className="text-[24px] font-extrabold text-ink sm:text-[28px]">
              {L(card.meaning)}
            </p>
            {card.partOfSpeech && (
              <p className="text-[13px] italic text-ink-subtle">{L(card.partOfSpeech)}</p>
            )}
            {card.example && (
              <div className="mt-2 max-w-lg rounded-md bg-surface-muted p-4">
                <p className="text-[14.5px] italic text-ink">{card.example}</p>
                {card.exampleMeaning && (
                  <p className="mt-1.5 text-[13px] text-ink-muted">
                    {card.exampleMeaning}
                  </p>
                )}
              </div>
            )}
          </>
        )}
      </button>

      {/* Nút phát âm, tách khỏi thẻ để bấm không làm lật thẻ */}
      {isSupported && (
        <div className="mt-3 flex justify-center">
          <Button
            variant="subtle"
            size="sm"
            icon="volume_up"
            onClick={() => speak(card.word)}
          >
            {t('flashcard.listenPronunciation')}
          </Button>
        </div>
      )}

      {/* Ba mức tự đánh giá, chỉ hiện sau khi đã lật thẻ */}
      <div className="mt-6">
        {isFlipped ? (
          <>
            <p className="mb-3 text-center text-caption text-ink-muted">
              {t('flashcard.rateQuestion')}
            </p>
            <div className="grid grid-cols-3 gap-2.5">
              {RECALL_BUTTONS.map((button, i) => (
                <button
                  key={button.level}
                  type="button"
                  disabled={isSaving}
                  onClick={() => handleRate(button.level)}
                  className={`flex min-h-[56px] flex-col items-center justify-center gap-0.5 rounded-md text-[13.5px] font-bold transition-colors duration-200 disabled:opacity-50 ${button.className}`}
                >
                  <span
                    aria-hidden="true"
                    className="material-symbols-outlined text-[20px]"
                  >
                    {button.icon}
                  </span>
                  {t(`recall.${button.level}`)}
                  <span className="text-[10.5px] font-semibold opacity-70">
                    {t('flashcard.keyHint', { key: i + 1 })}
                  </span>
                </button>
              ))}
            </div>
          </>
        ) : (
          <p className="text-center text-caption text-ink-subtle">
            {t('flashcard.flipToRate')}
          </p>
        )}
      </div>
    </div>
  );
};

export default FlashcardStudyPage;
