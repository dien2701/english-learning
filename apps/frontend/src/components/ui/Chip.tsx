import React, { type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';

import type { Level } from '../../types/common';

type Tone = 'brand' | 'neutral' | 'success' | 'warning' | 'danger' | 'info' | 'violet';

const TONE_CLASS: Record<Tone, string> = {
  brand: 'bg-accent-soft text-accent',
  neutral: 'bg-surface-muted text-ink-muted',
  success: 'bg-success-bg text-success-fg',
  warning: 'bg-warning-bg text-warning-fg',
  danger: 'bg-danger-bg text-danger-fg',
  info: 'bg-info-bg text-info-fg',
  violet: 'bg-level-advanced-bg text-level-advanced-fg',
};

interface ChipProps {
  children: ReactNode;
  tone?: Tone;
  /** Tên icon Material Symbols đặt trước nhãn. */
  icon?: string;
  className?: string;
}

/** Nhãn nhỏ bo tròn, dùng cho trình độ, trạng thái, kỹ năng. */
export const Chip: React.FC<ChipProps> = ({
  children,
  tone = 'neutral',
  icon,
  className = '',
}) => (
  <span
    className={`inline-flex items-center gap-1 rounded-pill px-2.5 py-1 text-[11.5px] font-bold leading-none ${TONE_CLASS[tone]} ${className}`}
  >
    {icon && (
      <span aria-hidden="true" className="material-symbols-outlined text-[14px]">
        {icon}
      </span>
    )}
    {children}
  </span>
);

const LEVEL_TONE: Record<Level, Tone> = {
  BEGINNER: 'success',
  INTERMEDIATE: 'warning',
  ADVANCED: 'violet',
};

/** Chip trình độ, mỗi mức một màu riêng để phân biệt bằng cả chữ lẫn màu. */
export const LevelChip: React.FC<{ level: Level }> = ({ level }) => {
  const { t } = useTranslation();
  return <Chip tone={LEVEL_TONE[level]}>{t(`level.${level}`)}</Chip>;
};

export default Chip;
