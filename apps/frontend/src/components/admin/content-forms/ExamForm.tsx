import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { useTranslation } from 'react-i18next';
import { SharedQuestionList } from './SharedQuestionList';

export const ExamForm: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="mt-4">
      <Form.Item name="prompt" label={t('contentForm.examDesc')}>
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="timeLimitMinutes" label={t('contentForm.timeLimit')} initialValue={0}>
        <InputNumber min={0} className="w-full" />
      </Form.Item>

      <SharedQuestionList name="items" label={t('contentForm.examQuestions')} withSkill />
    </div>
  );
};
