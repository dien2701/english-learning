import React, { forwardRef, type ReactNode } from 'react';
import { Link, type LinkProps } from 'react-router-dom';

/**
 * Nút dùng chung cho toàn hệ thống.
 *
 * Trước đây mỗi trang tự viết `<button className="...">` với tổ hợp class
 * riêng, nên cùng một loại hành động lại khác cỡ, khác bo góc, khác trạng
 * thái hover. Component này gom lại thành năm biến thể và ba cỡ cố định,
 * khớp với nút của Ant Design (bo pill, chữ 600, cao 44px) để hai hệ nhìn
 * như một.
 *
 * Quy ước theo STYLEGUIDE:
 * - Mỗi màn hình chỉ một nút `primary`.
 * - Vùng chạm tối thiểu 44×44px, riêng cỡ `sm` dành cho thanh công cụ dày
 *   đặc thì 40px.
 * - Không xoá vòng focus.
 * - Icon dùng Material Symbols, không dùng emoji.
 */

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'subtle'
  | 'danger'
  | 'link';

export type ButtonSize = 'sm' | 'md' | 'lg';

const VARIANT: Record<ButtonVariant, string> = {
  /* Nền #15803D + chữ trắng = 5.01:1, đạt AA ở cả hai chế độ. */
  primary:
    'bg-action text-white shadow-brand hover:bg-action-hover active:bg-action-hover',
  secondary:
    'bg-surface text-ink border border-hairline-strong hover:bg-surface-hover hover:border-field-border-hover',
  subtle: 'bg-surface-muted text-ink hover:bg-surface-hover',
  /* Đỏ nhạt chứ không đỏ đặc: các nút này chỉ mở hộp xác nhận, mà hộp
     xác nhận của Ant Design đã có nút đỏ đặc cho bước thực sự phá huỷ.
     Đỏ đặc lặp lại trên từng dòng bảng sẽ át hết mọi thứ khác. */
  danger: 'bg-danger-bg text-danger-fg hover:brightness-95',
  link: 'bg-transparent text-accent hover:underline px-0',
};

const SIZE: Record<ButtonSize, string> = {
  sm: 'min-h-[40px] px-4 text-[13px] gap-1.5',
  md: 'min-h-[44px] px-5 text-[14px] gap-2',
  lg: 'min-h-[52px] px-7 text-[15px] gap-2',
};

const ICON_ONLY_SIZE: Record<ButtonSize, string> = {
  sm: 'min-h-[40px] w-10 px-0 gap-0',
  md: 'min-h-[44px] w-11 px-0 gap-0',
  lg: 'min-h-[52px] w-13 px-0 gap-0',
};

const ICON_SIZE: Record<ButtonSize, string> = {
  sm: 'text-[17px]',
  md: 'text-[19px]',
  lg: 'text-[21px]',
};

const BASE =
  'inline-flex items-center justify-center rounded-pill font-bold leading-none ' +
  'transition-[background-color,border-color,opacity,box-shadow] duration-200 ' +
  'disabled:cursor-not-allowed disabled:opacity-55 disabled:shadow-none';

interface CommonProps {
  variant?: ButtonVariant;
  size?: ButtonSize;
  /** Tên icon Material Symbols, ví dụ `refresh`. Không dùng emoji. */
  icon?: string;
  iconPosition?: 'start' | 'end';
  /** Chiếm trọn chiều ngang — dùng cho form và màn hình hẹp. */
  block?: boolean;
  /** Hiện vòng quay và khoá nút; giữ nguyên bề rộng để layout không nhảy. */
  loading?: boolean;
  className?: string;
  children?: ReactNode;
}

function buildClass({
  variant = 'primary',
  size = 'md',
  block,
  iconOnly,
  className = '',
}: {
  variant?: ButtonVariant;
  size?: ButtonSize;
  block?: boolean;
  iconOnly: boolean;
  className?: string;
}) {
  return [
    BASE,
    VARIANT[variant],
    variant === 'link' ? 'min-h-[40px] gap-1.5 text-[13.5px]' : null,
    variant === 'link' ? null : iconOnly ? ICON_ONLY_SIZE[size] : SIZE[size],
    block ? 'w-full' : null,
    className,
  ]
    .filter(Boolean)
    .join(' ');
}

/** Vòng quay dựng bằng SVG để không phụ thuộc icon font khi mạng chậm. */
const Spinner: React.FC<{ size: ButtonSize }> = ({ size }) => (
  <svg
    aria-hidden="true"
    viewBox="0 0 20 20"
    className={`animate-spin ${ICON_SIZE[size]}`}
    style={{ width: '1em', height: '1em' }}
  >
    <circle
      cx="10"
      cy="10"
      r="8"
      fill="none"
      stroke="currentColor"
      strokeOpacity="0.28"
      strokeWidth="3"
    />
    <path
      d="M10 2a8 8 0 0 1 8 8"
      fill="none"
      stroke="currentColor"
      strokeWidth="3"
      strokeLinecap="round"
    />
  </svg>
);

function renderContent({
  icon,
  iconPosition = 'start',
  loading,
  size = 'md',
  children,
}: CommonProps) {
  const iconNode = loading ? (
    <Spinner size={size} />
  ) : icon ? (
    <span aria-hidden="true" className={`material-symbols-outlined ${ICON_SIZE[size]}`}>
      {icon}
    </span>
  ) : null;

  return (
    <>
      {iconPosition === 'start' && iconNode}
      {children}
      {iconPosition === 'end' && iconNode}
    </>
  );
}

type ButtonProps = CommonProps &
  Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, 'children' | 'className'>;

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = 'primary',
      size = 'md',
      icon,
      iconPosition = 'start',
      block,
      loading,
      className,
      children,
      disabled,
      type = 'button',
      ...rest
    },
    ref,
  ) => (
    <button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={buildClass({
        variant,
        size,
        block,
        iconOnly: !children,
        className,
      })}
      {...rest}
    >
      {renderContent({ icon, iconPosition, loading, size, children })}
    </button>
  ),
);

Button.displayName = 'Button';

type ButtonLinkProps = CommonProps & Omit<LinkProps, 'children' | 'className'>;

/** Cùng hình dạng với Button nhưng điều hướng bằng react-router. */
export const ButtonLink: React.FC<ButtonLinkProps> = ({
  variant = 'primary',
  size = 'md',
  icon,
  iconPosition = 'start',
  block,
  loading,
  className,
  children,
  ...rest
}) => (
  <Link
    className={buildClass({
      variant,
      size,
      block,
      iconOnly: !children,
      className,
    })}
    {...rest}
  >
    {renderContent({ icon, iconPosition, loading, size, children })}
  </Link>
);

/**
 * Nút chỉ có icon. Bắt buộc truyền `label` vì nút không chữ mà thiếu nhãn
 * thì trình đọc màn hình chỉ đọc được tên icon.
 */
interface IconButtonProps
  extends Omit<ButtonProps, 'children' | 'icon' | 'iconPosition'> {
  icon: string;
  label: string;
}

export const IconButton: React.FC<IconButtonProps> = ({
  icon,
  label,
  variant = 'subtle',
  size = 'md',
  className = '',
  ...rest
}) => (
  <Button
    variant={variant}
    size={size}
    icon={icon}
    aria-label={label}
    title={label}
    className={className}
    {...rest}
  />
);

export default Button;
