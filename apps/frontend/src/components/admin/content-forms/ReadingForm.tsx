import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import { SharedQuestionList } from './SharedQuestionList';
import { TopicField } from './TopicField';

export const ReadingForm: React.FC = () => {
  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label="Mô tả (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.Item name="timeLimitMinutes" label="Giới hạn thời gian (phút, 0 là không giới hạn)" initialValue={0}>
        <InputNumber min={0} className="w-full" />
      </Form.Item>
      <Form.Item
        name="contentBody"
        label="Đoạn văn đọc (cách đoạn bằng một dòng trống)"
        rules={[{ required: true }]}
      >
        <Input.TextArea rows={8} />
      </Form.Item>

      <SharedQuestionList name="items" label="Danh sách câu hỏi đọc hiểu" />
    </div>
  );
};
