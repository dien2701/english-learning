import React, { useCallback, useRef, useState } from 'react';
import { App, Checkbox, Form, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import AuthLayout from '../../components/auth/AuthLayout';
import PasswordStrength from '../../components/auth/PasswordStrength';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { useApiError } from '../../hooks/useApiError';
import { authService } from '../../services/authService';

interface RegisterForm {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  acceptTerms: boolean;
}

/** Kết quả hỏi server xem email đã có tài khoản chưa. */
type EmailCheck =
  | { state: 'idle' }
  | { state: 'checking' }
  | { state: 'available' };

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const RegisterPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState('');
  const [emailCheck, setEmailCheck] = useState<EmailCheck>({ state: 'idle' });
  const [form] = Form.useForm<RegisterForm>();
  const { message } = App.useApp();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { register } = useAuth();
  const { describe, applyTo } = useApiError();

  /* Người dùng có thể rời ô email nhiều lần; chỉ lời gọi cuối cùng được
     phép ghi kết quả, nếu không một phản hồi cũ về muộn sẽ đè lên. */
  const checkSeq = useRef(0);

  const onFinish = async (values: RegisterForm) => {
    setLoading(true);
    try {
      await register({
        fullName: values.fullName,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      message.success(t('auth.registerSuccess'));
      navigate('/dashboard', { replace: true });
    } catch (error) {
      // Đưa lỗi về đúng ô nhập thay vì chỉ hiện một thông báo chung.
      applyTo(form, error);
      message.error(describe(error, 'auth.registerFailed'));
    } finally {
      setLoading(false);
    }
  };

  /**
   * Hỏi server ngay khi rời ô email. Biết email đã có tài khoản từ lúc này
   * thì người dùng sửa luôn, thay vì điền hết form rồi mới nhận lỗi.
   */
  const handleEmailBlur = useCallback(
    async (event: React.FocusEvent<HTMLInputElement>) => {
      const email = event.target.value.trim();

      if (!EMAIL_PATTERN.test(email)) {
        setEmailCheck({ state: 'idle' });
        return;
      }

      const seq = ++checkSeq.current;
      setEmailCheck({ state: 'checking' });

      try {
        const { available } = await authService.checkEmail(email);
        if (seq !== checkSeq.current) return;

        if (available) {
          setEmailCheck({ state: 'available' });
        } else {
          setEmailCheck({ state: 'idle' });
          form.setFields([
            { name: 'email', errors: [t('errors.emailTaken')] },
          ]);
        }
      } catch {
        /* Mạng hỏng thì im lặng bỏ qua: đây chỉ là kiểm tra sớm cho tiện,
           lúc bấm nút đăng ký server vẫn kiểm tra lại lần nữa. */
        if (seq === checkSeq.current) setEmailCheck({ state: 'idle' });
      }
    },
    [form, t],
  );

  return (
    <AuthLayout
      title={t('auth.createAccount')}
      subtitle={t('auth.registerSubtitle')}
      footer={
        <>
          {t('auth.hasAccount')}{' '}
          <Link to="/login" className="font-bold text-accent hover:underline">
            {t('auth.login')}
          </Link>
        </>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        size="large"
        /* Kiểm tra ngay khi rời ô, rồi mới theo từng phím gõ — người dùng
           thấy lỗi sớm nhưng không bị mắng ngay từ ký tự đầu tiên. */
        validateTrigger={['onBlur', 'onChange']}
        initialValues={{ acceptTerms: false }}
      >
        <Form.Item
          label={t('auth.fullName')}
          name="fullName"
          rules={[
            { required: true, message: t('auth.validation.nameRequired') },
            { min: 2, message: t('auth.validation.nameShort') },
          ]}
        >
          <Input placeholder={t('auth.namePlaceholder')} autoComplete="name" />
        </Form.Item>

        <Form.Item
          label={t('auth.email')}
          name="email"
          rules={[
            { required: true, message: t('auth.validation.emailRequired') },
            { type: 'email', message: t('auth.validation.emailFormat') },
          ]}
          validateStatus={emailCheck.state === 'checking' ? 'validating' : undefined}
          hasFeedback={emailCheck.state === 'checking'}
          extra={
            emailCheck.state === 'available' ? (
              <span className="inline-flex items-center gap-1 text-accent">
                <span
                  aria-hidden="true"
                  className="material-symbols-outlined text-[15px]"
                >
                  check_circle
                </span>
                {t('auth.emailAvailable')}
              </span>
            ) : emailCheck.state === 'checking' ? (
              t('auth.emailChecking')
            ) : undefined
          }
        >
          <Input
            placeholder="ban@example.com"
            autoComplete="email"
            inputMode="email"
            onBlur={handleEmailBlur}
            onChange={() => setEmailCheck({ state: 'idle' })}
          />
        </Form.Item>

        <Form.Item
          label={t('auth.password')}
          name="password"
          rules={[
            { required: true, message: t('auth.validation.passwordRequired') },
            { min: 6, message: t('auth.validation.passwordMin') },
          ]}
          extra={t('auth.passwordHint')}
        >
          <Input.Password
            placeholder="••••••••"
            autoComplete="new-password"
            onChange={(event) => setPassword(event.target.value)}
          />
        </Form.Item>

        <PasswordStrength password={password} className="-mt-3 mb-5" />

        <Form.Item
          label={t('auth.confirmPassword')}
          name="confirmPassword"
          dependencies={['password']}
          rules={[
            { required: true, message: t('auth.validation.confirmRequired') },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('password') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(
                  new Error(t('auth.validation.confirmMismatch')),
                );
              },
            }),
          ]}
        >
          <Input.Password placeholder="••••••••" autoComplete="new-password" />
        </Form.Item>

        <Form.Item
          name="acceptTerms"
          valuePropName="checked"
          rules={[
            {
              validator: (_, value: boolean) =>
                value
                  ? Promise.resolve()
                  : Promise.reject(new Error(t('auth.validation.termsRequired'))),
            },
          ]}
        >
          <Checkbox>
            {/* Tên hai văn bản để chữ thường chứ không phải link: dự án
                chưa có trang Điều khoản và Chính sách, mà link dẫn tới
                trang 404 còn tệ hơn là không có link. Khi hai trang đó
                ra đời thì bọc lại bằng <Link>. */}
            <span className="text-[13.5px] text-ink">
              {t('auth.termsPrefix')}{' '}
              <strong className="font-bold text-accent">{t('auth.termsLink')}</strong>{' '}
              {t('auth.and')}{' '}
              <strong className="font-bold text-accent">{t('auth.privacyLink')}</strong>
            </span>
          </Checkbox>
        </Form.Item>

        <Button type="submit" size="lg" loading={loading} block>
          {t('auth.createAccount')}
        </Button>
      </Form>
    </AuthLayout>
  );
};

export default RegisterPage;
