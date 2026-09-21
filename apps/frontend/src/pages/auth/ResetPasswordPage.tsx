import React, { useState } from 'react';
import { App, Form, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';

import AuthLayout from '../../components/auth/AuthLayout';
import { Button } from '../../components/ui/Button';
import { useApiError } from '../../hooks/useApiError';
import { authService } from '../../services/authService';

interface ResetForm {
  email: string;
  code: string;
  password: string;
  confirmPassword: string;
}

const ResetPasswordPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [isDone, setIsDone] = useState(false);
  const [form] = Form.useForm<ResetForm>();
  const { message } = App.useApp();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { describe, applyTo } = useApiError();

  const onFinish = async (values: ResetForm) => {
    setLoading(true);
    try {
      await authService.resetPassword(values);
      setIsDone(true);
    } catch (error) {
      applyTo(form, error);
      message.error(describe(error, 'auth.resetError'));
    } finally {
      setLoading(false);
    }
  };

  if (isDone) {
    return (
      <AuthLayout title={t('auth.resetDone')} subtitle={t('auth.resetDoneHint')}>
        <div className="flex flex-col items-center gap-4 py-2">
          <span className="grid h-14 w-14 place-items-center rounded-pill bg-success-bg text-success">
            <span aria-hidden="true" className="material-symbols-outlined text-[30px]">
              check_circle
            </span>
          </span>

          <Button size="lg" block onClick={() => navigate('/login', { replace: true })}>
            {t('auth.login')}
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      title={t('auth.resetTitle')}
      subtitle={t('auth.resetSubtitle')}
      footer={
        <Link to="/login" className="font-bold text-accent hover:underline">
          {t('auth.backToLogin')}
        </Link>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        size="large"
        validateTrigger={['onBlur', 'onChange']}
        initialValues={{
          email: searchParams.get('email') ?? '',
          code: searchParams.get('code') ?? '',
        }}
      >
        <Form.Item
          label={t('auth.email')}
          name="email"
          rules={[
            { required: true, message: t('auth.validation.emailRequired') },
            { type: 'email', message: t('auth.validation.emailFormat') },
          ]}
        >
          <Input placeholder="name@example.com" autoComplete="email" inputMode="email" />
        </Form.Item>

        <Form.Item
          label={t('auth.code')}
          name="code"
          rules={[{ required: true, message: t('auth.validation.codeRequired') }]}
        >
          <Input placeholder="123456" inputMode="numeric" autoComplete="one-time-code" />
        </Form.Item>

        <Form.Item
          label={t('auth.newPassword')}
          name="password"
          rules={[
            { required: true, message: t('auth.validation.newPasswordRequired') },
            { min: 6, message: t('auth.validation.passwordMin') },
          ]}
        >
          <Input.Password placeholder="••••••••" autoComplete="new-password" />
        </Form.Item>

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
                return Promise.reject(new Error(t('auth.validation.confirmMismatch')));
              },
            }),
          ]}
        >
          <Input.Password placeholder="••••••••" autoComplete="new-password" />
        </Form.Item>

        <Button type="submit" size="lg" loading={loading} block>
          {t('auth.resetTitle')}
        </Button>
      </Form>
    </AuthLayout>
  );
};

export default ResetPasswordPage;
