import React from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Thanh đo độ mạnh mật khẩu cho màn hình đăng ký.
 *
 * Luôn kèm chữ bên cạnh bốn vạch màu: STYLEGUIDE cấm dùng riêng màu sắc
 * để truyền đạt ý nghĩa, và người mù màu sẽ không đọc được nếu chỉ có
 * đỏ–vàng–xanh.
 *
 * Đây là gợi ý, không phải luật chặn: luật bắt buộc vẫn là tối thiểu 6
 * ký tự, khai báo trong `rules` của Form.
 */

type StrengthScore = 0 | 1 | 2 | 3 | 4;

/** Điểm 0 dành cho mật khẩu quá ngắn; từ 1 trở lên cộng theo độ đa dạng. */
function scorePassword(password: string): StrengthScore {
  if (password.length < 6) return 0;

  let score = 1;
  if (password.length >= 10) score += 1;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password) && /[^A-Za-z0-9]/.test(password)) score += 1;
  else if (/\d/.test(password) || /[^A-Za-z0-9]/.test(password)) {
    score += password.length >= 8 ? 1 : 0;
  }

  return Math.min(score, 4) as StrengthScore;
}

const LABEL_KEY: Record<StrengthScore, string> = {
  0: 'auth.strength.tooShort',
  1: 'auth.strength.weak',
  2: 'auth.strength.fair',
  3: 'auth.strength.good',
  4: 'auth.strength.strong',
};

/* Màu chỉ là lớp tín hiệu phụ, chữ mới là lớp chính. */
const BAR_COLOR: Record<StrengthScore, string> = {
  0: 'bg-danger',
  1: 'bg-danger',
  2: 'bg-warning',
  3: 'bg-brand-500',
  4: 'bg-action',
};

const TEXT_COLOR: Record<StrengthScore, string> = {
  0: 'text-danger-fg',
  1: 'text-danger-fg',
  2: 'text-warning-fg',
  3: 'text-accent',
  4: 'text-accent',
};

interface PasswordStrengthProps {
  password: string;
  className?: string;
}

const PasswordStrength: React.FC<PasswordStrengthProps> = ({
  password,
  className = '',
}) => {
  const { t } = useTranslation();

  if (!password) return null;

  const score = scorePassword(password);
  const label = t(LABEL_KEY[score]);

  return (
    <div className={`mt-2 ${className}`}>
      <div className="flex items-center gap-2">
        <div className="flex flex-1 gap-1" aria-hidden="true">
          {[1, 2, 3, 4].map((step) => (
            <span
              key={step}
              className={`h-1.5 flex-1 rounded-pill transition-colors duration-200 ${
                score >= step ? BAR_COLOR[score] : 'bg-hairline'
              }`}
            />
          ))}
        </div>
        <span className={`text-caption font-bold ${TEXT_COLOR[score]}`}>
          {label}
        </span>
      </div>

      {/* Trình đọc màn hình chỉ cần câu tóm tắt, không cần bốn vạch. */}
      <span className="sr-only" role="status">
        {t('auth.strengthLabel')}: {label}
      </span>
    </div>
  );
};

export default PasswordStrength;
