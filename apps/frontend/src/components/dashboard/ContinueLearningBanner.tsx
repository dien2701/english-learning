import React from 'react';
import { useTranslation } from 'react-i18next';

import { ButtonLink } from '../ui/Button';
import { Chip, LevelChip } from '../ui/Chip';
import { EmptyBlock } from '../ui/StateBlocks';
import { SKILL_ICON } from '../../types/common';
import type { ContinueLearning } from '../../types/dashboard';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { useLanguage } from '../../hooks/useLanguage';

/**
 * Banner mở đầu Dashboard: ảnh học tập bên phải, bài đang học dở bên trái,
 * kèm nút đưa thẳng về đúng chỗ người dùng đang dừng.
 *
 * Đây là thứ duy nhất được phép nổi bật trên màn hình này — mọi khối còn
 * lại giữ tông trầm để mắt không bị chia sự chú ý.
 */
const ContinueLearningBanner: React.FC<{ item: ContinueLearning | null }> = ({
  item,
}) => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { skill } = useLabels();
  const { relativeTime } = useFormat();

  if (!item) {
    return (
      <section className="rounded-lg border border-hairline bg-surface shadow-sm">
        <EmptyBlock
          icon="rocket_launch"
          title={t('dashboard.emptyTitle')}
          message={t('dashboard.emptyHint')}
          action={
            <ButtonLink to="/flashcard">{t('dashboard.startLearning')}</ButtonLink>
          }
        />
      </section>
    );
  }

  return (
    <section className="overflow-hidden rounded-lg border border-hairline bg-surface shadow-sm">
      <div className="grid gap-0 md:grid-cols-[1fr_auto]">
        <div className="order-2 p-5 sm:p-7 md:order-1">
          <div className="flex flex-wrap items-center gap-2">
            <Chip tone="brand" icon={SKILL_ICON[item.skill]}>
              {skill(item.skill)}
            </Chip>
            <LevelChip level={item.level} />
            <span className="text-caption text-ink-subtle">
              {t('dashboard.lastStudied', {
                time: relativeTime(item.lastStudiedAt).toLowerCase(),
              })}
            </span>
          </div>

          <h2 className="mt-3 text-[22px] font-extrabold leading-tight tracking-tight text-ink sm:text-[26px]">
            {L(item.title)}
          </h2>

          <div className="mt-5 max-w-md">
            <div className="flex items-baseline justify-between gap-2">
              <span className="text-caption text-ink-muted">
                {t('dashboard.doneItems', {
                  done: item.completedItems,
                  total: item.totalItems,
                })}
              </span>
              <span className="text-[13px] font-extrabold text-accent">
                {item.progress}%
              </span>
            </div>

            <div
              role="progressbar"
              aria-valuenow={item.progress}
              aria-valuemin={0}
              aria-valuemax={100}
              aria-label={t('dashboard.progressOf', { title: L(item.title) })}
              className="mt-1.5 h-2 overflow-hidden rounded-pill bg-surface-muted"
            >
              <div
                className="h-full rounded-pill bg-brand-500 transition-[width] duration-500 ease-out"
                style={{ width: `${item.progress}%` }}
              />
            </div>
          </div>

          <ButtonLink
            to={item.resumePath}
            size="lg"
            icon="arrow_forward"
            iconPosition="end"
            className="mt-6"
          >
            {t('dashboard.continueLearning')}
          </ButtonLink>
        </div>

        {/* Ảnh học tập. Tỉ lệ cố định để không nhảy layout khi ảnh tải xong. */}
        <div className="order-1 aspect-[16/7] w-full overflow-hidden bg-surface-muted md:order-2 md:aspect-auto md:w-[320px] lg:w-[380px]">
          <img
            src={item.coverUrl}
            alt=""
            loading="lazy"
            className="h-full w-full object-cover"
          />
        </div>
      </div>
    </section>
  );
};

export default ContinueLearningBanner;
