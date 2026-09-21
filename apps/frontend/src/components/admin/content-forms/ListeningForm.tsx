import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';
import { TopicField } from './TopicField';

export const ListeningForm: React.FC = () => {
  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label="Mô tả (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <div className="grid grid-cols-2 gap-4">
        <Form.Item name="mediaUrl" label="Đường dẫn Audio (URL, trống thì đọc transcript bằng giọng máy)">
          <Input placeholder="https://..." />
        </Form.Item>
        <Form.Item name="durationSeconds" label="Thời lượng (giây)" initialValue={0}>
          <InputNumber min={0} className="w-full" />
        </Form.Item>
      </div>
      <Form.Item name="contentBody" label="Transcript" rules={[{ required: true }]}>
        <Input.TextArea rows={4} />
      </Form.Item>

      <SharedQuestionList name="items" label="Danh sách câu hỏi nghe" />
    </div>
  );
};
