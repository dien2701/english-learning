import React, { useCallback, useEffect, useState } from 'react';
import { App } from 'antd';
import { useTranslation } from 'react-i18next';

import ChatComposer from '../../components/chat/ChatComposer';
import ChatStatusBar from '../../components/chat/ChatStatusBar';
import ChatThread from '../../components/chat/ChatThread';
import { Button, IconButton } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApiError } from '../../hooks/useApiError';
import { useChatQuota } from '../../hooks/useChatQuota';
import { useFormat } from '../../hooks/useFormat';
import { chatService } from '../../services/chatService';
import { ApiError } from '../../shared/api/types';
import type { ChatConversation, ChatMessage } from '../../types/chat';

const ChatPage: React.FC = () => {
  const { message: toast } = App.useApp();
  const { t } = useTranslation();
  const { relativeTime } = useFormat();
  const { describe } = useApiError();

  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [activeId, setActiveId] = useState<string | null>(null);
  const [starters, setStarters] = useState<string[]>([]);

  /* Nội dung hội thoại được giữ kèm id của chính nó. Nhờ vậy suy ra được
     ngay là đang tải hay đã có dữ liệu, không cần một cờ loading riêng
     phải đồng bộ bằng tay. */
  const [thread, setThread] = useState<{
    id: string;
    messages: ChatMessage[];
  } | null>(null);

  const [isLoadingList, setIsLoadingList] = useState(true);
  const [isThinking, setIsThinking] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const { quota, setRemaining, markExhausted, exhausted } = useChatQuota();
  /** Lần gửi gần nhất bị AI lỗi: giữ nội dung để nút "Thử lại" gửi lại đúng tin đó. */
  const [failure, setFailure] = useState<{
    conversationId: string;
    optimisticId: string;
    text: string;
    message: string;
  } | null>(null);

  /** Tăng lên để nạp lại danh sách hội thoại. */
  const [listNonce, setListNonce] = useState(0);

  const messages = thread?.id === activeId ? thread.messages : [];
  const isLoadingThread = activeId !== null && thread?.id !== activeId;

  /** Thêm hoặc sửa tin nhắn của hội thoại đang mở. */
  const updateMessages = useCallback(
    (update: (previous: ChatMessage[]) => ChatMessage[]) => {
      setThread((previous) =>
        previous ? { ...previous, messages: update(previous.messages) } : previous,
      );
    },
    [],
  );

  /* Nạp danh sách hội thoại. Mọi setState đều nằm trong callback của
     promise nên không có cập nhật đồng bộ ngay trong thân effect. */
  useEffect(() => {
    let active = true;

    Promise.all([chatService.listConversations(), chatService.starters()])
      .then(([list, starterList]) => {
        if (!active) return;
        setConversations(list);
        setStarters(starterList);
        setActiveId((current) => current ?? list[0]?.id ?? null);
        setError(null);
      })
      .catch((loadError: unknown) => {
        if (!active) return;
        setError(
          loadError instanceof ApiError
            ? loadError
            : new ApiError(0, 'chat.loadError', 'UNKNOWN', undefined, {
                messageKey: 'chat.loadError',
              }),
        );
      })
      .finally(() => {
        if (active) setIsLoadingList(false);
      });

    return () => {
      active = false;
    };
  }, [listNonce]);

  /** Thử lại sau khi lỗi. */
  const retry = () => {
    setIsLoadingList(true);
    setListNonce((n) => n + 1);
  };

  // Tải nội dung hội thoại đang chọn.
  useEffect(() => {
    const id = activeId;
    if (!id) return;

    let active = true;

    chatService
      .getConversation(id)
      .then((conversation) => {
        if (active) setThread({ id, messages: conversation.messages });
      })
      .catch(() => {
        if (active) setThread({ id, messages: [] });
      });

    return () => {
      active = false;
    };
  }, [activeId]);

  const startNew = async () => {
    try {
      const conversation = await chatService.createConversation();
      setConversations((prev) => [conversation, ...prev]);
      setThread({ id: conversation.id, messages: [] });
      setActiveId(conversation.id);
    } catch {
      toast.error(t('chat.createError'));
    }
  };

  const remove = async (id: string) => {
    try {
      await chatService.deleteConversation(id);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeId === id) {
        setThread(null);
        setActiveId(null);
      }
    } catch {
      toast.error(t('chat.deleteError'));
    }
  };

  const send = async (text: string) => {
    let targetId = activeId;

    // Chưa có hội thoại nào thì tạo mới rồi gửi luôn.
    if (!targetId) {
      try {
        const conversation = await chatService.createConversation();
        targetId = conversation.id;
        setConversations((prev) => [conversation, ...prev]);
        setThread({ id: conversation.id, messages: [] });
        setActiveId(conversation.id);
      } catch {
        toast.error(t('chat.createError'));
        return;
      }
    }

    const optimistic: ChatMessage = {
      id: `tmp-${Date.now()}`,
      role: 'USER',
      content: text,
      createdAt: new Date().toISOString(),
    };

    updateMessages((prev) => [...prev, optimistic]);
    setFailure(null);
    setIsThinking(true);

    try {
      const { userMessage, reply, remaining } = await chatService.sendMessage(
        targetId,
        text,
      );
      setRemaining(remaining);
      updateMessages((prev) => [
        ...prev.filter((m) => m.id !== optimistic.id),
        userMessage,
        reply,
      ]);
      // Cập nhật lại danh sách để tiêu đề và đoạn xem trước đúng.
      const list = await chatService.listConversations();
      setConversations(list);
    } catch (sendError: unknown) {
      if (sendError instanceof ApiError && sendError.code === 'CHAT_DAILY_LIMIT') {
        // Hết lượt: backend không lưu tin, bỏ tin tạm và khoá ô nhập.
        markExhausted();
        updateMessages((prev) => prev.filter((m) => m.id !== optimistic.id));
      } else {
        setFailure({
          conversationId: targetId,
          optimisticId: optimistic.id,
          text,
          message: describe(sendError, 'chat.sendError'),
        });
      }
    } finally {
      setIsThinking(false);
    }
  };

  /** Gửi lại đúng tin vừa lỗi (tin tạm cũ được thay bằng tin mới). */
  const retrySend = () => {
    if (!failure || failure.conversationId !== activeId) return;
    const { optimisticId, text } = failure;
    updateMessages((prev) => prev.filter((m) => m.id !== optimisticId));
    void send(text);
  };

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('chat.title')}
        description={t('chat.subtitle')}
        action={
          <Button icon="add" onClick={startNew}>
            {t('chat.newConversation')}
          </Button>
        }
      />

      {error ? (
        <div className="rounded-lg border border-hairline bg-surface shadow-sm">
          <ErrorState message={describe(error)} onRetry={retry} />
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
          {/* Danh sách hội thoại */}
          <aside className="lg:col-span-4 xl:col-span-3">
            <div className="rounded-lg border border-hairline bg-surface p-3 shadow-sm">
              <h2 className="px-2 py-1.5 text-caption font-bold uppercase tracking-wide text-ink-subtle">
                {t('chat.conversations')}
              </h2>

              {isLoadingList ? (
                <div className="flex flex-col gap-2 p-1">
                  {[0, 1, 2].map((i) => (
                    <Skeleton key={i} className="h-16 w-full" />
                  ))}
                </div>
              ) : conversations.length === 0 ? (
                <p className="px-2 py-4 text-[13px] text-ink-muted">
                  {t('chat.noConversations')}
                </p>
              ) : (
                <ul className="flex flex-col gap-1">
                  {conversations.map((conversation) => (
                    <li key={conversation.id} className="group relative">
                      <button
                        type="button"
                        onClick={() => setActiveId(conversation.id)}
                        aria-current={activeId === conversation.id}
                        className={`w-full rounded-md p-2.5 pr-9 text-left transition-colors ${
                          activeId === conversation.id
                            ? 'bg-accent-subtle'
                            : 'hover:bg-surface-hover'
                        }`}
                      >
                        <span className="block truncate text-[13.5px] font-bold text-ink">
                          {conversation.title}
                        </span>
                        <span className="mt-0.5 block truncate text-[12px] text-ink-muted">
                          {conversation.preview || t('chat.noMessages')}
                        </span>
                        <span className="mt-1 block text-[11px] text-ink-subtle">
                          {relativeTime(conversation.updatedAt)}
                        </span>
                      </button>

                      <IconButton
                        icon="delete"
                        label={t('chat.deleteConversation', {
                          title: conversation.title,
                        })}
                        size="sm"
                        onClick={() => remove(conversation.id)}
                        className="absolute right-1.5 top-1.5 min-h-[32px] w-8 bg-transparent text-ink-subtle opacity-0 hover:bg-danger-bg hover:text-danger focus-visible:opacity-100 group-hover:opacity-100"
                      />
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </aside>

          {/* Khung hội thoại */}
          <section className="lg:col-span-8 xl:col-span-9">
            <div className="flex h-[min(38rem,calc(100vh-13rem))] flex-col overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm">
              {isLoadingThread ? (
                <div className="flex-1 p-5">
                  <Skeleton className="h-full w-full" />
                </div>
              ) : (
                <ChatThread
                  messages={messages}
                  isThinking={isThinking}
                  starters={starters}
                  onPickStarter={send}
                />
              )}

              <ChatStatusBar
                quota={quota}
                exhausted={exhausted}
                errorMessage={
                  failure?.conversationId === activeId ? failure?.message : null
                }
                onRetry={retrySend}
              />
              <ChatComposer onSend={send} disabled={isThinking || exhausted} />
            </div>
          </section>
        </div>
      )}
    </div>
  );
};

export default ChatPage;
