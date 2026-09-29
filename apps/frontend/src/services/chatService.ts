import { http, postSse } from "../shared/api/client";
import { ApiError } from "../shared/api/types";
import type {
  ChatConversation,
  ChatMessage,
  ChatConversationDetail,
  ChatQuota,
  SendMessageResult,
} from "../types/chat";

export const chatService = {
  listConversations: (): Promise<ChatConversation[]> =>
    http.get<ChatConversation[]>("/chat/conversations"),

  getConversation: (id: string): Promise<ChatConversationDetail> =>
    http.get<ChatConversationDetail>(`/chat/conversations/${id}`),

  createConversation: (): Promise<ChatConversationDetail> =>
    http.post<ChatConversationDetail>("/chat/conversations"),

  deleteConversation: (id: string): Promise<{ deleted: boolean }> =>
    http.delete<{ deleted: boolean }>(`/chat/conversations/${id}`),

  /** Gửi tin nhắn, nhận lại cả tin của người dùng lẫn câu trả lời. */
  sendMessage: (
    conversationId: string,
    content: string,
  ): Promise<SendMessageResult> =>
    http.post<SendMessageResult>(
      `/chat/conversations/${conversationId}/messages`,
      { content },
    ),

  /** Số lượt nhắn còn lại hôm nay. */
  quota: (): Promise<ChatQuota> => http.get<ChatQuota>("/chat/quota"),

  /** Câu hỏi gợi ý cho hội thoại trống. */
  starters: (): Promise<string[]> => http.get<string[]>("/chat/starters"),
};

/**
 * Gửi tin và nhận câu trả lời theo luồng: `update` được gọi để thêm/sửa tin trợ lý đang đến dở, rồi thay tin
 * tạm của người dùng (`optimisticId`) bằng tin thật. Lỗi thì tin trợ lý dở bị bỏ. Trả về số lượt còn lại.
 */
export async function sendMessageStreaming(
  conversationId: string,
  content: string,
  optimisticId: string,
  update: (change: (previous: ChatMessage[]) => ChatMessage[]) => void,
): Promise<number> {
  const streamId = `stream-${Date.now()}`;
  const out: { done?: SendMessageResult; error?: ApiError } = {};
  let received = "";

  try {
    await postSse(
      `/chat/conversations/${conversationId}/messages/stream`,
      { content },
      (event, data) => {
        if (event === "delta") {
          const first = received === "";
          received += (data as { t: string }).t;
          const text = received;
          update((prev) =>
            first
              ? [
                  ...prev,
                  {
                    id: streamId,
                    role: "ASSISTANT",
                    content: text,
                    createdAt: new Date().toISOString(),
                    typing: true,
                  },
                ]
              : prev.map((m) =>
                  m.id === streamId ? { ...m, content: text } : m,
                ),
          );
        } else if (event === "done") {
          out.done = data as SendMessageResult;
        } else if (event === "error") {
          const e = data as {
            message: string;
            code: string;
            messageKey?: string;
          };
          out.error = new ApiError(0, e.message, e.code, undefined, {
            messageKey: e.messageKey,
          });
        }
      },
    );
    if (out.error) throw out.error;
    if (!out.done) {
      throw new ApiError(0, "Luồng bị ngắt.", "NETWORK", undefined, {
        messageKey: "errors.network",
      });
    }
  } catch (streamError) {
    update((prev) => prev.filter((m) => m.id !== streamId));
    throw streamError;
  }

  const { userMessage, reply, remaining } = out.done;
  // Giữ id của tin đang hiện dần để React không dựng lại (mất hiệu ứng).
  update((prev) => [
    ...prev.filter((m) => m.id !== optimisticId && m.id !== streamId),
    userMessage,
    { ...reply, id: streamId, typing: true },
  ]);
  return remaining;
}

export default chatService;
