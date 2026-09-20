/** Kiểu dữ liệu dùng chung cho nhiều module. */

export type Role = 'USER' | 'ADMIN';

export type AccountStatus = 'ACTIVE' | 'LOCKED' | 'PENDING';

export type Level = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

/** Sáu nhóm nội dung học tập của hệ thống. */
export type Skill =
  | 'VOCABULARY'
  | 'WRITING'
  | 'LISTENING'
  | 'READING'
  | 'SPEAKING'
  | 'EXAM';

export interface User {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  avatarUrl?: string;
  role: Role;
  status: AccountStatus;
  /** Ngày tạo tài khoản, chuẩn ISO 8601. */
  createdAt: string;
}

/** Tên icon Material Symbols tương ứng từng kỹ năng. */
export const SKILL_ICON: Record<Skill, string> = {
  VOCABULARY: 'style',
  WRITING: 'edit_note',
  LISTENING: 'headphones',
  READING: 'menu_book',
  SPEAKING: 'mic',
  EXAM: 'quiz',
};

/** Đường dẫn gốc của từng kỹ năng trong ứng dụng. */
export const SKILL_PATH: Record<Skill, string> = {
  VOCABULARY: '/flashcard',
  WRITING: '/writing',
  LISTENING: '/listening',
  READING: '/reading',
  SPEAKING: '/speaking',
  EXAM: '/exam',
};
