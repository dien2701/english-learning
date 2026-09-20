import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

import ChatComposer from './ChatComposer';
import ChatThread from './ChatThread';
import { chatService } from '../../services/chatService';
import type { ChatMessage } from '../../types/chat';

/**
 * Cửa sổ chat thu nhỏ gắn cố định ở góc màn hình, mở ra từ sidebar.
 *
 * Widget giữ một hội thoại riêng, tạo mới ngay lần mở đầu tiên. Người dùng
 * muốn xem lại toàn bộ lịch sử thì bấm sang trang Trợ lý AI.
 */
const ChatWidget: React.FC<{ isOpen: boolean; onClose: () => void }> = ({
  isOpen,
  onClose,
}) => {
  const { t } = useTranslation();
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [starters, setStarters] = useState<string[]>([]);
  const [isThinking, setIsThinking] = useState(false);

  const panelRef = useRef<HTMLDivElement>(null);

  // Lần mở đầu tiên thì tạo hội thoại và lấy câu hỏi gợi ý.
  useEffect(() => {
    if (!isOpen || conversationId) return;

    let active = true;

    void (async () => {
      try {
        const [conversation, starterList] = await Promise.all([
          chatService.createConversation(),
          chatService.starters(),
        ]);
        if (!active) return;
        setConversationId(conversation.id);
        setStarters(starterList);
      } catch {
        // Không mở được thì để người dùng dùng trang chat đầy đủ.
      }
    })();

    return () => {
      active = false;
    };
  }, [isOpen, conversationId]);

  // Đóng bằng phím Esc.
  useEffect(() => {
    if (!isOpen) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };

    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [isOpen, onClose]);

  const send = useCallback(
    async (text: string) => {
      if (!conversationId) return;

      const optimistic: ChatMessage = {
        id: `tmp-${Date.now()}`,
        role: 'USER',
        content: text,
        createdAt: new Date().toISOString(),
      };

      setMessages((prev) => [...prev, optimistic]);
      setIsThinking(true);

      try {
        const { userMessage, reply } = await chatService.sendMessage(
          conversationId,
          text,
        );
        // Thay tin tạm bằng tin thật từ server rồi nối câu trả lời.
        setMessages((prev) => [
          ...prev.filter((m) => m.id !== optimistic.id),
          userMessage,
          reply,
        ]);
      } catch {
        setMessages((prev) => [
          ...prev,
          {
            id: `err-${Date.now()}`,
            role: 'ASSISTANT',
            content: t('chat.sendError'),
            createdAt: new Date().toISOString(),
          },
        ]);
      } finally {
        setIsThinking(false);
      }
    },
    [conversationId, t],
  );

  if (!isOpen) return null;

  return (
    <div
      ref={panelRef}
      role="dialog"
      aria-label={t('chat.widgetTitle')}
      className="fixed bottom-4 right-4 z-50 flex h-[min(34rem,calc(100vh-2rem))] w-[min(23rem,calc(100vw-2rem))] flex-col overflow-hidden rounded-lg border border-hairline bg-surface shadow-lg"
    >
      <header className="flex items-center gap-2.5 border-b border-hairline px-4 py-3">
        <span className="grid h-8 w-8 shrink-0 place-items-center rounded-pill bg-action text-white">
          <span aria-hidden="true" className="material-symbols-outlined text-[18px]">
            smart_toy
          </span>
        </span>

        <div className="min-w-0 flex-1">
          <p className="text-[14px] font-extrabold text-ink">
            {t('chat.widgetTitle')}
          </p>
          <p className="text-[11.5px] text-ink-muted">{t('chat.widgetSubtitle')}</p>
        </div>

        <Link
          to="/chat"
          onClick={onClose}
          aria-label={t('chat.expand')}
          className="grid h-9 w-9 place-items-center rounded-md text-ink-muted transition-colors hover:bg-surface-hover hover:text-ink"
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[19px]">
            open_in_full
          </span>
        </Link>

        <button
          type="button"
          onClick={onClose}
          aria-label={t('chat.closeWidget')}
          className="grid h-9 w-9 place-items-center rounded-md text-ink-muted transition-colors hover:bg-surface-hover hover:text-ink"
        >
          <span aria-hidden="true" className="material-symbols-outlined text-[20px]">
            close
          </span>
        </button>
      </header>

      <ChatThread
        compact
        messages={messages}
        isThinking={isThinking}
        starters={starters}
        onPickStarter={send}
      />

      <ChatComposer onSend={send} disabled={!conversationId || isThinking} />
    </div>
  );
};

export default ChatWidget;
