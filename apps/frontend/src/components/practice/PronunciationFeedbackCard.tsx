import React, { useMemo } from 'react';
import { Tooltip } from 'antd';
import { useTranslation } from 'react-i18next';

import { Card } from '../ui/Card';
import { Chip } from '../ui/Chip';
import type { SpeakingPromptAssessment, SpeakingWordIssue } from '../../types/speaking';

interface PronunciationFeedbackCardProps {
  /** Câu mẫu người học đã đọc. */
  promptText: string;
  assessment: SpeakingPromptAssessment;
}

const WORD_PATTERN = /([\p{L}\p{N}']+)/u;

function normalize(word: string): string {
  return word.toLowerCase().replace(/^'+|'+$/g, '');
}

/** Tô từ lỗi trong câu mẫu: gạch chân lượn kèm đậm chữ (không chỉ dựa vào màu), tooltip hiện nghe thành gì và mẹo. */
const HighlightedSentence: React.FC<{ text: string; issues: SpeakingWordIssue[] }> = ({
  text,
  issues,
}) => {
  const { t } = useTranslation();
  const byWord = useMemo(
    () => new Map(issues.map((i) => [normalize(i.word), i])),
    [issues],
  );

  return (
    <p className="text-[18px] font-bold leading-relaxed text-ink">
      {text.split(WORD_PATTERN).map((part, index) => {
        const issue = byWord.get(normalize(part));
        // Phần tử lẻ của split là các từ, phần chẵn là dấu câu và khoảng trắng.
        if (index % 2 === 0 || !issue) return <span key={index}>{part}</span>;

        return (
          <Tooltip
            key={index}
            title={
              <div className="text-[13px]">
                {issue.heardAs && (
                  <div>{t('speaking.feedback.heardAs', { heard: issue.heardAs })}</div>
                )}
                {issue.tip && <div>{issue.tip}</div>}
              </div>
            }
          >
            <span
              tabIndex={0}
              className="cursor-help rounded-sm bg-danger-bg font-extrabold text-danger-fg underline decoration-wavy underline-offset-4"
            >
              {part}
            </span>
          </Tooltip>
        );
      })}
    </p>
  );
};

/** Thẻ nhận xét phát âm một câu, hiện ngay sau khi AI chấm xong. Chỉ nhận props, không gọi API. */
export const PronunciationFeedbackCard: React.FC<PronunciationFeedbackCardProps> = ({
  promptText,
  assessment,
}) => {
  const { t } = useTranslation();
  const { score, wordIssues, tips, transcript } = assessment;
  const isGood = wordIssues.length === 0;

  return (
    <div aria-live="polite">
    <Card className="mb-5">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-card-title text-ink">{t('speaking.feedback.title')}</h2>
        <Chip tone={score >= 8 ? 'success' : score >= 5 ? 'warning' : 'danger'} icon="grade">
          {t('speaking.feedback.score', { score: score.toFixed(1) })}
        </Chip>
      </div>

      <div className="mt-3">
        <HighlightedSentence text={promptText} issues={wordIssues} />
      </div>

      {transcript ? (
        <p className="mt-2 text-[13.5px] text-ink-muted">
          <span className="font-semibold">{t('speaking.transcript')}</span> {transcript}
        </p>
      ) : (
        <p className="mt-2 text-[13.5px] text-ink-muted">{t('speaking.feedback.noSpeech')}</p>
      )}

      {isGood && tips.length === 0 ? (
        <p className="mt-3 text-[14px] font-semibold text-success-fg">
          {t('speaking.feedback.allGood')}
        </p>
      ) : (
        <div className="mt-4">
          <h3 className="text-caption font-bold uppercase tracking-wide text-ink-subtle">
            {t('speaking.feedback.needsWork')}
          </h3>
          <ul className="mt-2 space-y-2">
            {wordIssues.map((issue) => (
              <li
                key={issue.word}
                className="rounded-md border border-hairline bg-surface-muted p-3 text-[13.5px] text-ink"
              >
                <span className="font-extrabold">{issue.word}</span>
                {issue.heardAs && (
                  <span className="text-ink-muted">
                    {' '}
                    · {t('speaking.feedback.heardAs', { heard: issue.heardAs })}
                  </span>
                )}
                <div className="mt-1">{issue.issue}</div>
                {issue.tip && <div className="mt-0.5 text-ink-muted">{issue.tip}</div>}
              </li>
            ))}
            {tips.map((tip) => (
              <li key={tip} className="text-[13.5px] text-ink-muted">
                {tip}
              </li>
            ))}
          </ul>
        </div>
      )}
    </Card>
    </div>
  );
};

export default PronunciationFeedbackCard;
