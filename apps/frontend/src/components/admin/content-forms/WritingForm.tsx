import React from 'react';
import { Form, Input, InputNumber, Button } from 'antd';
import { TopicField } from './TopicField';

export const WritingForm: React.FC = () => {
  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="instructions" label="Đề bài (tiếng Anh)" rules={[{ required: true }]}>
        <Input.TextArea rows={4} />
      </Form.Item>
      <div className="grid grid-cols-2 gap-4">
        <Form.Item name="suggestedMinutes" label="Thời gian gợi ý (phút)" initialValue={30}>
          <InputNumber min={1} className="w-full" />
        </Form.Item>
        <Form.Item name="minWords" label="Số từ tối thiểu" initialValue={0}>
          <InputNumber min={0} className="w-full" />
        </Form.Item>
      </div>
      <Form.List name="hints">
        {(fields, { add, remove }) => (
          <div className="space-y-2">
            <h4 className="mb-2 text-[15px] font-semibold text-ink">Gợi ý triển khai</h4>
            {fields.map((field) => (
              <div key={field.key} className="flex items-center gap-2">
                <Form.Item {...field} noStyle rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Button type="text" danger onClick={() => remove(field.name)} icon={<span className="material-symbols-outlined text-[18px]">remove</span>} />
              </div>
            ))}
            <Button type="dashed" onClick={() => add('')} block icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>}>
              Thêm gợi ý
            </Button>
          </div>
        )}
      </Form.List>
    </div>
  );
};
