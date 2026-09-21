import React, { useEffect, useEffectEvent, useState } from 'react';
import { Drawer, Form, Input, Select, Space, Button, App, Spin, Tabs, Upload } from 'antd';
import { useTranslation } from 'react-i18next';
import { toFormValues, toPayload, type ContentFormValues, type QuestionFormValue } from './contentMapping';
import type { Skill, Level } from '../../../types/common';
import { useLabels } from '../../../hooks/useLabels';
import { adminService } from '../../../services/adminService';
import { useApiError } from '../../../hooks/useApiError';

import { VocabularyForm } from './VocabularyForm';
import { ListeningForm } from './ListeningForm';
import { ReadingForm } from './ReadingForm';
import { WritingForm } from './WritingForm';
import { SpeakingForm } from './SpeakingForm';
import { ExamForm } from './ExamForm';

interface ContentEditorDrawerProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  /** Nếu null => Thêm mới. Nếu có id => Cập nhật */
  editingId: string | null;
}

const SKILLS: Skill[] = ['VOCABULARY', 'LISTENING', 'READING', 'WRITING', 'SPEAKING', 'EXAM'];
const LEVELS: Level[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

export const ContentEditorDrawer: React.FC<ContentEditorDrawerProps> = ({
  open,
  onClose,
  onSuccess,
  editingId,
}) => {
  const { t } = useTranslation();
  const { skill: skillLabel, level: levelLabel } = useLabels();
  const { message } = App.useApp();
  const { describe } = useApiError();
  const [form] = Form.useForm();
  
  const [saving, setSaving] = useState(false);
  const [loadedId, setLoadedId] = useState<string | null>(null);
  const selectedSkill = Form.useWatch<Skill | undefined>('skill', form);
  const loading = open && !!editingId && loadedId !== editingId;

  const onLoaded = useEffectEvent((id: string, data: Awaited<ReturnType<typeof adminService.getContent>>) => {
    form.setFieldsValue(toFormValues(data.payload));
    setLoadedId(id);
  });

  const onLoadError = useEffectEvent((err: unknown) => {
    message.error(describe(err, 'admin.loadError'));
    onClose();
  });

  useEffect(() => {
    if (!open) return;
    if (!editingId) {
      form.resetFields();
      return;
    }
    let cancelled = false;
    adminService.getContent(editingId).then(
      (data) => { if (!cancelled) onLoaded(editingId, data); },
      (err) => { if (!cancelled) onLoadError(err); },
    );
    return () => { cancelled = true; };
  }, [open, editingId, form]);

  const handleFinish = async (values: ContentFormValues) => {
    try {
      setSaving(true);
      const payload = toPayload(values);

      if (editingId) {
        await adminService.updateContent(editingId, payload);
        message.success(t('common.saved'));
      } else {
        await adminService.createContent(payload);
        message.success(t('common.created'));
      }
      onSuccess();
      onClose();
    } catch (err) {
      message.error(describe(err, 'admin.saveError'));
    } finally {
      setSaving(false);
    }
  };

  const renderSkillSpecificFields = () => {
    if (!selectedSkill) return null;

    return (
      <div className="mt-4">
        <h3 className="mb-4 text-card-title">{t('admin.detailsForSkill', { skill: skillLabel(selectedSkill) })}</h3>
        
        {selectedSkill === 'VOCABULARY' && <VocabularyForm />}
        {selectedSkill === 'LISTENING' && <ListeningForm />}
        {selectedSkill === 'READING' && <ReadingForm />}
        {selectedSkill === 'WRITING' && <WritingForm />}
        {selectedSkill === 'SPEAKING' && <SpeakingForm />}
        {selectedSkill === 'EXAM' && <ExamForm />}
      </div>
    );
  };

  const renderCsvImport = () => {
    return (
      <div className="mt-4">
        <h3 className="mb-4 text-card-title">Nhập dữ liệu từ CSV</h3>
        
        <div className="mb-6 rounded-md bg-brand-50 p-4 text-[13.5px] text-ink">
          <p className="mb-2 font-semibold">Cấu trúc file CSV yêu cầu (có hàng tiêu đề):</p>
          <ul className="ml-5 list-disc space-y-1 text-ink-subtle">
            <li><strong>Cột 1 (Đề bài):</strong> Nội dung đề bài chung (nếu có). Tất cả các câu hỏi sẽ được nhóm lại dưới đề bài này.</li>
            <li><strong>Cột 2 (Loại câu hỏi):</strong> Nhập <code>MULTIPLE_CHOICE</code> hoặc <code>FILL_BLANK</code>.</li>
            <li><strong>Cột 3 (Nội dung):</strong> Nội dung câu hỏi phụ. Với điền từ, dùng <code>[blank]</code> cho ô trống.</li>
            <li><strong>Cột 4, 5, 6, 7 (Lựa chọn):</strong> Các lựa chọn cho câu hỏi trắc nghiệm. Bỏ trống nếu là câu điền từ.</li>
            <li><strong>Cột 8 (Đáp án đúng):</strong> Nhập chính xác lựa chọn đúng. Nếu có nhiều đáp án, phân cách bằng dấu chấm phẩy (<code>;</code>).</li>
          </ul>
        </div>

        <Upload
          accept=".csv"
          beforeUpload={(file) => {
            const reader = new FileReader();
            reader.onload = (e) => {
              const text = e.target?.result as string;
              if (!text) return;
              
              const lines = text.split('\n').filter(line => line.trim().length > 0);
              if (lines.length < 2) {
                message.error('File CSV trống hoặc không đúng định dạng');
                return;
              }
              
              const parseCSVLine = (line: string) => {
                const result = [];
                let current = '';
                let inQuotes = false;
                for (let i = 0; i < line.length; i++) {
                  const char = line[i];
                  if (char === '"' && line[i+1] === '"' && inQuotes) {
                    current += '"';
                    i++;
                  } else if (char === '"') {
                    inQuotes = !inQuotes;
                  } else if (char === ',' && !inQuotes) {
                    result.push(current.trim());
                    current = '';
                  } else {
                    current += char;
                  }
                }
                result.push(current.trim());
                return result;
              };
              
              const importedItems: QuestionFormValue[] = [];
              let commonPrompt = form.getFieldValue('prompt') || '';
              
              for (let i = 1; i < lines.length; i++) {
                const values = parseCSVLine(lines[i]);
                
                const prompt = values[0];
                if (prompt && !commonPrompt) commonPrompt = prompt;
                
                const type: QuestionFormValue['type'] = values[1] === 'FILL_BLANK' ? 'FILL_BLANK' : 'MULTIPLE_CHOICE';
                const content = values[2] || '';
                
                const options = [];
                if (values[3]) options.push(values[3]);
                if (values[4]) options.push(values[4]);
                if (values[5]) options.push(values[5]);
                if (values[6]) options.push(values[6]);
                
                const correctStr = values[7] || '';
                const parsedAnswers = correctStr.split(';').map(s => s.trim()).filter(Boolean);
                // Trắc nghiệm chỉ có đúng một đáp án đúng.
                const correctAnswers = type === 'MULTIPLE_CHOICE' ? parsedAnswers.slice(0, 1) : parsedAnswers;
                
                importedItems.push({
                  type,
                  content,
                  options,
                  correctAnswers
                });
              }
              
              // Giữ lại các câu hỏi hiện có và thêm câu hỏi mới
              const currentItems = form.getFieldValue('items') || [];
              form.setFieldsValue({ 
                prompt: commonPrompt,
                items: [...currentItems, ...importedItems] 
              });
              
              message.success(`Đã nhập thành công ${importedItems.length} câu hỏi. Hãy quay lại tab "Nhập thủ công" để kiểm tra.`);
            };
            reader.readAsText(file);
            return false;
          }}
          showUploadList={false}
        >
          <Button type="primary" className="bg-action">
            <span className="material-symbols-outlined mr-2 text-[20px]">upload_file</span>
            Chọn file CSV
          </Button>
        </Upload>
      </div>
    );
  };

  return (
    <Drawer
      title={editingId ? t('admin.editContent', 'Chỉnh sửa Nội dung') : t('admin.addContent', 'Thêm mới Nội dung')}
      width={800}
      onClose={onClose}
      open={open}
      destroyOnClose
      extra={
        <Space>
          <Button onClick={onClose} disabled={saving}>{t('common.cancel')}</Button>
          <Button type="primary" onClick={() => form.submit()} loading={saving} className="bg-action">
            {t('common.save')}
          </Button>
        </Space>
      }
    >
      {loading ? (
        <div className="flex h-40 items-center justify-center">
          <Spin size="large" />
        </div>
      ) : (
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFinish}
        >
          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="skill"
              label={t('filter.skill')}
              rules={[{ required: true }]}
              className="col-span-1"
            >
              <Select
                disabled={!!editingId} // Không cho đổi skill khi đang sửa
                options={SKILLS.map(s => ({ value: s, label: skillLabel(s) }))}
              />
            </Form.Item>
            <Form.Item
              name="level"
              label={t('filter.level')}
              rules={[{ required: true }]}
              className="col-span-1"
            >
              <Select
                options={LEVELS.map(l => ({ value: l, label: levelLabel(l) }))}
              />
            </Form.Item>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Form.Item
              name="titleVi"
              label={t('admin.titleVi', 'Tiêu đề (Tiếng Việt)')}
              rules={[{ required: true }]}
              className="col-span-1"
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="titleEn"
              label={t('admin.titleEn', 'Tiêu đề (Tiếng Anh)')}
              rules={[{ required: true }]}
              className="col-span-1"
            >
              <Input />
            </Form.Item>
          </div>

          {selectedSkill && (
            <div className="mt-6 rounded-md border border-hairline bg-surface p-4 shadow-sm">
              <Tabs
                defaultActiveKey="manual"
                items={[
                  {
                    key: 'manual',
                    label: 'Nhập thủ công',
                    children: renderSkillSpecificFields(),
                  },
                  {
                    key: 'csv',
                    label: 'Nhập từ CSV',
                    children: renderCsvImport(),
                  }
                ]}
              />
            </div>
          )}
        </Form>
      )}
    </Drawer>
  );
};
