import React from 'react';
import { Form, Input, Button } from 'antd';
import { TopicField } from './TopicField';

export const SpeakingForm: React.FC = () => {
  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label="Mô tả bài nói (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.List name="items">
        {(fields, { add, remove }) => (
          <div className="space-y-4">
            <h4 className="mb-2 text-[15px] font-semibold text-ink">Danh sách câu cần đọc</h4>
            {fields.map(({ key, name, ...restField }) => (
              <div key={key} className="relative rounded border border-hairline bg-surface p-4 pr-10 shadow-sm">
                <Form.Item {...restField} name={[name, 'id']} hidden>
                  <Input />
                </Form.Item>
                <Form.Item {...restField} name={[name, 'text']} label="Câu tiếng Anh" rules={[{ required: true }]}>
                  <Input.TextArea rows={2} />
                </Form.Item>
                <Form.Item {...restField} name={[name, 'meaningVi']} label="Nghĩa tiếng Việt (tuỳ chọn)">
                  <Input />
                </Form.Item>
                <Button type="text" danger onClick={() => remove(name)} className="absolute right-2 top-2" icon={<span className="material-symbols-outlined text-[18px]">delete</span>} />
              </div>
            ))}
            <Button type="dashed" onClick={() => add()} block icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>} className="border-action text-action hover:border-action-hover hover:text-action-hover">
              Thêm câu
            </Button>
          </div>
        )}
      </Form.List>
    </div>
  );
};
