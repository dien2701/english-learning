import React from 'react';
import { Form, Input, Button } from 'antd';
import { TopicField } from './TopicField';

export const VocabularyForm: React.FC = () => {
  return (
    <>
      <div className="mt-4">
        <TopicField />
      </div>
      <Form.Item name="prompt" label="Mô tả bộ từ vựng (tuỳ chọn)">
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.List name="items">
        {(fields, { add, remove }) => (
          <div className="mt-2 space-y-4">
            <h4 className="mb-2 text-[15px] font-semibold text-ink">Danh sách Từ vựng</h4>
            {fields.map(({ key, name, ...restField }) => (
              <div key={key} className="relative rounded border border-hairline bg-surface p-4 pr-10 shadow-sm">
                <Form.Item {...restField} name={[name, 'id']} hidden>
                  <Input />
                </Form.Item>
                <div className="grid grid-cols-2 gap-4">
                  <Form.Item {...restField} name={[name, 'word']} label="Từ vựng" rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                  <Form.Item {...restField} name={[name, 'phonetic']} label="Phiên âm (tuỳ chọn)">
                    <Input />
                  </Form.Item>
                </div>
                <Form.Item {...restField} name={[name, 'meaningVi']} label="Nghĩa" rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Form.Item {...restField} name={[name, 'example']} label="Câu ví dụ (tuỳ chọn)">
                  <Input />
                </Form.Item>
                <Button type="text" danger onClick={() => remove(name)} className="absolute right-2 top-2" icon={<span className="material-symbols-outlined text-[18px]">delete</span>} />
              </div>
            ))}
            <Button type="dashed" onClick={() => add()} block icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>} className="border-action text-action hover:border-action-hover hover:text-action-hover">
              Thêm từ vựng
            </Button>
          </div>
        )}
      </Form.List>
    </>
  );
};
