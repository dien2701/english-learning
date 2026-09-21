import React from 'react';
import { Form, Select } from 'antd';
import { useTranslation } from 'react-i18next';
import { useApi } from '../../../hooks/useApi';
import { useLanguage } from '../../../hooks/useLanguage';
import { adminService } from '../../../services/adminService';

/** Ô chọn chủ đề (tên `topicId`), nạp danh sách từ `/admin/topics`. */
export const TopicField: React.FC = () => {
  const { t } = useTranslation();
  const { L } = useLanguage();
  const { data, isLoading } = useApi(() => adminService.listTopics(), []);

  return (
    <Form.Item
      name="topicId"
      label={t('admin.topics')}
      rules={[{ required: true, message: t('errors.field.topicRequired') }]}
    >
      <Select
        loading={isLoading}
        showSearch={{ optionFilterProp: 'label' }}
        options={(data ?? []).map((topic) => ({ value: topic.id, label: L(topic.name) }))}
      />
    </Form.Item>
  );
};
