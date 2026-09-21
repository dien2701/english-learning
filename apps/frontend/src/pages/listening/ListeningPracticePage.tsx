import React, { useCallback, useState } from 'react';
import { App, Modal } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import AudioPlayer from '../../components/practice/AudioPlayer';
import ExamTimer from '../../components/practice/ExamTimer';
import QuestionList from '../../components/practice/QuestionList';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useCountdown } from '../../hooks/useCountdown';
import { useLeaveGuard } from '../../hooks/useLeaveGuard';
import { useStudyHeartbeat } from '../../hooks/useStudyHeartbeat';
import { listeningService } from '../../services/contentService';
import type { AnswerSubmission } from '../../types/practice';
import { useLanguage } from '../../hooks/useLanguage';
import { useTranslation } from 'react-i18next';

const ListeningPracticePage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe } = useApiError();
  const { id = '' } = useParams();
  useStudyHeartbeat('LISTENING', id);
  const navigate = useNavigate();
  const { message } = App.useApp();

  const { data: lesson, isLoading, error, reload } = useApi(
    () => listeningService.get(id),
    [id],
  );

  const [answers, setAnswers] = useState<Record<string, AnswerSubmission>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Bài nghe không giới hạn thời gian, đồng hồ chỉ ghi lại thời gian làm.
  const { elapsed } = useCountdown(0, undefined, Boolean(lesson));

  const release = useLeaveGuard(Boolean(lesson) && !isSubmitting);

  const answeredCount = Object.keys(answers).length;
  const totalQuestions = lesson?.questions.length ?? 0;

  const submit = useCallback(async () => {
    if (isSubmitting) return;

    setIsSubmitting(true);
    try {
      const result = await listeningService.submit(
        id,
        Object.values(answers),
        elapsed,
      );
      release();
      navigate(`/listening/result/${result.attemptId}`, {
        state: { result },
        replace: true,
      });
    } catch (submitError) {
      message.error(describe(submitError, 'errors.submitFailed'));
      setIsSubmitting(false);
    }
  }, [id, answers, elapsed, isSubmitting, release, navigate, message, describe]);

  const handleSubmitClick = () => {
    const unanswered = totalQuestions - answeredCount;

    if (unanswered > 0) {
      Modal.confirm({
        title: t('exam.unansweredTitle'),
        content: t('exam.unansweredBody', { count: unanswered }),
        okText: t('common.submit'),
        cancelText: t('exam.keepGoing'),
        onOk: submit,
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
        <Skeleton className="h-[520px] w-full" />
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-4xl px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={L(lesson.title)}
        description={L(lesson.description)}
        backTo={{ label: t('listening.allLessons'), to: '/listening' }}
        action={<ExamTimer remaining={null} elapsed={elapsed} />}
      />

      <div className="mb-5 flex flex-wrap items-center gap-2">
        <LevelChip level={lesson.level} />
        <Chip tone="neutral">{L(lesson.topicName)}</Chip>
        <Chip tone="neutral" icon="help">
          {totalQuestions} {t('common.questions')}
        </Chip>
      </div>

      <div className="mb-5">
        <AudioPlayer
          src={lesson.audioUrl || undefined}
          speakText={L(lesson.description)}
          estimatedSeconds={lesson.durationSeconds}
        />
      </div>

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
  );
};

export default ListeningPracticePage;
