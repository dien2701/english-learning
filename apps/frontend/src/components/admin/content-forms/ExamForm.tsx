import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';

export const ExamForm: React.FC = () => {
  return (
    <div className="mt-4">
      <Form.Item name="prompt" label="Mô tả / Giới thiệu bài kiểm tra">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="timeLimitMinutes" label="Giới hạn thời gian (phút, 0 là không giới hạn)" initialValue={0}>
        <InputNumber min={0} className="w-full" />
      </Form.Item>

      <SharedQuestionList name="items" label="Danh sách câu hỏi kiểm tra" withSkill />
    </div>
  );
};
