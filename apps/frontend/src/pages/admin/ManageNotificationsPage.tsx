import React, { useState } from 'react';
import { App, Form, Input, Modal, Select } from 'antd';
import { useTranslation } from 'react-i18next';

import { Button } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { Chip } from '../../components/ui/Chip';
import { EmptyBlock, ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { useFormat } from '../../hooks/useFormat';
import { useLabels } from '../../hooks/useLabels';
import { adminService } from '../../services/adminService';
import { AUDIENCE_KEYS, type AudienceKey } from '../../types/admin';

interface NotificationForm {
  title: string;
  content: string;
  audience: AudienceKey;
}

const ManageNotificationsPage: React.FC = () => {
  const { message, modal } = App.useApp();
  const { t } = useTranslation();
  const { audience: audienceLabel } = useLabels();
  const { relativeTime } = useFormat();
  const { describe, applyTo } = useApiError();
  const [form] = Form.useForm<NotificationForm>();

  const [isOpen, setIsOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const { data, isLoading, error, reload } = useApi(
    () => adminService.listNotifications(),
    [],
  );

  const save = async (values: NotificationForm, send: boolean) => {
    setIsSaving(true);
    try {
      await adminService.createNotification({ ...values, send });
      message.success(
        send ? t('admin.notificationSent') : t('admin.draftSaved'),
      );
      setIsOpen(false);
      form.resetFields();
      reload();
    } catch (saveError) {
      applyTo(form, saveError);
      message.error(describe(saveError, 'admin.notificationError'));
    } finally {
      setIsSaving(false);
    }
  };

  const sendDraft = (id: string, title: string, audience: AudienceKey) => {
    modal.confirm({
      title: t('admin.sendTitle'),
      content: t('admin.sendBody', {
        title,
        audience: audienceLabel(audience).toLowerCase(),
      }),
      okText: t('admin.send'),
      cancelText: t('common.cancel'),
      onOk: async () => {
        try {
          await adminService.sendNotification(id);
          message.success(t('admin.notificationSent'));
          reload();
        } catch (sendError) {
          message.error(describe(sendError, 'admin.sendError'));
        }
      },
    });
  };

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('admin.notificationsTitle')}
        description={t('admin.notificationsSubtitle')}
        action={
          <Button
            icon="add"
            onClick={() => {
              form.resetFields();
              setIsOpen(true);
            }}
          >
            {t('admin.composeNotification')}
          </Button>
        }
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <Skeleton className="h-[360px] w-full" />
      ) : data.items.length === 0 ? (
        <Card flush>
          <EmptyBlock
            icon="campaign"
            title={t('admin.notificationsEmptyTitle')}
            message={t('admin.notificationsEmptyHint')}
          />
        </Card>
      ) : (
        <Card>
          <ul className="divide-y divide-hairline">
            {data.items.map((notification) => (
              <li key={notification.id} className="py-4 first:pt-0 last:pb-0">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="text-[15px] font-extrabold text-ink">
                        {notification.title}
                      </h2>
                      <Chip
                        tone={notification.status === 'SENT' ? 'success' : 'neutral'}
                      >
                        {notification.status === 'SENT'
                          ? t('admin.sent')
                          : t('admin.draft')}
                      </Chip>
                    </div>

                    <p className="mt-1.5 text-[13.5px] text-ink-muted">
                      {notification.content}
                    </p>

                    <p className="mt-2 text-[11.5px] text-ink-subtle">
                      {audienceLabel(notification.audience)}
                      {notification.recipientCount !== undefined &&
                        ` · ${t('admin.recipients', {
                          count: notification.recipientCount,
                        })}`}
                      {notification.sentAt
                        ? ` · ${t('admin.sentAt', {
                            time: relativeTime(notification.sentAt),
                          })}`
                        : ` · ${t('admin.createdAtTime', {
                            time: relativeTime(notification.createdAt),
                          })}`}
                    </p>
                  </div>

                  {notification.status === 'DRAFT' && (
                    <Button
                      size="sm"
                      icon="send"
                      onClick={() =>
                        sendDraft(
                          notification.id,
                          notification.title,
                          notification.audience,
                        )
                      }
                      className="shrink-0"
                    >
                      {t('admin.send')}
                    </Button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}

      <Modal
        open={isOpen}
        title={t('admin.composeNotification')}
        onCancel={() => setIsOpen(false)}
        footer={null}
        width={560}
        destroyOnHidden
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark={false}
          initialValues={{ audience: 'ALL' }}
        >
          <Form.Item
            label={t('admin.notificationTitle')}
            name="title"
            rules={[{ required: true, message: t('admin.titleRequired') }]}
          >
            <Input size="large" placeholder={t('admin.notificationTitlePlaceholder')} />
          </Form.Item>

          <Form.Item
            label={t('admin.notificationContent')}
            name="content"
            rules={[{ required: true, message: t('admin.contentRequired') }]}
          >
            <Input.TextArea
              rows={4}
              placeholder={t('admin.notificationContentPlaceholder')}
            />
          </Form.Item>

          <Form.Item
            label={t('admin.audience')}
            name="audience"
            rules={[{ required: true, message: t('admin.audienceRequired') }]}
          >
            <Select
              size="large"
              options={AUDIENCE_KEYS.map((key) => ({
                value: key,
                label: audienceLabel(key),
              }))}
            />
          </Form.Item>

          <div className="flex flex-wrap justify-end gap-2">
            <Button variant="subtle" size="lg" onClick={() => setIsOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button
              variant="secondary"
              size="lg"
              loading={isSaving}
              onClick={() => form.validateFields().then((v) => save(v, false))}
            >
              {t('admin.saveDraft')}
            </Button>
            <Button
              size="lg"
              loading={isSaving}
              onClick={() => form.validateFields().then((v) => save(v, true))}
            >
              {t('admin.sendNow')}
            </Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
};

export default ManageNotificationsPage;
