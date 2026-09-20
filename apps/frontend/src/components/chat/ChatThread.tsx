import React, { useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import { SKILL_ICON } from '../../types/common';
import type { ChatMessage } from '../../types/chat';

interface ChatThreadProps {
  messages: ChatMessage[];
  /** Hiện bong bóng "đang soạn" khi trợ lý chưa trả lời xong. */
  isThinking?: boolean;
  /** Câu hỏi gợi ý cho hội thoại còn trống. */
  starters?: string[];
  onPickStarter?: (text: string) => void;
  /** Bản rút gọn dùng cho widget ở sidebar. */
  compact?: boolean;
}

/** Danh sách tin nhắn của một hội thoại. */
const ChatThread: React.FC<ChatThreadProps> = ({
  messages,
  isThinking,
  starters,
  onPickStarter,
  compact,
}) => {
  const { t } = useTranslation();
  const endRef = useRef<HTMLDivElement>(null);

  // Tự cuộn xuống tin mới nhất mỗi khi có thêm tin.
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages.length, isThinking]);

  if (messages.length === 0) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center gap-4 p-6 text-center">
        <span className="grid h-12 w-12 place-items-center rounded-pill bg-accent-soft text-accent">
          <span aria-hidden="true" className="material-symbols-outlined text-[26px]">
            smart_toy
          </span>
        </span>

        <div>
          <p className="text-[15px] font-extrabold text-ink">
            {t('chat.emptyTitle')}
          </p>
          <p className="mt-1 text-[13px] text-ink-muted">{t('chat.emptyHint')}</p>
        </div>

        {starters && starters.length > 0 && (
          <ul className="flex w-full max-w-md flex-col gap-2">
            {starters.map((starter) => (
              <li key={starter}>
                <button
                  type="button"
                  onClick={() => onPickStarter?.(starter)}
                  className="w-full rounded-md border border-hairline px-3.5 py-2.5 text-left text-[13px] text-ink transition-colors hover:bg-surface-hover"
                >
                  {starter}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  return (
    <div
      className={`flex flex-1 flex-col gap-4 overflow-y-auto ${compact ? 'p-3' : 'p-5'}`}
    >
      {messages.map((msg) => {
        const isUser = msg.role === 'USER';

        return (
          <div
            key={msg.id}
            className={`flex gap-2.5 ${isUser ? 'flex-row-reverse' : ''}`}
          >
            {!isUser && (
              <span className="grid h-8 w-8 shrink-0 place-items-center rounded-pill bg-accent-soft text-accent">
                <span
                  aria-hidden="true"
                  className="material-symbols-outlined text-[17px]"
                >
                  smart_toy
                </span>
              </span>
            )}

            <div className={`min-w-0 ${compact ? 'max-w-[85%]' : 'max-w-[75%]'}`}>
              <div
                className={`rounded-lg px-3.5 py-2.5 text-[14px] leading-relaxed ${
                  isUser
                    ? 'bg-action text-white'
                    : msg.isRefusal
                      ? 'bg-surface-muted text-ink-muted'
                      : 'bg-surface-muted text-ink'
                }`}
              >
                <p className="whitespace-pre-wrap">{msg.content}</p>
              </div>

              {/* Bài học gợi ý đi kèm câu trả lời */}
              {msg.links && msg.links.length > 0 && (
                <ul className="mt-2 flex flex-col gap-1.5">
                  {msg.links.map((link) => (
                    <li key={link.path}>
                      <Link
                        to={link.path}
                        className="flex min-h-[40px] items-center gap-2 rounded-md border border-hairline bg-surface px-3 text-[13px] font-semibold text-ink transition-colors hover:border-action hover:text-accent"
                      >
                        <span
                          aria-hidden="true"
                          className="material-symbols-outlined text-[18px] text-accent"
                        >
                          {SKILL_ICON[link.skill]}
                        </span>
                        <span className="min-w-0 flex-1 truncate">{link.label}</span>
                        <span
                          aria-hidden="true"
                          className="material-symbols-outlined text-[17px] text-ink-subtle"
                        >
                          arrow_forward
                        </span>
                      </Link>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        );
      })}

      {isThinking && (
        <div className="flex gap-2.5" role="status" aria-live="polite">
          <span className="grid h-8 w-8 shrink-0 place-items-center rounded-pill bg-accent-soft text-accent">
            <span aria-hidden="true" className="material-symbols-outlined text-[17px]">
              smart_toy
            </span>
          </span>
          <div className="flex items-center gap-1 rounded-lg bg-surface-muted px-4 py-3">
            <span className="sr-only">{t('chat.thinking')}</span>
            {[0, 1, 2].map((i) => (
              <span
                key={i}
                aria-hidden="true"
                className="h-1.5 w-1.5 animate-pulse rounded-pill bg-ink-subtle"
                style={{ animationDelay: `${i * 150}ms` }}
              />
            ))}
          </div>
        </div>
      )}

      <div ref={endRef} />
    </div>
  );
};

export default ChatThread;
