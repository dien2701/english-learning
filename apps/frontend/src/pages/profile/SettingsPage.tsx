import React, { useState } from 'react';
import { App, InputNumber, Select, Switch, TimePicker } from 'antd';
import dayjs from 'dayjs';
import { useTranslation } from 'react-i18next';

import PageHeader from '../../components/ui/PageHeader';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useThemeMode } from '../../contexts/ThemeContext';
import { profileService, type UserSettings } from '../../services/userService';

/** Một dòng cài đặt: nhãn bên trái, điều khiển bên phải. */
const SettingRow: React.FC<{
  label: string;
  description?: string;
  control: React.ReactNode;
  htmlFor?: string;
}> = ({ label, description, control, htmlFor }) => (
  <div className="flex flex-wrap items-center justify-between gap-3 py-4">
    <div className="min-w-0 flex-1">
      <label htmlFor={htmlFor} className="text-[14px] font-semibold text-ink">
        {label}
      </label>
      {description && (
        <p className="mt-0.5 text-[12.5px] text-ink-muted">{description}</p>
      )}
    </div>
    <div className="shrink-0">{control}</div>
  </div>
);

const SettingsPage: React.FC = () => {
  const { message } = App.useApp();
  const { t, i18n } = useTranslation();
  const { describe } = useApiError();
  const { mode, setMode } = useThemeMode();

  const { data, isLoading, error, reload } = useApi(
    () => profileService.getSettings(),
    [],
  );

  /* Giữ riêng phần người dùng vừa chỉnh rồi phủ lên dữ liệu từ server,
     nhờ vậy không phải sao chép cả object vào state. */
  const [pending, setPending] = useState<Partial<UserSettings>>({});

  const settings: UserSettings | null = data ? { ...data, ...pending } : null;

  /** Lưu ngay khi đổi — cài đặt nhỏ thì không cần nút lưu riêng. */
  const patch = async (changes: Partial<UserSettings>) => {
    if (!settings) return;

    const previous = pending;
    setPending((prev) => ({ ...prev, ...changes }));

    try {
      await profileService.updateSettings(changes);

      // Đổi ngôn ngữ thì áp dụng luôn cho giao diện.
      if (changes.language) void i18n.changeLanguage(changes.language);

      // ThemeProvider lo phần còn lại: class .dark cho Tailwind và
      // dựng lại theme của Ant Design.
      if (changes.theme) setMode(changes.theme);

      message.success(t('settings.saved'));
    } catch (saveError) {
      setPending(previous); // Trả lại giá trị cũ khi lưu hỏng.
      message.error(describe(saveError, 'settings.saveError'));
    }
  };

  if (error) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6 lg:px-8">
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      </div>
    );
  }

  if (isLoading || !settings) {
    return (
      <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6 lg:px-8">
        <Skeleton className="h-[420px] w-full" />
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-3xl px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('settings.title')}
        description={t('settings.subtitle')}
      />

      <div className="flex flex-col gap-5">
        <Card>
          <CardHeader title={t('settings.appearance')} />

          <div className="divide-y divide-hairline">
            <SettingRow
              htmlFor="setting-language"
              label={t('settings.language')}
              description={t('settings.languageHint')}
              control={
                <Select
                  id="setting-language"
                  size="large"
                  /* Lấy theo ngôn ngữ đang hiển thị, không theo giá trị đã
                     lưu: người dùng còn đổi được bằng nút VI/EN trên header,
                     nếu đọc giá trị lưu thì ô này sẽ lệch với giao diện. */
                  value={i18n.language === 'en' ? ('en' as const) : ('vi' as const)}
                  onChange={(value) => patch({ language: value })}
                  className="w-36"
                  options={[
                    { value: 'vi', label: t('settings.vietnamese') },
                    { value: 'en', label: t('settings.english') },
                  ]}
                />
              }
            />

            <SettingRow
              htmlFor="setting-theme"
              label={t('settings.displayMode')}
              description={t('settings.displayModeHint')}
              control={
                <Select
                  id="setting-theme"
                  size="large"
                  value={mode}
                  onChange={(value) => patch({ theme: value })}
                  className="w-36"
                  options={[
                    { value: 'light', label: t('header.themeLight') },
                    { value: 'dark', label: t('header.themeDark') },
                  ]}
                />
              }
            />
          </div>
        </Card>

        <Card>
          <CardHeader title={t('settings.learning')} />

          <div className="divide-y divide-hairline">
            <SettingRow
              htmlFor="setting-goal"
              label={t('settings.dailyGoal')}
              description={t('settings.dailyGoalHint')}
              control={
                <InputNumber
                  id="setting-goal"
                  size="large"
                  min={5}
                  max={240}
                  step={5}
                  value={settings.dailyGoalMinutes}
                  onChange={(value) =>
                    value !== null && patch({ dailyGoalMinutes: value })
                  }
                  addonAfter={t('settings.minutesUnit')}
                  className="w-40"
                />
              }
            />
          </div>
        </Card>

        <Card>
          <CardHeader title={t('settings.reminders')} />

          <div className="divide-y divide-hairline">
            <SettingRow
              label={t('settings.emailReminder')}
              description={t('settings.emailReminderHint')}
              control={
                <Switch
                  checked={settings.emailReminders}
                  onChange={(checked) => patch({ emailReminders: checked })}
                  aria-label={t('settings.emailReminderAria')}
                />
              }
            />

            <SettingRow
              label={t('settings.reminderTime')}
              description={t('settings.reminderTimeHint')}
              control={
                <TimePicker
                  size="large"
                  format="HH:mm"
                  allowClear={false}
                  disabled={!settings.emailReminders}
                  value={dayjs(settings.reminderTime, 'HH:mm')}
                  onChange={(value) =>
                    value && patch({ reminderTime: value.format('HH:mm') })
                  }
                  aria-label={t('settings.reminderTimeAria')}
                  className="w-32"
                />
              }
            />
          </div>
        </Card>
      </div>
    </div>
  );
};

export default SettingsPage;
