import React, { useEffect, useState } from 'react';
import { App, Form, Input } from 'antd';
import { useTranslation } from 'react-i18next';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { profileService } from '../../services/userService';

interface ProfileForm {
  fullName: string;
  email: string;
}

interface PasswordForm {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const ProfilePage: React.FC = () => {
  const { message } = App.useApp();
  const { t } = useTranslation();
  const { date, duration } = useFormat();
  const { describe, applyTo } = useApiError();
  const [profileForm] = Form.useForm<ProfileForm>();
  const [passwordForm] = Form.useForm<PasswordForm>();

  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isSavingPassword, setIsSavingPassword] = useState(false);

  const { data, isLoading, error, reload } = useApi(() => profileService.get(), []);

  // Đổ dữ liệu vào form sau khi tải xong.
  useEffect(() => {
    if (data) {
      profileForm.setFieldsValue({ fullName: data.fullName, email: data.email });
    }
  }, [data, profileForm]);

  const saveProfile = async (values: ProfileForm) => {
    setIsSavingProfile(true);
    try {
      await profileService.update(values);
      message.success(t('profile.updated'));
      reload();
    } catch (saveError) {
      applyTo(profileForm, saveError);
      message.error(describe(saveError, 'profile.updateError'));
    } finally {
      setIsSavingProfile(false);
    }
  };

  const savePassword = async (values: PasswordForm) => {
    setIsSavingPassword(true);
    try {
      await profileService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      message.success(t('profile.passwordChanged'));
      passwordForm.resetFields();
    } catch (saveError) {
      applyTo(passwordForm, saveError);
      message.error(describe(saveError, 'profile.passwordError'));
    } finally {
      setIsSavingPassword(false);
    }
  };

  if (error) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (isLoading || !data) {
    return (
      <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[420px] w-full" />
      </div>
    );
  }

  const initials = data.fullName
    .trim()
    .split(/\s+/)
    .map((part) => part[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader title={t('profile.title')} />

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-12">
        <div className="lg:col-span-4">
          <Card className="text-center">
            <span className="mx-auto grid h-20 w-20 place-items-center rounded-pill bg-action text-[26px] font-extrabold text-white">
              {initials}
            </span>

            <h2 className="mt-4 text-[18px] font-extrabold text-ink">
              {data.fullName}
            </h2>
            <p className="mt-0.5 text-[13px] text-ink-muted">{data.email}</p>
            <p className="mt-2 text-caption text-ink-subtle">
              {t('profile.joinedAt', { date: date(data.joinedAt) })}
            </p>

            <dl className="mt-6 grid grid-cols-3 gap-3 border-t border-hairline pt-5">
              {[
                {
                  label: t('profile.totalTime'),
                  value: duration(data.totalMinutes),
                },
                {
                  label: t('profile.completedLessons'),
                  value: String(data.completedLessons),
                },
                {
                  label: t('profile.masteredWords'),
                  value: String(data.masteredWords),
                },
              ].map((stat) => (
                <div key={stat.label}>
                  <dd className="text-[15px] font-extrabold text-ink">{stat.value}</dd>
                  <dt className="mt-0.5 text-[11px] text-ink-muted">{stat.label}</dt>
                </div>
              ))}
            </dl>
          </Card>
        </div>

        <div className="flex flex-col gap-5 lg:col-span-8">
          <Card>
            <CardHeader title={t('profile.personalInfo')} />

            <Form
              form={profileForm}
              layout="vertical"
              onFinish={saveProfile}
              requiredMark={false}
              className="mt-4"
            >
              <Form.Item
                label={t('profile.fullName')}
                name="fullName"
                rules={[{ required: true, message: t('auth.validation.nameRequired') }]}
              >
                <Input size="large" />
              </Form.Item>

              <Form.Item
                label="Email"
                name="email"
                rules={[
                  { required: true, message: t('auth.validation.emailRequired') },
                  { type: 'email', message: t('auth.validation.emailFormat') },
                ]}
              >
                <Input size="large" />
              </Form.Item>

              <Button type="submit" size="lg" loading={isSavingProfile}>
                {t('common.save')}
              </Button>
            </Form>
          </Card>

          <Card>
            <CardHeader title={t('profile.changePassword')} />

            <Form
              form={passwordForm}
              layout="vertical"
              onFinish={savePassword}
              requiredMark={false}
              className="mt-4"
            >
              <Form.Item
                label={t('profile.currentPassword')}
                name="currentPassword"
                rules={[
                  { required: true, message: t('profile.currentPasswordRequired') },
                ]}
              >
                <Input.Password size="large" autoComplete="current-password" />
              </Form.Item>

              <Form.Item
                label={t('profile.newPassword')}
                name="newPassword"
                rules={[
                  { required: true, message: t('auth.validation.newPasswordRequired') },
                  { min: 6, message: t('auth.validation.passwordMin') },
                ]}
              >
                <Input.Password size="large" autoComplete="new-password" />
              </Form.Item>

              <Form.Item
                label={t('profile.confirmNewPassword')}
                name="confirmPassword"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: t('profile.confirmNewRequired') },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(
                        new Error(t('auth.validation.confirmMismatch')),
                      );
                    },
                  }),
                ]}
              >
                <Input.Password size="large" autoComplete="new-password" />
              </Form.Item>

              <Button type="submit" size="lg" loading={isSavingPassword}>
                {t('profile.changePassword')}
              </Button>
            </Form>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
