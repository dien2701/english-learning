import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import useLanguage from '../../hooks/useLanguage';

/** Phần tối thiểu của Web Speech API (chưa có trong lib.dom). */
interface SpeechRecognitionLike {
  lang: string;
  interimResults: boolean;
  onresult: ((e: { results: ArrayLike<ArrayLike<{ transcript: string }>> }) => void) | null;
  onend: (() => void) | null;
  onerror: (() => void) | null;
  start: () => void;
  stop: () => void;
}

const SpeechRecognitionCtor: (new () => SpeechRecognitionLike) | undefined =
  typeof window === 'undefined'
    ? undefined
    : (window as unknown as Record<string, new () => SpeechRecognitionLike>)
        .SpeechRecognition ??
      (window as unknown as Record<string, new () => SpeechRecognitionLike>)
        .webkitSpeechRecognition;

interface ChatComposerProps {
  onSend: (text: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

/** Ô soạn tin nhắn. Enter để gửi, Shift+Enter để xuống dòng. */
const ChatComposer: React.FC<ChatComposerProps> = ({
  onSend,
  disabled,
  placeholder,
}) => {
  const { t } = useTranslation();
  const { language } = useLanguage();
  const [text, setText] = useState('');
  const [listening, setListening] = useState(false);
  const recognitionRef = useRef<SpeechRecognitionLike | null>(null);

  useEffect(() => () => recognitionRef.current?.stop(), []);

  const toggleVoice = () => {
    if (!SpeechRecognitionCtor) return;
    if (listening) {
      recognitionRef.current?.stop();
      return;
    }
    const rec = new SpeechRecognitionCtor();
    rec.lang = language === 'vi' ? 'vi-VN' : 'en-US';
    rec.interimResults = true;
    const base = text.trim() ? `${text.trim()} ` : '';
    rec.onresult = (e) => {
      let spoken = '';
      for (let i = 0; i < e.results.length; i += 1) {
        spoken += e.results[i][0].transcript;
      }
      setText(base + spoken);
    };
    rec.onend = () => setListening(false);
    rec.onerror = () => setListening(false);
    recognitionRef.current = rec;
    setListening(true);
    rec.start();
  };

  const send = () => {
    const trimmed = text.trim();
    if (!trimmed || disabled) return;
    recognitionRef.current?.stop();
    onSend(trimmed);
    setText('');
  };

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        send();
      }}
      className="flex items-end gap-2 border-t border-hairline p-3"
    >
      <label htmlFor="chat-input" className="sr-only">
        {t('chat.inputLabel')}
      </label>

      <textarea
        id="chat-input"
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            send();
          }
        }}
        rows={1}
        placeholder={placeholder ?? t('chat.placeholder')}
        disabled={disabled}
        /* Dùng chung kiểu ô nhập với phần còn lại: nền mint nhạt, viền đậm
           hơn hairline, focus thì viền dày 2px kèm quầng sáng. */
        className="max-h-32 min-h-[44px] flex-1 resize-none rounded-md border border-field-border bg-field px-3.5 py-2.5 text-[14px] text-ink outline-none transition-colors placeholder:text-ink-subtle focus:border-2 focus:border-field-ring focus:bg-field-focus focus:px-[13px] disabled:opacity-60"
      />

      {SpeechRecognitionCtor && (
        <button
          type="button"
          onClick={toggleVoice}
          disabled={disabled}
          aria-pressed={listening}
          aria-label={listening ? t('chat.voiceStop') : t('chat.voiceStart')}
          className={`grid h-11 w-11 shrink-0 place-items-center rounded-pill border border-field-border transition-colors duration-200 disabled:cursor-not-allowed disabled:opacity-40 ${
            listening ? 'animate-pulse bg-action text-white' : 'bg-field text-ink'
          }`}
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[21px]">
            {listening ? 'stop' : 'mic'}
          </span>
        </button>
      )}

      <button
        type="submit"
        disabled={disabled || text.trim() === ''}
        aria-label={t('chat.send')}
        className="grid h-11 w-11 shrink-0 place-items-center rounded-pill bg-action text-white transition-colors duration-200 hover:bg-action-hover disabled:cursor-not-allowed disabled:opacity-40"
      >
        <span aria-hidden="true" className="material-symbols-outlined text-[21px]">
          send
        </span>
      </button>
    </form>
  );
};

export default ChatComposer;
