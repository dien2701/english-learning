import React from 'react';
import { Form, Input, Select, Button, Card, Radio } from 'antd';
import type { FormListFieldData } from 'antd';
import { useTranslation } from 'react-i18next';

interface SharedQuestionListProps {
  name: string | (string | number)[];
  label?: string;
  /** Đề kiểm tra: mỗi câu chọn kỹ năng. */
  withSkill?: boolean;
}

const QuestionItem: React.FC<{ fieldName: number; restField: Omit<FormListFieldData, 'key' | 'name'>; remove: (name: number) => void; index: number; listName: string | (string | number)[]; withSkill?: boolean }> = ({ fieldName, restField, remove, index, listName, withSkill }) => {
  const { t } = useTranslation();
  const typePath = Array.isArray(listName) ? [...listName, fieldName, 'type'] : [listName, fieldName, 'type'];
  const type = Form.useWatch(typePath) || 'MULTIPLE_CHOICE';

  const optionsPath = Array.isArray(listName) ? [...listName, fieldName, 'options'] : [listName, fieldName, 'options'];
  const currentOptions = Form.useWatch(optionsPath) || [];
  
  // Lọc các option hợp lệ (là chuỗi và không rỗng) để đưa vào dropdown chọn đáp án
  const validOptions = (Array.isArray(currentOptions) ? currentOptions : [])
    .filter((opt: unknown): opt is string => typeof opt === 'string' && opt.trim() !== '')
    .map((opt: string) => ({ value: opt, label: opt }));

  return (
    <Card
      size="small"
      className="relative mb-4 overflow-visible border-hairline shadow-sm"
      title={<span className="text-[14px] font-medium">{t('contentForm.questionN', { n: index + 1 })}</span>}
      extra={
        <Button
          type="text"
          danger
          onClick={() => remove(fieldName)}
          icon={<span className="material-symbols-outlined text-[18px]">delete</span>}
          title={t('contentForm.deleteQuestion')}
        />
      }
    >
      <Form.Item {...restField} name={[fieldName, 'id']} hidden>
        <Input />
      </Form.Item>

      {withSkill && (
        <Form.Item
          {...restField}
          name={[fieldName, 'skill']}
          label={t('contentForm.skill')}
          rules={[{ required: true, message: t('contentForm.skillRequired') }]}
        >
          <Select
            options={[
              { value: 'LISTENING', label: t('skill.LISTENING') },
              { value: 'READING', label: t('skill.READING') },
              { value: 'VOCABULARY', label: t('skill.VOCABULARY') },
              { value: 'WRITING', label: t('skill.WRITING') },
            ]}
          />
        </Form.Item>
      )}

      <Form.Item
        {...restField}
        name={[fieldName, 'type']}
        label={t('contentForm.questionType')}
        initialValue="MULTIPLE_CHOICE"
      >
        <Radio.Group buttonStyle="solid">
          <Radio.Button value="MULTIPLE_CHOICE">{t('contentForm.multipleChoice')}</Radio.Button>
          <Radio.Button value="FILL_BLANK">{t('contentForm.fillBlank')}</Radio.Button>
        </Radio.Group>
      </Form.Item>

      <Form.Item
        {...restField}
        name={[fieldName, 'content']}
        label={t('contentForm.questionContent')}
        rules={[{ required: true, message: t('contentForm.contentRequired') }]}
      >
        <Input.TextArea rows={2} placeholder={t('contentForm.questionPlaceholder')} />
      </Form.Item>

      {type === 'MULTIPLE_CHOICE' && (
        <div className="mb-4">
          <div className="mb-2 text-[14px] text-ink">{t('contentForm.options')}</div>
          <Form.List name={[fieldName, 'options']} initialValue={['', '']}>
            {(fields, { add, remove }) => (
              <div className="flex flex-col gap-2">
                {fields.map((field, idx) => (
                  <div key={field.key} className="flex items-center gap-2">
                    <Form.Item
                      {...field}
                      noStyle
                      rules={[{ required: true, message: t('contentForm.optionRequired') }]}
                    >
                      <Input placeholder={t('contentForm.optionN', { n: idx + 1 })} />
                    </Form.Item>
                    <Button
                      type="text"
                      danger
                      onClick={() => remove(field.name)}
                      icon={<span className="material-symbols-outlined text-[18px]">remove</span>}
                      className={fields.length <= 2 ? 'invisible' : ''}
                    />
                  </div>
                ))}
                <Button
                  type="dashed"
                  onClick={() => add('')}
                  block
                  icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>}
                >
                  {t('contentForm.addOption')}
                </Button>
              </div>
            )}
          </Form.List>
        </div>
      )}

      {type === 'MULTIPLE_CHOICE' ? (
        <Form.Item
          {...restField}
          name={[fieldName, 'correctAnswers']}
          label={t('contentForm.correctAnswer')}
          rules={[{ required: true, message: t('contentForm.correctRequired'), type: 'array', min: 1 }]}
          getValueProps={(value: string[] | undefined) => ({ value: value?.[0] })}
          getValueFromEvent={(value: string | undefined) => (value === undefined ? [] : [value])}
        >
          <Select
            placeholder={t('contentForm.correctPlaceholder')}
            style={{ width: '100%' }}
            options={validOptions}
            notFoundContent={t('contentForm.noOptionsYet')}
          />
        </Form.Item>
      ) : (
        <Form.Item
          {...restField}
          name={[fieldName, 'correctAnswers']}
          label={t('contentForm.correctAnswerTags')}
          rules={[{ required: true, message: t('contentForm.correctRequired'), type: 'array', min: 1 }]}
        >
          <Select mode="tags" placeholder={t('contentForm.correctTagsPlaceholder')} style={{ width: '100%' }} />
        </Form.Item>
      )}
      <Form.Item {...restField} name={[fieldName, 'explanation']} label={t('contentForm.explanation')}>
        <Input.TextArea rows={2} />
      </Form.Item>
    </Card>
  );
};

export const SharedQuestionList: React.FC<SharedQuestionListProps> = ({ name, label, withSkill }) => {
  const { t } = useTranslation();

  return (
    <div className="mt-4">
      {label && <h4 className="mb-3 text-[15px] font-semibold text-ink">{label}</h4>}
      <Form.List name={name}>
        {(fields, { add, remove }) => (
          <div>
            {fields.map(({ key, name: fieldName, ...restField }, index) => (
              <QuestionItem 
                key={key} 
                fieldName={fieldName} 
                restField={restField} 
                remove={remove} 
                index={index}
                listName={name}
                withSkill={withSkill}
              />
            ))}
            <Button
              type="dashed"
              onClick={() => add({ type: 'MULTIPLE_CHOICE', content: '', options: ['', ''], correctAnswers: [] })}
              block
              icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>}
              className="mt-2 border-action text-action hover:border-action-hover hover:text-action-hover"
            >
              {t('contentForm.addQuestion')}
            </Button>
          </div>
        )}
      </Form.List>
    </div>
  );
};
