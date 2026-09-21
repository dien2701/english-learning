import React, { useState } from 'react';
import { App } from 'antd';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip, LevelChip } from '../../components/ui/Chip';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useRecorder, type Recording } from '../../hooks/useRecorder';
import { useSpeech } from '../../hooks/useSpeech';
import { formatClock } from '../../hooks/useCountdown';
import { speakingService } from '../../services/contentService';
import { useLanguage } from '../../hooks/useLanguage';

/** Thanh sóng âm vẽ theo mức âm lượng đang thu được. */
const WaveMeter: React.FC<{ level: number; isActive: boolean }> = ({
  level,
  isActive,
}) => (
  <div aria-hidden="true" className="flex h-8 items-center gap-1">
    {Array.from({ length: 16 }, (_, i) => {
      // Các cột giữa cao hơn hai bên để sóng trông tự nhiên.
      const weight = 1 - Math.abs(i - 7.5) / 9;
      const height = isActive ? Math.max(4, level * 32 * weight + 4) : 4;

      return (
        <span
          key={i}
          className={`w-1 rounded-pill transition-[height] duration-75 ${
            isActive ? 'bg-brand-500' : 'bg-surface-muted'
          }`}
          style={{ height }}
        />
      );
    })}
  </div>
);

const SpeakingPracticePage: React.FC = () => {
  const { t } = useTranslation();
  const { L, language } = useLanguage();
  const { describe, fieldErrors } = useApiError();
  const { id = '' } = useParams();
  const navigate = useNavigate();
  const { message } = App.useApp();

  const { data: lesson, isLoading, error, reload } = useApi(
    () => speakingService.get(id),
    [id],
  );

  const recorder = useRecorder();
  const { speak, isSupported: canSpeak } = useSpeech();

  const [index, setIndex] = useState(0);
  const [recordings, setRecordings] = useState<Record<string, Recording>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const prompt = lesson?.prompts[index];
  const recorded = prompt ? recordings[prompt.id] : undefined;
  const recordedCount = Object.keys(recordings).length;

  const toggleRecording = async () => {
    if (!prompt) return;

    if (recorder.status === 'RECORDING') {
      const result = await recorder.stop();
      if (result) {
        setRecordings((prev) => ({ ...prev, [prompt.id]: result }));
      } else {
        message.warning(t('speaking.noAudio'));
      }
      return;
    }

    const started = await recorder.start();
    if (!started) {
      message.error(t('speaking.micError'));
    }
  };

  const submit = async () => {
    if (!lesson || isSubmitting) return;

    setIsSubmitting(true);
    try {
      const totalDuration = Object.values(recordings).reduce(
        (sum, r) => sum + r.durationSeconds,
        0,
      );

      // Giữ đúng thứ tự câu của bài, chỉ gửi những câu đã thu.
      const result = await speakingService.submit(
        lesson.id,
        lesson.prompts
          .filter((p) => recordings[p.id])
          .map((p) => ({ promptId: p.id, blob: recordings[p.id].blob })),
        Math.round(totalDuration),
      );

      navigate(`/speaking/result/${result.attemptId}`, {
        state: { result },
        replace: true,
      });
    } catch (submitError) {
      message.error(
        fieldErrors(submitError)?.audio ?? describe(submitError, 'errors.submitFailed'),
      );
      setIsSubmitting(false);
    }
  };

  if (error) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (isLoading || !lesson || !prompt) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
        <Skeleton className="h-[480px] w-full" />
      </div>
    );
  }

  const isRecording = recorder.status === 'RECORDING';
  const isLast = index === lesson.prompts.length - 1;

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6">
      <PageHeader
        title={L(lesson.title)}
        backTo={{ label: t('speaking.allLessons'), to: '/speaking' }}
      />

      <div className="mb-5 flex flex-wrap items-center gap-2">
        <LevelChip level={lesson.level} />
        <Chip tone="neutral">{L(lesson.topicName)}</Chip>
        <Chip tone="neutral" icon="record_voice_over">
          {t('speaking.recordedCount', {
            done: recordedCount,
            total: lesson.prompts.length,
          })}
        </Chip>
      </div>

      {recorder.status === 'UNSUPPORTED' && (
        <div
          role="alert"
          className="mb-5 rounded-lg border border-hairline bg-warning-bg p-4 text-[13.5px] text-warning-fg"
        >
          {t('speaking.unsupported')}
        </div>
      )}

      {recorder.status === 'DENIED' && (
        <div
          role="alert"
          className="mb-5 rounded-lg border border-hairline bg-danger-bg p-4 text-[13.5px] text-danger-fg"
        >
          {t('speaking.denied')}
        </div>
      )}

      {/* Câu cần đọc */}
      <Card className="mb-5">
        <div className="flex items-center justify-between gap-3">
          <span className="text-caption font-bold uppercase tracking-wide text-ink-subtle">
            {t('speaking.sentenceOf', {
              current: prompt.order,
              total: lesson.prompts.length,
            })}
          </span>

          {canSpeak && (
            <Button
              variant="subtle"
              size="sm"
              icon="volume_up"
              onClick={() => speak(prompt.text)}
            >
              {t('speaking.listenSample')}
            </Button>
          )}
        </div>

        <p className="mt-4 text-[22px] font-extrabold leading-snug tracking-tight text-ink">
          {prompt.text}
        </p>

        {prompt.phonetic && (
          <p className="mt-2 text-[14px] text-ink-subtle">{prompt.phonetic}</p>
        )}

        {/* Nghĩa tiếng Việt chỉ có ích khi giao diện đang ở tiếng Việt;
            ở chế độ tiếng Anh, câu gốc đã là thứ cần đọc. */}
        {language === 'vi' && (
          <p className="mt-2.5 text-[14px] text-ink-muted">{prompt.meaning}</p>
        )}
      </Card>

      {/* Khu vực thu âm */}
      <Card className="mb-5">
        <div className="flex flex-col items-center gap-4">
          <WaveMeter level={recorder.level} isActive={isRecording} />

          <button
            type="button"
            onClick={toggleRecording}
            disabled={recorder.status === 'UNSUPPORTED' || recorder.status === 'REQUESTING'}
            className={`inline-flex min-h-[52px] items-center gap-2.5 rounded-pill px-8 text-[15px] font-bold transition-colors duration-200 disabled:opacity-50 ${
              isRecording
                ? 'bg-danger text-white'
                : 'bg-action text-white shadow-brand hover:bg-action-hover'
            }`}
          >
            <span aria-hidden="true" className="material-symbols-outlined text-[22px]">
              {isRecording ? 'stop_circle' : 'mic'}
            </span>
            {isRecording
              ? t('speaking.stopRecordingWithTime', {
                  time: formatClock(recorder.elapsed),
                })
              : recorded
                ? t('speaking.reRecord')
                : t('speaking.startRecording')}
          </button>

          {recorded && !isRecording && (
            <div className="w-full max-w-sm">
              <p className="mb-2 text-center text-caption text-ink-muted">
                {t('speaking.playback')}
              </p>
              {/* Bản ghi chỉ nằm trong trình duyệt cho tới khi bấm nộp bài. */}
              <audio
                src={recorded.url}
                controls
                className="w-full"
                aria-label={t('speaking.recordingOf', { order: prompt.order })}
              />
            </div>
          )}
        </div>
      </Card>

      {/* Điều hướng giữa các câu */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Button
          variant="subtle"
          icon="arrow_back"
          onClick={() => setIndex((i) => Math.max(0, i - 1))}
          disabled={index === 0 || isRecording}
        >
          {t('speaking.prevSentence')}
        </Button>

        {isLast ? (
          <Button
            size="lg"
            icon="auto_awesome"
            iconPosition="end"
            loading={isSubmitting}
            onClick={submit}
            disabled={recordedCount === 0 || isRecording}
          >
            {isSubmitting ? t('speaking.sending') : t('speaking.submitToAi')}
          </Button>
        ) : (
          <Button
            icon="arrow_forward"
            iconPosition="end"
            onClick={() => setIndex((i) => Math.min(lesson.prompts.length - 1, i + 1))}
            disabled={isRecording}
          >
            {t('speaking.nextSentence')}
          </Button>
        )}
      </div>
    </div>
  );
};

export default SpeakingPracticePage;
