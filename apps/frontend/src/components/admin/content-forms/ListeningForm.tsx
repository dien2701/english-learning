import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { useTranslation } from 'react-i18next';
import { SharedQuestionList } from './SharedQuestionList';
import { TopicField } from './TopicField';

export const ListeningForm: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label={t('contentForm.descOptional')}>
        <Input.TextArea rows={2} />
      </Form.Item>
      <div className="grid grid-cols-2 gap-4">
        <Form.Item name="mediaUrl" label={t('contentForm.audioUrl')}>
          <Input placeholder="https://..." />
        </Form.Item>
        <Form.Item name="durationSeconds" label={t('contentForm.durationSeconds')} initialValue={0}>
          <InputNumber min={0} className="w-full" />
        </Form.Item>
      </div>
      <Form.Item name="contentBody" label={t('contentForm.transcript')} rules={[{ required: true }]}>
        <Input.TextArea rows={4} />
      </Form.Item>

      <SharedQuestionList name="items" label={t('contentForm.listeningQuestions')} />
    </div>
  );
};
