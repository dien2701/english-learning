import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

import type { Level, Skill } from '../types/common';
import type { RecallLevel } from '../types/flashcard';
import type { WritingIssue, WritingStatus } from '../types/writing';
import type { SpeakingScores } from '../types/speaking';
import type { AudienceKey } from '../types/admin';

/**
 * Nhãn cho các giá trị enum, dịch theo ngôn ngữ đang chọn.
 *
 * Trước đây mỗi kiểu có một hằng `..._LABEL` viết cứng tiếng Việt trong
 * `src/types`, nên chọn EN thì trình độ, kỹ năng, trạng thái bài viết…
 * vẫn hiện tiếng Việt. Gom về một hook để mọi nơi lấy nhãn theo cùng một
 * đường, và bộ khoá dịch là nguồn duy nhất.
 */
interface UseLabelsResult {
  level: (value: Level) => string;
  skill: (value: Skill) => string;
  recall: (value: RecallLevel) => string;
  writingStatus: (value: WritingStatus) => string;
  issueCategory: (value: WritingIssue['category']) => string;
  speakingScore: (value: keyof SpeakingScores) => string;
  audience: (value: AudienceKey) => string;
}

export function useLabels(): UseLabelsResult {
  const { t } = useTranslation();

  return useMemo(
    () => ({
      level: (value) => t(`level.${value}`),
      skill: (value) => t(`skill.${value}`),
      recall: (value) => t(`recall.${value}`),
      writingStatus: (value) => t(`writing.status.${value}`),
      issueCategory: (value) => t(`writing.issueCategory.${value}`),
      speakingScore: (value) => t(`speaking.scores.${value}`),
      audience: (value) => t(`admin.audiences.${value}`),
    }),
    [t],
  );
}

export default useLabels;
