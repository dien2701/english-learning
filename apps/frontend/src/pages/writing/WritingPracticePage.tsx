import React, { useCallback, useState } from 'react';
import { App, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import ExamTimer from '../../components/practice/ExamTimer';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useCountdown } from '../../hooks/useCountdown';
import { writingService } from '../../services/contentService';
import { useLanguage } from '../../hooks/useLanguage';

const WritingPracticePage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe, fieldErrors } = useApiError();
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();

  const { data: prompt, isLoading, error, reload } = useApi(
    () => writingService.getPrompt(id),
    [id],
  );

  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const wordCount = content.trim() ? content.trim().split(/\s+/).length : 0;
  const minWords = prompt?.minWords ?? 0;
  const meetsMinimum = wordCount >= minWords;

  const submit = useCallback(async () => {
    if (isSubmitting) return;

    setIsSubmitting(true);
    try {
      const submission = await writingService.submit(id, content);
      // Chuyển sang màn hình kết quả, nơi hiển thị trạng thái AI đang chấm.
      navigate(`/writing/${id}/result`, {
        state: { submissionId: submission.id },
        replace: true,
      });
    } catch (submitError) {
      message.error(
        fieldErrors(submitError)?.content ?? describe(submitError, 'errors.submitFailed'),
      );
      setIsSubmitting(false);
    }
  }, [id, content, isSubmitting, navigate, message, describe, fieldErrors]);

  /* Hết thời lượng gợi ý thì nhắc, nhưng không tự nộp — đây là thời gian
     tham khảo, không phải giới hạn cứng như bài kiểm tra. */
  const { elapsed } = useCountdown(0, undefined, Boolean(prompt));

  if (error) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState message={error.message} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (isLoading || !prompt) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[520px] w-full" />
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={L(prompt.title)}
        backTo={{ label: t('writing.allPrompts'), to: '/writing' }}
        action={<ExamTimer remaining={null} elapsed={elapsed} />}
      />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
        {/* Đề bài */}
        <div className="lg:col-span-4">
          <Card className="lg:sticky lg:top-[88px]">
            <div className="flex flex-wrap items-center gap-2">
              <LevelChip level={prompt.level} />
              <Chip tone="neutral">{L(prompt.topicName)}</Chip>
              <Chip tone="neutral" icon="schedule">
                {prompt.suggestedMinutes} {t('common.minutes')}
              </Chip>
            </div>

            <h2 className="mt-4 text-card-title text-ink">
              {t('writing.promptRequirement')}
            </h2>
            <p className="mt-2 text-[14px] leading-relaxed text-ink-muted">
              {prompt.prompt}
            </p>

            <h3 className="mt-5 text-[13px] font-bold uppercase tracking-wide text-ink-subtle">
              {t('writing.hints')}
            </h3>
            <ul className="mt-2 flex flex-col gap-2">
              {prompt.hints.map((hint) => (
                <li key={hint} className="flex gap-2 text-[13.5px] text-ink-muted">
                  <span
                    aria-hidden="true"
                    className="material-symbols-outlined mt-0.5 shrink-0 text-[16px] text-accent"
                  >
                    check_circle
                  </span>
                  {hint}
                </li>
              ))}
            </ul>
          </Card>
        </div>

        {/* Ô viết bài */}
        <div className="lg:col-span-8">
          <Card>
            <label
              htmlFor="writing-content"
              className="text-card-title text-ink"
            >
              {t('writing.yourEssay')}
            </label>

            <Input.TextArea
              id="writing-content"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={t('writing.placeholder')}
              autoSize={{ minRows: 16, maxRows: 30 }}
              className="mt-3 !text-[15px] !leading-relaxed"
            />

            <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
              <p
                className={`text-[13px] font-semibold ${
                  meetsMinimum ? 'text-success' : 'text-ink-muted'
                }`}
              >
                {t('writing.wordCount', { count: wordCount })}
                <span className="font-normal text-ink-subtle">
                  {' '}
                  · {t('writing.minWordsHint', { count: minWords })}
                </span>
                {meetsMinimum && (
                  <span className="ml-1.5 inline-flex items-center gap-1">
                    <span
                      aria-hidden="true"
                      className="material-symbols-outlined text-[15px]"
                    >
                      check_circle
                    </span>
                    {t('writing.met')}
                  </span>
                )}
              </p>

              <Button
                size="lg"
                icon="auto_awesome"
                iconPosition="end"
                loading={isSubmitting}
                /* Backend từ chối bài thiếu số từ tối thiểu nên chặn ngay ở đây. */
                disabled={wordCount === 0 || !meetsMinimum}
                onClick={() => void submit()}
              >
                {isSubmitting ? t('common.submitting') : t('writing.submitToAi')}
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default WritingPracticePage;
