import React from 'react';
import { Form, Input } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';

export const ExamForm: React.FC = () => {
  return (
    <div className="mt-4">
      <Form.Item name="prompt" label="Mô tả / Giới thiệu bài kiểm tra">
        <Input.TextArea rows={2} />
      </Form.Item>
      
      <SharedQuestionList name="items" label="Danh sách câu hỏi kiểm tra" />
    </div>
  );
};
