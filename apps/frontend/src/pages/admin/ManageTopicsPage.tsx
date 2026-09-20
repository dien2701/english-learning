import React, { useState } from 'react';
import { App, Form, Input, Modal, Popconfirm } from 'antd';
import { useTranslation } from 'react-i18next';

import { Button, IconButton } from '../../components/ui/Button';
import PageHeader from '../../components/ui/PageHeader';
import { Card } from '../../components/ui/Card';
import { ErrorState, Skeleton } from '../../components/ui/StateBlocks';
import { useApi } from '../../hooks/useApi';
import { useApiError } from '../../hooks/useApiError';
import { adminService } from '../../services/adminService';
import type { Topic } from '../../types/practice';
import { useLanguage } from '../../hooks/useLanguage';

interface TopicForm {
  name: string;
}

const ManageTopicsPage: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { describe, applyTo } = useApiError();
  const { message } = App.useApp();
  const [form] = Form.useForm<TopicForm>();

  const [editing, setEditing] = useState<Topic | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const { data, isLoading, error, reload } = useApi(
    () => adminService.listTopics(),
    [],
  );

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setIsOpen(true);
  };

  const openEdit = (topic: Topic) => {
    setEditing(topic);
    form.setFieldsValue({ name: L(topic.name) });
    setIsOpen(true);
  };

  const save = async (values: TopicForm) => {
    setIsSaving(true);
    try {
      if (editing) {
        await adminService.updateTopic(editing.id, values.name);
        message.success(t('admin.topicUpdated'));
      } else {
        await adminService.createTopic(values.name);
        message.success(t('admin.topicAdded'));
      }
      setIsOpen(false);
      reload();
    } catch (saveError) {
      applyTo(form, saveError);
      message.error(describe(saveError, 'admin.topicError'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await adminService.deleteTopic(id);
      message.success(t('admin.topicDeleted'));
      reload();
    } catch (deleteError) {
      message.error(describe(deleteError, 'admin.topicError'));
    }
  };

  return (
    <div className="mx-auto w-full max-w-content px-4 py-6 sm:px-6 lg:px-8">
      <PageHeader
        title={t('admin.topicsTitle')}
        description={t('admin.topicsSubtitle')}
        action={
          <Button icon="add" onClick={openCreate}>
            {t('admin.addTopic')}
          </Button>
        }
      />

      {error ? (
        <Card flush>
          <ErrorState message={describe(error)} onRetry={reload} />
        </Card>
      ) : isLoading || !data ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-[92px] w-full" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((topic) => (
            <div
              key={topic.id}
              className="flex items-center gap-3 rounded-lg border border-hairline bg-surface p-4 shadow-sm"
            >
              <span className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-accent-soft text-accent">
                <span aria-hidden="true" className="material-symbols-outlined text-[20px]">
                  sell
                </span>
              </span>

              <div className="min-w-0 flex-1">
                <p className="truncate text-[14.5px] font-bold text-ink">{L(topic.name)}</p>
                <p className="text-[12px] text-ink-muted">
                  {t('admin.topicItemCount', { count: topic.itemCount ?? 0 })}
                </p>
              </div>

              <div className="flex shrink-0 items-center">
                <IconButton
                  icon="edit"
                  label={t('admin.editTopicOf', { name: L(topic.name) })}
                  variant="subtle"
                  onClick={() => openEdit(topic)}
                  className="bg-transparent"
                />
                <Popconfirm
                  title={t('admin.deleteTopicTitle')}
                  description={t('admin.deleteTopicBody')}
                  onConfirm={() => handleDelete(topic.id)}
                  okText={t('common.delete')}
                  cancelText={t('common.cancel')}
                  placement="topRight"
                  okButtonProps={{ danger: true }}
                >
                  <IconButton
                    icon="delete"
                    label={t('common.delete')}
                    variant="subtle"
                    className="bg-transparent text-[var(--color-error)]"
                  />
                </Popconfirm>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={isOpen}
        title={editing ? t('admin.editTopic') : t('admin.addTopic')}
        onCancel={() => setIsOpen(false)}
        footer={null}
        destroyOnHidden
      >
        <Form form={form} layout="vertical" onFinish={save} requiredMark={false}>
          <Form.Item
            label={t('admin.topicName')}
            name="name"
            rules={[{ required: true, message: t('admin.topicRequired') }]}
          >
            <Input size="large" placeholder={t('admin.topicPlaceholder')} autoFocus />
          </Form.Item>

          <div className="flex justify-end gap-2">
            <Button variant="subtle" size="lg" onClick={() => setIsOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" size="lg" loading={isSaving}>
              {editing ? t('common.save') : t('admin.addTopic')}
            </Button>
          </div>
        </Form>
      </Modal>
    </div>
  );
};

export default ManageTopicsPage;
