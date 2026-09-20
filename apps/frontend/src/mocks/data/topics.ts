import type { Topic } from '../../types/practice';
import type { L10n } from '../../types/l10n';

/** Chủ đề dùng chung cho mọi kỹ năng. */
export const topics: Topic[] = [
  { id: 'tp-toeic', name: { vi: 'TOEIC', en: 'TOEIC' } },
  { id: 'tp-ielts', name: { vi: 'IELTS', en: 'IELTS' } },
  { id: 'tp-foundation', name: { vi: 'Nền tảng', en: 'Foundation' } },
  { id: 'tp-work', name: { vi: 'Công việc', en: 'Work' } },
  { id: 'tp-life', name: { vi: 'Đời sống', en: 'Everyday life' } },
  { id: 'tp-travel', name: { vi: 'Du lịch', en: 'Travel' } },
];

const FALLBACK: L10n = { vi: 'Khác', en: 'Other' };

export function topicName(id: string): L10n {
  return topics.find((t) => t.id === id)?.name ?? FALLBACK;
}
