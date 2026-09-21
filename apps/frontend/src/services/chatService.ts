import { http } from '../shared/api/client';
import type {
  ChatConversation,
  ChatConversationDetail,
  ChatQuota,
  SendMessageResult,
} from '../types/chat';

export const chatService = {
  listConversations: (): Promise<ChatConversation[]> =>
    http.get<ChatConversation[]>('/chat/conversations'),

  getConversation: (id: string): Promise<ChatConversationDetail> =>
    http.get<ChatConversationDetail>(`/chat/conversations/${id}`),

  createConversation: (): Promise<ChatConversationDetail> =>
    http.post<ChatConversationDetail>('/chat/conversations'),

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
  quota: (): Promise<ChatQuota> => http.get<ChatQuota>('/chat/quota'),

  /** Câu hỏi gợi ý cho hội thoại trống. */
  starters: (): Promise<string[]> => http.get<string[]>('/chat/starters'),
};

export default chatService;
