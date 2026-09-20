import React, { useCallback, useEffect, useRef, useState } from 'react';
import { App, Modal } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import ExamTimer from '../../components/practice/ExamTimer';
import QuestionList from '../../components/practice/QuestionList';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useCountdown } from '../../hooks/useCountdown';
import { examService } from '../../services/contentService';
import type { AnswerSubmission } from '../../types/practice';
import { useLanguage } from '../../hooks/useLanguage';

const ExamPracticePage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();

  const { data: exam, isLoading, error, reload } = useApi(
    () => examService.get(id),
    [id],
  );

  const [answers, setAnswers] = useState<Record<string, AnswerSubmission>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const hasSubmitted = useRef(false);
  const elapsedRef = useRef(0);
  const answersRef = useRef(answers);

  const submit = useCallback(
    async (isTimeout = false) => {
      if (hasSubmitted.current) return;
      hasSubmitted.current = true;
      setIsSubmitting(true);

      try {
        const result = await examService.submit(
          id,
          Object.values(answersRef.current),
          elapsedRef.current,
        );

        if (isTimeout) {
          message.warning(t('exam.timeUp'));
        }

        navigate(`/exam/result/${result.attemptId}`, {
          state: { result },
          replace: true,
        });
      } catch (submitError) {
        hasSubmitted.current = false;
        message.error(describe(submitError, 'errors.submitFailed'));
        setIsSubmitting(false);
      }
    },
    [id, navigate, message, describe, t],
  );

  const timeLimitSeconds = (exam?.timeLimitMinutes ?? 0) * 60;

  const { remaining, elapsed } = useCountdown(
    timeLimitSeconds,
    () => void submit(true),
    Boolean(exam),
  );
  /* Đồng bộ trong effect thay vì ghi ref lúc render. Hai giá trị này chỉ
     dùng bên trong submit khi hết giờ, không tham gia vào việc dựng UI. */
  useEffect(() => {
    answersRef.current = answers;
    elapsedRef.current = elapsed;
  }, [answers, elapsed]);

  const answeredCount = Object.keys(answers).length;
  const totalQuestions = exam?.questions.length ?? 0;

  const handleSubmitClick = () => {
    const unanswered = totalQuestions - answeredCount;

    Modal.confirm({
      title: t('exam.submitTitle'),
      content:
        unanswered > 0
          ? t('exam.submitWithUnanswered', { count: unanswered })
          : t('exam.submitAllDone'),
      okText: t('common.submit'),
      cancelText: t('exam.review'),
      onOk: () => submit(),
    });
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

  if (isLoading || !exam) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[560px] w-full" />
      </div>
    );
  }

  const progress = Math.round((answeredCount / Math.max(1, totalQuestions)) * 100);

  return (
    <div className="mx-auto w-full max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={L(exam.title)}
        backTo={{ label: t('exam.allExams'), to: '/exam' }}
        action={
          <ExamTimer
            remaining={timeLimitSeconds > 0 ? remaining : null}
            elapsed={elapsed}
          />
        }
      />

      {/* Thanh tiến độ trả lời, dính lại khi cuộn để luôn thấy được */}
      <div className="sticky top-[76px] z-20 mb-5 rounded-lg border border-hairline bg-surface p-4 shadow-sm">
        <div className="flex items-baseline justify-between gap-2">
          <span className="text-[13px] font-bold text-ink">
            {t('exam.answeredOf', {
              done: answeredCount,
              total: totalQuestions,
            })}
          </span>
          <span className="text-[12.5px] tabular-nums text-ink-muted">{progress}%</span>
        </div>

        <div
          role="progressbar"
          aria-valuenow={progress}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={t('exam.progressLabel')}
          className="mt-2 h-1.5 overflow-hidden rounded-pill bg-surface-muted"
        >
          <div
            className="h-full rounded-pill bg-brand-500 transition-[width] duration-300"
            style={{ width: `${progress}%` }}
          />
        </div>

        {/* Nhảy nhanh tới từng câu */}
        <div className="mt-3 flex flex-wrap gap-1.5">
          {exam.questions.map((question) => {
            const isAnswered = Boolean(answers[question.id]);

            return (
              <a
                key={question.id}
                href={`#${question.id}`}
                aria-label={t('exam.goToQuestion', {
                  order: question.order,
                  state: isAnswered
                    ? t('exam.answered')
                    : t('exam.notAnswered'),
                })}
                className={`grid h-8 w-8 place-items-center rounded-md text-[12.5px] font-bold transition-colors ${
                  isAnswered
                    ? 'bg-action text-white'
                    : 'bg-surface-muted text-ink-muted hover:bg-surface-hover'
                }`}
              >
                {question.order}
              </a>
            );
          })}
        </div>
      </div>

      <QuestionList
        questions={exam.questions}
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
  );
};

export default ExamPracticePage;
