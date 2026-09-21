import React from 'react';
import { Form, Input, Select, Button, Card, Radio } from 'antd';
import type { FormListFieldData } from 'antd';

interface SharedQuestionListProps {
  name: string | (string | number)[];
  label?: string;
}

const QuestionItem: React.FC<{ fieldName: number; restField: Omit<FormListFieldData, 'key' | 'name'>; remove: (name: number) => void; index: number; listName: string | (string | number)[] }> = ({ fieldName, restField, remove, index, listName }) => {
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
      title={<span className="text-[14px] font-medium">Câu hỏi {index + 1}</span>}
      extra={
        <Button
          type="text"
          danger
          onClick={() => remove(fieldName)}
          icon={<span className="material-symbols-outlined text-[18px]">delete</span>}
          title="Xóa câu hỏi"
        />
      }
    >
      <Form.Item
        {...restField}
        name={[fieldName, 'type']}
        label="Loại câu hỏi"
        initialValue="MULTIPLE_CHOICE"
      >
        <Radio.Group buttonStyle="solid">
          <Radio.Button value="MULTIPLE_CHOICE">Trắc nghiệm</Radio.Button>
          <Radio.Button value="FILL_BLANK">Điền từ</Radio.Button>
        </Radio.Group>
      </Form.Item>

      <Form.Item
        {...restField}
        name={[fieldName, 'content']}
        label="Nội dung câu hỏi"
        rules={[{ required: true, message: 'Vui lòng nhập nội dung' }]}
      >
        <Input.TextArea rows={2} placeholder="Nhập câu hỏi (Với điền từ, dùng [blank] cho ô trống)" />
      </Form.Item>

      {type === 'MULTIPLE_CHOICE' && (
        <div className="mb-4">
          <div className="mb-2 text-[14px] text-ink">Các lựa chọn</div>
          <Form.List name={[fieldName, 'options']} initialValue={['', '']}>
            {(fields, { add, remove }) => (
              <div className="flex flex-col gap-2">
                {fields.map((field, idx) => (
                  <div key={field.key} className="flex items-center gap-2">
                    <Form.Item
                      {...field}
                      noStyle
                      rules={[{ required: true, message: 'Vui lòng nhập nội dung lựa chọn' }]}
                    >
                      <Input placeholder={`Lựa chọn ${idx + 1}`} />
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
                  Thêm lựa chọn
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
          label="Đáp án đúng"
          rules={[{ required: true, message: 'Cần có đáp án đúng', type: 'array', min: 1 }]}
        >
          <Select 
            mode="multiple" 
            placeholder="Chọn đáp án đúng từ danh sách lựa chọn" 
            style={{ width: '100%' }}
            options={validOptions}
            notFoundContent="Vui lòng nhập các lựa chọn trước"
          />
        </Form.Item>
      ) : (
        <Form.Item
          {...restField}
          name={[fieldName, 'correctAnswers']}
          label="Đáp án đúng (Nhấn Enter để thêm)"
          rules={[{ required: true, message: 'Cần có đáp án đúng', type: 'array', min: 1 }]}
        >
          <Select mode="tags" placeholder="Nhập đáp án đúng và nhấn Enter" style={{ width: '100%' }} />
        </Form.Item>
      )}
    </Card>
  );
};

export const SharedQuestionList: React.FC<SharedQuestionListProps> = ({ name, label }) => {
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
              />
            ))}
            <Button
              type="dashed"
              onClick={() => add({ type: 'MULTIPLE_CHOICE', content: '', options: ['', ''], correctAnswers: [] })}
              block
              icon={<span className="material-symbols-outlined mr-1 text-[18px]">add</span>}
              className="mt-2 border-action text-action hover:border-action-hover hover:text-action-hover"
            >
              Thêm câu hỏi
            </Button>
          </div>
        )}
      </Form.List>
    </div>
  );
};
