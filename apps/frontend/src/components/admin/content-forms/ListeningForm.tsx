import React from 'react';
import { Form, Input } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';

export const ListeningForm: React.FC = () => {

  return (
    <div className="mt-4">
      <Form.Item name="prompt" label="Đề bài chung (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="mediaUrl" label="Đường dẫn Audio (URL)" rules={[{ required: true }]}>
        <Input placeholder="https://..." />
      </Form.Item>
      <Form.Item name="contentBody" label="Transcript (tuỳ chọn)">
        <Input.TextArea rows={4} />
      </Form.Item>
      
      <SharedQuestionList name="items" label="Danh sách câu hỏi nghe" />
    </div>
  );
};
