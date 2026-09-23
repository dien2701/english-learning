import React from 'react';
import { useTranslation } from 'react-i18next';

interface PhotoCreditProps {
  author?: string | null;
  authorUrl?: string | null;
  /** Lớp của khối bọc ngoài (định vị, màu chữ). */
  className?: string;
}

/** Dòng ghi công ảnh Unsplash, đặt lên trên ảnh hoặc ngay dưới ảnh. Không có tác giả thì không hiện gì. */
const PhotoCredit: React.FC<PhotoCreditProps> = ({ author, authorUrl, className = '' }) => {
  const { t } = useTranslation();
  if (!author) return null;

  const content = t('common.photoBy', { author });
  return (
    <span className={`truncate text-[11px] ${className}`}>
      {authorUrl ? (
        <a
          href={authorUrl}
          target="_blank"
          rel="noreferrer noopener"
          className="hover:underline"
          onClick={(e) => e.stopPropagation()}
        >
          {content}
        </a>
      ) : (
        content
      )}
    </span>
  );
};

export default PhotoCredit;
