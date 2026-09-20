import React from 'react';
import { Form, Input } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';

export const ReadingForm: React.FC = () => {

  return (
    <div className="mt-4">
      <Form.Item name="prompt" label="Đề bài chung (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="contentBody" label="Đoạn văn đọc" rules={[{ required: true }]}>
        <Input.TextArea rows={6} />
      </Form.Item>
      
      <SharedQuestionList name="items" label="Danh sách câu hỏi đọc hiểu" />
    </div>
  );
};
