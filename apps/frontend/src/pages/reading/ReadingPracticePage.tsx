import React, { useCallback, useEffect, useRef, useState } from 'react';
import { App, Modal } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import ExamTimer from '../../components/practice/ExamTimer';
import QuestionList from '../../components/practice/QuestionList';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useCountdown } from '../../hooks/useCountdown';
import { readingService } from '../../services/contentService';
import type { AnswerSubmission } from '../../types/practice';
import { useLanguage } from '../../hooks/useLanguage';

const ReadingPracticePage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();

  const { data: lesson, isLoading, error, reload } = useApi(
    () => readingService.get(id),
    [id],
  );

  const [answers, setAnswers] = useState<Record<string, AnswerSubmission>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Giữ cờ riêng để hết giờ chỉ nộp đúng một lần.
  const hasSubmitted = useRef(false);

  // Đọc thời gian đã trôi bên trong submit mà không phải thêm vào deps.
  const elapsedRef = useRef(0);

  const submit = useCallback(
    async (isTimeout = false) => {
      if (hasSubmitted.current) return;
      hasSubmitted.current = true;
      setIsSubmitting(true);

      try {
        const result = await readingService.submit(
          id,
          Object.values(answers),
          elapsedRef.current,
        );

        if (isTimeout) message.warning(t('exam.timeUpAuto'));

        navigate(`/reading/result/${result.attemptId}`, {
          state: { result },
          replace: true,
        });
      } catch (submitError) {
        hasSubmitted.current = false;
        message.error(describe(submitError, 'errors.submitFailed'));
        setIsSubmitting(false);
      }
    },
    [id, answers, navigate, message, describe, t],
  );

  const timeLimitSeconds = (lesson?.timeLimitMinutes ?? 0) * 60;

  const { remaining, elapsed } = useCountdown(
    timeLimitSeconds,
    () => void submit(true),
    Boolean(lesson),
  );

  // Đồng bộ trong effect, không ghi ref lúc render.
  useEffect(() => {
    elapsedRef.current = elapsed;
  }, [elapsed]);

  const answeredCount = Object.keys(answers).length;
  const totalQuestions = lesson?.questions.length ?? 0;

  const handleSubmitClick = () => {
    const unanswered = totalQuestions - answeredCount;

    if (unanswered > 0) {
      Modal.confirm({
        title: t('exam.unansweredTitle'),
        content: t('exam.unansweredBody', { count: unanswered }),
        okText: t('common.submit'),
        cancelText: t('exam.keepGoing'),
        onOk: () => submit(),
      });
      return;
    }

    void submit();
  };

  if (error) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (isLoading || !lesson) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[560px] w-full" />
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={L(lesson.title)}
        backTo={{ label: t('reading.allLessons'), to: '/reading' }}
        action={
          <ExamTimer
            remaining={timeLimitSeconds > 0 ? remaining : null}
            elapsed={elapsed}
          />
        }
      />

      <div className="mb-5 flex flex-wrap items-center gap-2">
        <LevelChip level={lesson.level} />
        <Chip tone="neutral">{L(lesson.topicName)}</Chip>
        <Chip tone="neutral" icon="notes">
          {lesson.wordCount} {t('common.words')}
        </Chip>
      </div>

      {/* Bài đọc bên trái, câu hỏi bên phải để vừa đọc vừa trả lời */}
      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <div>
          <Card className="lg:sticky lg:top-[88px] lg:max-h-[calc(100vh-120px)] lg:overflow-y-auto">
            <h2 className="text-card-title text-ink">{t('reading.passage')}</h2>
            <div className="mt-3 flex flex-col gap-3.5">
              {lesson.passage.map((paragraph, index) => (
                <p
                  key={index}
                  className="max-w-[70ch] text-[15px] leading-[1.75] text-ink"
                >
                  {paragraph}
                </p>
              ))}
            </div>
          </Card>
        </div>

        <div>
          <div className="mb-4 flex items-center justify-between gap-3">
            <h2 className="text-section text-ink">{t('exam.questionsTitle')}</h2>
            <span className="text-caption text-ink-muted">
              {t('exam.answeredCount', {
                done: answeredCount,
                total: totalQuestions,
              })}
            </span>
          </div>

          <QuestionList
            questions={lesson.questions}
            answers={answers}
            onAnswer={(questionId, answer) =>
              setAnswers((prev) => ({ ...prev, [questionId]: answer }))
            }
          />

          <div className="mt-6 flex justify-end">
            <Button
              size="lg"
              icon="check_circle"
              iconPosition="end"
              loading={isSubmitting}
              onClick={handleSubmitClick}
            >
              {isSubmitting ? t('common.submitting') : t('common.submit')}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReadingPracticePage;
