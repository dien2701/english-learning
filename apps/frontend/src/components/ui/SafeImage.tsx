import React, { useState } from 'react';

interface SafeImageProps {
  src?: string | null;
  alt: string;
  /** Lớp của thẻ img (kích thước, object-fit). */
  className?: string;
  /** Khối hiển thị khi không có ảnh hoặc ảnh tải lỗi. Mặc định là khối nhấn có icon ảnh. */
  fallback?: React.ReactNode;
}

const DefaultFallback: React.FC = () => (
  <div
    aria-hidden="true"
    className="grid h-full w-full place-items-center bg-accent-soft text-accent"
  >
    <span className="material-symbols-outlined text-[28px] opacity-70">image</span>
  </div>
);

/**
 * Ảnh từ nguồn ngoài có thể lỗi hoặc bị chặn; khi đó thay bằng khối dự
 * phòng theo token màu. Trạng thái lỗi gắn với đường dẫn nên đổi ảnh khác
 * là tự thử lại, không cần effect để đặt lại.
 */
const SafeImage: React.FC<SafeImageProps> = ({ src, alt, className = '', fallback }) => {
  const [failedSrc, setFailedSrc] = useState<string | null>(null);

  if (!src || failedSrc === src) return <>{fallback ?? <DefaultFallback />}</>;

  return (
    <img
      src={src}
      alt={alt}
      loading="lazy"
      onError={() => setFailedSrc(src)}
      className={className}
    />
  );
};

export default SafeImage;
