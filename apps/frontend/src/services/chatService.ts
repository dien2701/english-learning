import { http } from '../shared/api/client';
import type {
  ChatConversation,
  ChatConversationDetail,
  ChatMessage,
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
  ): Promise<{ userMessage: ChatMessage; reply: ChatMessage }> =>
    http.post<{ userMessage: ChatMessage; reply: ChatMessage }>(
      `/chat/conversations/${conversationId}/messages`,
      { content },
    ),

  /** Câu hỏi gợi ý cho hội thoại trống. */
  starters: (): Promise<string[]> => http.get<string[]>('/chat/starters'),
};

export default chatService;
