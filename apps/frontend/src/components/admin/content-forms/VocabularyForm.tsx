import React from 'react';
import { Form, Input, Button } from 'antd';
import { useTranslation } from 'react-i18next';

export const VocabularyForm: React.FC = () => {
  const { t } = useTranslation();

  return (
    <>
      <div className="mt-4 grid grid-cols-2 gap-4">
        <Form.Item name="topicNameVi" label={t('admin.topicNameVi', 'Tên chủ đề (VI)')} rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item name="topicNameEn" label={t('admin.topicNameEn', 'Tên chủ đề (EN)')} rules={[{ required: true }]}>
          <Input />
        </Form.Item>
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
                <Form.Item {...restField} name={[name, 'word']} label="Từ vựng" rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Form.Item {...restField} name={[name, 'meaning']} label="Nghĩa" rules={[{ required: true }]}>
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
