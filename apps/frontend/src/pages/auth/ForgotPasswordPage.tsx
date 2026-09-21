import React, { useState } from 'react';
import { App, Form, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';

import AuthLayout from '../../components/auth/AuthLayout';
import { Button } from '../../components/ui/Button';
import { useApiError } from '../../hooks/useApiError';
import { authService } from '../../services/authService';

const ForgotPasswordPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [sentTo, setSentTo] = useState<string | null>(null);
  const { message } = App.useApp();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { describe } = useApiError();

  const onFinish = async (values: { email: string }) => {
    setLoading(true);
    try {
      await authService.forgotPassword(values.email);
      setSentTo(values.email);
    } catch (error) {
      message.error(describe(error, 'auth.sendError'));
    } finally {
      setLoading(false);
    }
  };

  /* Màn hình sau khi gửi. Nội dung cố ý không xác nhận email có tồn tại
     hay không, để không lộ tài khoản nào đang có trong hệ thống. */
  if (sentTo) {
    return (
      <AuthLayout
        title={t('auth.checkInbox')}
        subtitle={t('auth.sentTo', { email: sentTo })}
      >
        <div className="rounded-md bg-surface-muted p-4">
          <p className="text-body text-ink-muted">{t('auth.codeValid')}</p>
        </div>

        <Button
          size="lg"
          block
          className="mt-5"
          onClick={() =>
            navigate(`/reset-password?email=${encodeURIComponent(sentTo)}`)
          }
        >
          {t('auth.enterCode')}
        </Button>

        <Button
          variant="subtle"
          size="lg"
          block
          className="mt-2"
          onClick={() => setSentTo(null)}
        >
          {t('auth.useAnotherEmail')}
        </Button>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout
      title={t('auth.forgotTitle')}
      subtitle={t('auth.forgotSubtitle')}
      footer={
        <>
          {t('auth.rememberedPassword')}{' '}
          <Link to="/login" className="font-bold text-accent hover:underline">
            {t('auth.login')}
          </Link>
        </>
      }
    >
      <Form
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        size="large"
        validateTrigger={['onBlur', 'onChange']}
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

        <Button type="submit" size="lg" loading={loading} block>
          {t('auth.sendCode')}
        </Button>
      </Form>
    </AuthLayout>
  );
};

export default ForgotPasswordPage;
