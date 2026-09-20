import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { formatClock } from '../../hooks/useCountdown';

interface AudioPlayerProps {
  /** Đường dẫn tệp âm thanh. Bỏ trống thì dùng giọng đọc của trình duyệt. */
  src?: string;
  /** Văn bản để đọc khi không có tệp âm thanh. */
  speakText?: string;
  /** Thời lượng ước tính, dùng cho thanh tiến trình ở chế độ giọng đọc. */
  estimatedSeconds: number;
  label?: string;
}

const SPEEDS = [0.75, 1, 1.25, 1.5];

/**
 * Trình phát cho bài luyện nghe.
 *
 * Bản chạy thử chưa có tệp mp3 thật, nên khi `src` để trống trình phát sẽ
 * đọc transcript bằng Web Speech API. Nhờ vậy người dùng vẫn nghe được
 * thật sự thay vì nhìn một thanh tiến trình chạy suông. Khi nối backend
 * và có đường dẫn Cloudinary, truyền `src` vào là chuyển sang thẻ audio.
 */
const AudioPlayer: React.FC<AudioPlayerProps> = ({
  src,
  speakText,
  estimatedSeconds,
  label,
}) => {
  const { t } = useTranslation();
  const [isPlaying, setIsPlaying] = useState(false);
  const [elapsed, setElapsed] = useState(0);
  const [speed, setSpeed] = useState(1);

  const audioRef = useRef<HTMLAudioElement | null>(null);
  const tickRef = useRef<number | null>(null);

  const usesSpeech = !src;

  // Suy ra trực tiếp, không cần state: có tệp âm thanh thì luôn dùng được,
  // còn khi phải nhờ giọng đọc thì phụ thuộc vào trình duyệt.
  const isSupported =
    !usesSpeech || (typeof window !== 'undefined' && 'speechSynthesis' in window);

  /** Dừng hẳn và đưa con trỏ về đầu. */
  const stopAll = useCallback(() => {
    if (tickRef.current !== null) {
      window.clearInterval(tickRef.current);
      tickRef.current = null;
    }
    if (usesSpeech && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    audioRef.current?.pause();
    setIsPlaying(false);
  }, [usesSpeech]);

  // Rời khỏi trang thì phải tắt tiếng, nếu không giọng đọc vẫn chạy tiếp.
  useEffect(() => stopAll, [stopAll]);

  const startTicking = useCallback(() => {
    if (tickRef.current !== null) window.clearInterval(tickRef.current);

    tickRef.current = window.setInterval(() => {
      setElapsed((prev) => {
        const next = prev + 0.25 * speed;
        return next >= estimatedSeconds ? estimatedSeconds : next;
      });
    }, 250);
  }, [speed, estimatedSeconds]);

  const play = useCallback(() => {
    if (usesSpeech) {
      if (!('speechSynthesis' in window) || !speakText) return;

      window.speechSynthesis.cancel();

      const utterance = new SpeechSynthesisUtterance(speakText);
      utterance.lang = 'en-US';
      utterance.rate = speed;
      utterance.onend = () => {
        stopAll();
        setElapsed(estimatedSeconds);
      };

      window.speechSynthesis.speak(utterance);
      startTicking();
      setIsPlaying(true);
      return;
    }

    void audioRef.current?.play();
    setIsPlaying(true);
  }, [usesSpeech, speakText, speed, estimatedSeconds, startTicking, stopAll]);

  const pause = useCallback(() => {
    if (usesSpeech) {
      // speechSynthesis.pause không đáng tin trên một số trình duyệt,
      // nên dừng hẳn và cho phép phát lại từ đầu.
      stopAll();
      return;
    }
    audioRef.current?.pause();
    setIsPlaying(false);
  }, [usesSpeech, stopAll]);

  const restart = useCallback(() => {
    stopAll();
    setElapsed(0);
    if (!usesSpeech && audioRef.current) audioRef.current.currentTime = 0;
  }, [stopAll, usesSpeech]);

  const changeSpeed = (next: number) => {
    setSpeed(next);
    if (audioRef.current) audioRef.current.playbackRate = next;
    if (usesSpeech && isPlaying) {
      // Đổi tốc độ giữa chừng thì phải đọc lại từ đầu.
      restart();
    }
  };

  const progress =
    estimatedSeconds === 0 ? 0 : Math.min(100, (elapsed / estimatedSeconds) * 100);

  if (usesSpeech && !isSupported) {
    return (
      <div
        role="alert"
        className="rounded-lg border border-hairline bg-warning-bg p-4 text-[13.5px] text-warning-fg"
      >
        {t('listening.noSpeech')}
      </div>
    );
  }

  return (
    <section
      aria-label={label ?? t('listening.player')}
      className="rounded-lg border border-hairline bg-surface p-5 shadow-sm"
    >
      {src && (
        <audio
          ref={audioRef}
          src={src}
          onTimeUpdate={(e) => setElapsed(e.currentTarget.currentTime)}
          onEnded={() => setIsPlaying(false)}
          preload="metadata"
        />
      )}

      <div className="flex items-center gap-4">
        <button
          type="button"
          onClick={isPlaying ? pause : play}
          aria-label={isPlaying ? t('listening.pause') : t('listening.play')}
          className="grid h-12 w-12 shrink-0 place-items-center rounded-pill bg-action text-white shadow-brand transition-colors duration-200 hover:bg-action-hover"
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
            {isPlaying ? 'pause' : 'play_arrow'}
          </span>
        </button>

        <button
          type="button"
          onClick={restart}
          aria-label={t('listening.replay')}
          className="grid h-10 w-10 shrink-0 place-items-center rounded-pill text-ink-muted transition-colors duration-200 hover:bg-surface-hover hover:text-ink"
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[21px]">
            replay
          </span>
        </button>

        <div className="min-w-0 flex-1">
          <div
            role="progressbar"
            aria-valuenow={Math.round(progress)}
            aria-valuemin={0}
            aria-valuemax={100}
            aria-label={t('listening.playProgress')}
            className="h-1.5 overflow-hidden rounded-pill bg-surface-muted"
          >
            <div
              className="h-full rounded-pill bg-brand-500 transition-[width] duration-200"
              style={{ width: `${progress}%` }}
            />
          </div>

          <div className="mt-1.5 flex justify-between text-[11.5px] tabular-nums text-ink-subtle">
            <span>{formatClock(elapsed)}</span>
            <span>{formatClock(estimatedSeconds)}</span>
          </div>
        </div>

        <div
          role="group"
          aria-label={t('listening.speed')}
          className="hidden items-center gap-1 rounded-pill bg-surface-muted p-1 sm:flex"
        >
          {SPEEDS.map((value) => (
            <button
              key={value}
              type="button"
              onClick={() => changeSpeed(value)}
              aria-pressed={speed === value}
              className={`min-h-[28px] rounded-pill px-2.5 text-[12px] font-bold transition-colors duration-200 ${
                speed === value
                  ? 'bg-surface text-accent shadow-xs'
                  : 'text-ink-muted hover:text-ink'
              }`}
            >
              {value}×
            </button>
          ))}
        </div>
      </div>

      {usesSpeech && (
        <p className="mt-3 text-caption text-ink-subtle">
          {t('listening.speechFallback')}
        </p>
      )}
    </section>
  );
};

export default AudioPlayer;
