import React, { useState } from 'react';
import { App, Checkbox, Form, Input } from 'antd';
import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import AuthLayout from '../../components/auth/AuthLayout';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../contexts/AuthContext';
import { useApiError } from '../../hooks/useApiError';

interface LoginForm {
  email: string;
  password: string;
  remember?: boolean;
}

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm<LoginForm>();
  const { message } = App.useApp();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { describe, applyTo } = useApiError();

  const onFinish = async (values: LoginForm) => {
    setLoading(true);
    try {
      const user = await login({
        email: values.email,
        password: values.password,
      });

      message.success(t('auth.loginSuccess'));

      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? (user.role === 'ADMIN' ? '/admin' : '/dashboard'), {
        replace: true,
      });
    } catch (error) {
      applyTo(form, error);
      // Thông báo cố ý không tiết lộ email có tồn tại trong hệ thống hay không.
      message.error(describe(error, 'auth.loginFailed'));
    } finally {
      setLoading(false);
    }
  };

  /** Điền nhanh tài khoản mẫu — chỉ có ý nghĩa khi chạy với dữ liệu giả lập. */
  const fillDemo = (email: string) => {
    form.setFieldsValue({ email, password: '123456' });
  };

  return (
    <AuthLayout
      title={t('auth.login')}
      subtitle={t('auth.loginSubtitle')}
      footer={
        <>
          {t('auth.noAccount')}{' '}
          <Link to="/register" className="font-bold text-accent hover:underline">
            {t('auth.register')}
          </Link>
        </>
      }
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        requiredMark={false}
        initialValues={{ remember: true }}
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
          <Input placeholder="ban@example.com" autoComplete="email" inputMode="email" />
        </Form.Item>

        <Form.Item
          label={t('auth.password')}
          name="password"
          rules={[
            { required: true, message: t('auth.validation.passwordRequired') },
          ]}
        >
          <Input.Password placeholder="••••••••" autoComplete="current-password" />
        </Form.Item>

        <div className="mb-5 flex items-center justify-between gap-3">
          <Form.Item name="remember" valuePropName="checked" noStyle>
            <Checkbox>{t('auth.remember')}</Checkbox>
          </Form.Item>

          <Link
            to="/forgot-password"
            className="text-[13.5px] font-bold text-accent hover:underline"
          >
            {t('auth.forgot')}
          </Link>
        </div>

        <Button type="submit" size="lg" loading={loading} block>
          {t('auth.login')}
        </Button>
      </Form>

      <div className="mt-6 rounded-md bg-surface-muted p-3">
        <p className="text-caption text-ink-muted">{t('auth.demoHint')}</p>
        <div className="mt-2 flex flex-wrap gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => fillDemo('hocvien@enlearning.vn')}
          >
            {t('auth.demoStudent')}
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => fillDemo('admin@enlearning.vn')}
          >
            {t('auth.demoAdmin')}
          </Button>
        </div>
      </div>
    </AuthLayout>
  );
};

export default LoginPage;
