import type { Skill } from './common';

export type ChatRole = 'USER' | 'ASSISTANT';

/** Liên kết bài học mà trợ lý gợi ý kèm trong câu trả lời. */
export interface ChatSuggestionLink {
  label: string;
  path: string;
  skill: Skill;
}

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  createdAt: string;
  /** Chỉ có ở câu trả lời của trợ lý. */
  links?: ChatSuggestionLink[];
  /**
   * Đánh dấu câu trả lời từ chối vì câu hỏi nằm ngoài phạm vi học tiếng Anh.
   * Giao diện hiển thị nhẹ nhàng hơn, không coi là lỗi.
   */
  isRefusal?: boolean;
}

export interface ChatConversation {
  id: string;
  title: string;
  /** Đoạn đầu của tin nhắn cuối, dùng cho danh sách hội thoại. */
  preview: string;
  updatedAt: string;
  messageCount: number;
}

export interface ChatConversationDetail extends ChatConversation {
  messages: ChatMessage[];
}
