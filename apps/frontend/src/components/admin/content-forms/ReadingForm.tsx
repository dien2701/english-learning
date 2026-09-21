import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { useTranslation } from 'react-i18next';
import { SharedQuestionList } from './SharedQuestionList';
import { TopicField } from './TopicField';

export const ReadingForm: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label={t('contentForm.descOptional')}>
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="timeLimitMinutes" label={t('contentForm.timeLimit')} initialValue={0}>
        <InputNumber min={0} className="w-full" />
      </Form.Item>
      <Form.Item
        name="contentBody"
        label={t('contentForm.passage')}
        rules={[{ required: true }]}
      >
        <Input.TextArea rows={8} />
      </Form.Item>

      <SharedQuestionList name="items" label={t('contentForm.readingQuestions')} />
    </div>
  );
};
