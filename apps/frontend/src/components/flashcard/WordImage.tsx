import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import PhotoCredit from '../ui/PhotoCredit';

interface WordImageProps {
  word: string;
  src: string | null;
  /** Lớp bọc ngoài; dùng để đặt kích thước và bo góc. */
  className?: string;
  /** Kích thước chữ của khối dự phòng. */
  size?: 'sm' | 'lg';
  /** Ghi công Unsplash; chỉ hiện ở kích thước lớn, đủ chỗ để đọc. */
  imageAuthor?: string | null;
  imageAuthorUrl?: string | null;
}

/** Quá thời gian này mà ảnh chưa xong thì chuyển sang khối dự phòng. */
const LOAD_TIMEOUT_MS = 5000;

/**
 * Ảnh minh hoạ cho một từ vựng.
 *
 * Ảnh lấy từ nguồn ngoài nên có ba kiểu hỏng: tải lỗi, bị chặn, hoặc
 * request treo không bao giờ trả lời. Trường hợp treo không kích hoạt
 * onError, nên ngoài onError còn cần một ngưỡng chờ — nếu không, chỗ
 * ảnh sẽ trống trơn mãi giữa thẻ.
 */
const WordImage: React.FC<WordImageProps> = ({
  word,
  src,
  className = '',
  size = 'lg',
  imageAuthor,
  imageAuthorUrl,
}) => {
  const { t } = useTranslation();

  /* Trạng thái gắn liền với đường dẫn ảnh: đổi sang từ khác là tự coi
     như chưa tải, không cần effect để đặt lại state. */
  const [state, setState] = useState<{
    src: string | null;
    status: 'loading' | 'loaded' | 'failed';
  }>({ src, status: 'loading' });

  const current = state.src === src ? state : { src, status: 'loading' as const };
  const isLoaded = current.status === 'loaded';
  const hasFailed = current.status === 'failed';

  const imgRef = useRef<HTMLImageElement | null>(null);

  useEffect(() => {
    if (!src || isLoaded || hasFailed) return;

    const timer = window.setTimeout(() => {
      // Ảnh có thể đã nằm sẵn trong bộ nhớ đệm và không bắn sự kiện nào.
      const img = imgRef.current;
      const ok = Boolean(img?.complete && img.naturalWidth > 0);
      setState({ src, status: ok ? 'loaded' : 'failed' });
    }, LOAD_TIMEOUT_MS);

    return () => window.clearTimeout(timer);
  }, [src, isLoaded, hasFailed]);

  const showFallback = !src || hasFailed;

  return (
    <div className={`relative overflow-hidden bg-surface-muted ${className}`}>
      {/* Khối dự phòng nằm dưới, ảnh phủ lên khi tải xong. Cách này giữ
          chỗ sẵn nên layout không nhảy lúc ảnh về. */}
      <div
        aria-hidden="true"
        className="flex h-full w-full flex-col items-center justify-center gap-1 bg-accent-soft text-accent"
      >
        <span
          className={`font-extrabold uppercase leading-none ${
            size === 'lg' ? 'text-[34px]' : 'text-[20px]'
          }`}
        >
          {word.trim().charAt(0)}
        </span>
        <span
          className={`material-symbols-outlined opacity-70 ${
            size === 'lg' ? 'text-[22px]' : 'text-[16px]'
          }`}
        >
          image
        </span>
      </div>

      {!showFallback && (
        <img
          ref={imgRef}
          src={src}
          alt={t('flashcard.imageAlt', { word })}
          loading="lazy"
          onLoad={() => setState({ src, status: 'loaded' })}
          onError={() => setState({ src, status: 'failed' })}
          className={`absolute inset-0 h-full w-full object-cover transition-opacity duration-300 ${
            isLoaded ? 'opacity-100' : 'opacity-0'
          }`}
        />
      )}

      {size === 'lg' && isLoaded && (
        <PhotoCredit
          author={imageAuthor}
          authorUrl={imageAuthorUrl}
          className="absolute bottom-1.5 right-2 text-white/85 drop-shadow"
        />
      )}
    </div>
  );
};

export default WordImage;
