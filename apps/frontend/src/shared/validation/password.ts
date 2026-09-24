import type { TFunction } from 'i18next';

/** Khớp PasswordPolicy ở backend: tối thiểu 8 ký tự, có chữ cái, chữ số và ký tự đặc biệt. */
export const PASSWORD_PATTERN = /^(?=.*\p{L})(?=.*\p{N})(?=.*[^\p{L}\p{N}\s]).{8,}$/u;

export const passwordRule = (t: TFunction) => ({
  pattern: PASSWORD_PATTERN,
  message: t('auth.validation.passwordMin'),
});
