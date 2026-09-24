import React from 'react';
import { Form, Input, Button } from 'antd';
import { useTranslation } from 'react-i18next';
import { TopicField } from './TopicField';
import { ImageUrlField } from './ImageUrlField';

export const VocabularyForm: React.FC = () => {
  const { t } = useTranslation();

  return (
    <>
      <div className="mt-4">
        <TopicField />
        <ImageUrlField name="imageUrl" withAuthor label={t('contentForm.coverImageUrl')} />
      </div>
      <Form.Item name="prompt" label={t('contentForm.vocabDesc')}>
        <Input.TextArea rows={2} />
      </Form.Item>
      <Form.List name="items">
        {(fields, { add, remove }) => (
          <div className="mt-2 space-y-4">
            <h4 className="mb-2 text-[15px] font-semibold text-ink">{t('contentForm.vocabList')}</h4>
            {fields.map(({ key, name, ...restField }) => (
              <div key={key} className="relative rounded border border-hairline bg-surface p-4 pr-10 shadow-sm">
                <Form.Item {...restField} name={[name, 'id']} hidden>
                  <Input />
                </Form.Item>
                <div className="grid grid-cols-2 gap-4">
                  <Form.Item {...restField} name={[name, 'word']} label={t('contentForm.word')} rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                  <Form.Item {...restField} name={[name, 'phonetic']} label={t('contentForm.phonetic')}>
                    <Input />
                  </Form.Item>
                </div>
                <Form.Item {...restField} name={[name, 'meaningVi']} label={t('contentForm.meaning')} rules={[{ required: true }]}>
                  <Input />
                </Form.Item>
                <Form.Item {...restField} name={[name, 'example']} label={t('contentForm.example')}>
                  <Input />
                </Form.Item>
                <ImageUrlField name={[name, 'imageUrl']} watchPath={['items', name, 'imageUrl']} label={t('contentForm.wordImageUrl')} compact />
                <Button type="text" danger onClick={() => remove(name)} className="absolute right-2 top-2" icon={<span className="material-symbols-outlined text-[18px]">delete</span>} />
              </div>
            ))}
            <Button type="dashed" onClick={() => add()} block icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>} className="border-action text-action hover:border-action-hover hover:text-action-hover">
              {t('contentForm.addWord')}
            </Button>
          </div>
        )}
      </Form.List>
    </>
  );
};
