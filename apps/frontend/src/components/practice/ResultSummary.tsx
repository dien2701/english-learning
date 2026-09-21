import React from 'react';
import { useTranslation } from 'react-i18next';

import { formatScore } from '../../utils/format';
import type { GradedAnswer, PracticeResult } from '../../types/practice';
import { formatClock } from '../../hooks/useCountdown';
import { useLanguage } from '../../hooks/useLanguage';

/** Vòng tròn hiển thị điểm tổng. */
export const ScoreRing: React.FC<{ score: number; size?: number }> = ({
  score,
  size = 128,
}) => {
  const { t } = useTranslation();
  const radius = size / 2 - 8;
  const circumference = 2 * Math.PI * radius;
  const ratio = Math.max(0, Math.min(1, score / 10));

  return (
    <div className="relative shrink-0" style={{ width: size, height: size }}>
      <svg width={size} height={size} aria-hidden="true" className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={8}
          className="stroke-surface-muted"
        />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={8}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={circumference * (1 - ratio)}
          className="stroke-action transition-[stroke-dashoffset] duration-700 ease-out"
        />
      </svg>

      <div className="absolute inset-0 grid place-items-center">
        <div className="text-center">
          <p className="text-[28px] font-extrabold leading-none tracking-tight text-ink">
            {formatScore(score)}
          </p>
          <p className="mt-0.5 text-[11px] font-semibold text-ink-subtle">
            / 10 {t('common.points')}
          </p>
        </div>
      </div>
    </div>
  );
};

/** Khối tổng hợp kết quả: điểm, số câu đúng sai, thời gian làm bài. */
export const ResultOverview: React.FC<{ result: PracticeResult }> = ({ result }) => {
  const { t } = useTranslation();
  const { L } = useLanguage();

  return (
  <section className="rounded-lg border border-hairline bg-surface p-6 shadow-sm">
    <div className="flex flex-wrap items-center gap-6">
      <ScoreRing score={result.score} />

      <div className="min-w-0 flex-1">
        <h2 className="text-card-title text-ink">{L(result.lessonTitle)}</h2>

        <dl className="mt-4 grid grid-cols-2 gap-x-6 gap-y-3 sm:grid-cols-3">
          <div>
            <dt className="text-caption text-ink-muted">{t('common.correct')}</dt>
            <dd className="mt-0.5 text-[20px] font-extrabold text-success">
              {result.correctCount}
            </dd>
          </div>
          <div>
            <dt className="text-caption text-ink-muted">{t('common.wrong')}</dt>
            <dd className="mt-0.5 text-[20px] font-extrabold text-danger">
              {result.wrongCount}
            </dd>
          </div>
          <div>
            <dt className="text-caption text-ink-muted">{t('common.duration')}</dt>
            <dd className="mt-0.5 text-[20px] font-extrabold text-ink">
              {formatClock(result.durationSeconds)}
            </dd>
          </div>
        </dl>
      </div>
    </div>
    </section>
  );
};

/** Danh sách câu trả lời kèm đáp án đúng và giải thích. */
export const AnswerReview: React.FC<{ answers: GradedAnswer[] }> = ({ answers }) => {
  const { t } = useTranslation();

  return (
  <ol className="flex flex-col gap-3">
    {answers.map((answer) => (
      <li
        key={answer.questionId}
        className={`rounded-lg border-l-4 border-y border-r border-hairline bg-surface p-5 shadow-sm ${
          answer.isCorrect ? 'border-l-success' : 'border-l-danger'
        }`}
      >
        <div className="flex items-start gap-3">
          {/* Kèm cả icon lẫn chữ, không chỉ dựa vào màu để báo đúng sai. */}
          <span
            className={`mt-0.5 grid h-6 w-6 shrink-0 place-items-center rounded-pill ${
              answer.isCorrect
                ? 'bg-success-bg text-success-fg'
                : 'bg-danger-bg text-danger-fg'
            }`}
          >
            <span aria-hidden="true" className="material-symbols-outlined text-[16px]">
              {answer.isCorrect ? 'check' : 'close'}
            </span>
          </span>

          <div className="min-w-0 flex-1">
            <p className="text-[14.5px] font-bold text-ink">
              <span className="text-accent">{answer.order}.</span> {answer.text}
            </p>

            <dl className="mt-2.5 flex flex-col gap-1.5 text-[13.5px]">
              <div className="flex flex-wrap gap-x-2">
                <dt className="text-ink-muted">{t('exam.yourAnswer')}</dt>
                <dd
                  className={`font-semibold ${
                    answer.isCorrect ? 'text-success-fg' : 'text-danger-fg'
                  }`}
                >
                  {answer.userAnswer ?? t('exam.noAnswer')}
                </dd>
                <dd className="sr-only">
                  {answer.isCorrect ? t('exam.isCorrect') : t('exam.isWrong')}
                </dd>
              </div>

              {!answer.isCorrect && (
                <div className="flex flex-wrap gap-x-2">
                  <dt className="text-ink-muted">{t('exam.correctAnswer')}</dt>
                  <dd className="font-semibold text-ink">{answer.correctAnswer}</dd>
                </div>
              )}
            </dl>

            {answer.explanation && (
              <p className="mt-2.5 rounded-md bg-surface-muted p-3 text-[13px] text-ink-muted">
                {answer.explanation}
              </p>
            )}
          </div>
        </div>
      </li>
    ))}
  </ol>
  );
};
