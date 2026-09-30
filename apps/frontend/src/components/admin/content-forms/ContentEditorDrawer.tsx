import React, { useEffect, useEffectEvent, useState } from 'react';
import { Drawer, Form, Input, Select, Space, Button, App, Spin, Tabs, Upload } from 'antd';
import { Trans, useTranslation } from 'react-i18next';
import {
  toFormValues,
  toPayload,
  type CardFormValue,
  type ContentFormValues,
  type PromptFormValue,
  type QuestionFormValue,
} from './contentMapping';
import type { Skill, Level } from '../../../types/common';
import { useLabels } from '../../../hooks/useLabels';
import { adminService } from '../../../services/adminService';
import { useApiError } from '../../../hooks/useApiError';
import type { AdminContentPayload, ListeningAudio } from '../../../types/admin';

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

/** Thẻ dùng trong chuỗi dịch của hướng dẫn CSV. */
const csvTags = { strong: <strong />, code: <code /> };

/** Skill dùng chung dạng câu hỏi (SharedQuestionList): cấu trúc CSV 8 cột như cũ. */
const QUESTION_SKILLS: Skill[] = ['LISTENING', 'READING', 'EXAM'];

/** Mỗi skill có shape item khác nhau nên cột CSV (và hướng dẫn) khác nhau. */
const CSV_COLUMN_KEYS: Record<Skill, string[]> = {
  LISTENING: ['csvCol1', 'csvCol2', 'csvCol3', 'csvCol4', 'csvCol8'],
  READING: ['csvCol1', 'csvCol2', 'csvCol3', 'csvCol4', 'csvCol8'],
  EXAM: ['csvCol1', 'csvCol2', 'csvCol3', 'csvCol4', 'csvCol8'],
  VOCABULARY: ['csvColVocab1', 'csvColVocab2', 'csvColVocab3', 'csvColVocab4', 'csvColVocab5'],
  SPEAKING: ['csvColSpeaking1', 'csvColSpeaking2'],
  WRITING: ['csvColWriting1'],
};

const parseCSVLine = (line: string): string[] => {
  const result: string[] = [];
  let current = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    const char = line[i];
    if (char === '"' && line[i + 1] === '"' && inQuotes) {
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
  const transcript = Form.useWatch<string | undefined>('contentBody', form);
  const [loadedAudio, setAudio] = useState<Pick<ListeningAudio, 'audioUrl' | 'audioSource'>>({
    audioUrl: null,
    audioSource: null,
  });
  const [loadedTranscript, setLoadedTranscript] = useState('');
  const [loadedPayload, setLoadedPayload] = useState<AdminContentPayload | undefined>(undefined);
  const loading = open && !!editingId && loadedId !== editingId;
  // Đang tạo mới thì bỏ qua audio/transcript còn sót từ lần sửa trước.
  const audio = editingId ? loadedAudio : { audioUrl: null, audioSource: null };

  const onLoaded = useEffectEvent((id: string, data: Awaited<ReturnType<typeof adminService.getContent>>) => {
    const values = toFormValues(data.payload);
    form.setFieldsValue(values);
    setAudio({ audioUrl: values.mediaUrl ?? null, audioSource: data.audioSource ?? null });
    setLoadedTranscript(values.contentBody ?? '');
    setLoadedPayload(data.payload);
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
      const payload = toPayload(values, editingId ? loadedPayload : undefined);

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
        {selectedSkill === 'LISTENING' && (
          <ListeningForm
            lessonId={editingId}
            audio={audio}
            transcriptChanged={!!editingId && (transcript ?? '') !== loadedTranscript}
            onAudioChange={(next) => {
              setAudio(next);
              form.setFieldValue('mediaUrl', next.audioUrl ?? undefined);
            }}
          />
        )}
        {selectedSkill === 'READING' && <ReadingForm />}
        {selectedSkill === 'WRITING' && <WritingForm />}
        {selectedSkill === 'SPEAKING' && <SpeakingForm />}
        {selectedSkill === 'EXAM' && <ExamForm />}
      </div>
    );
  };

  const renderCsvImport = () => {
    if (!selectedSkill) return null;
    const skill = selectedSkill;
    const columnKeys = CSV_COLUMN_KEYS[skill];

    return (
      <div className="mt-4">
        <h3 className="mb-4 text-card-title">{t('contentForm.csvTitle')}</h3>

        <div className="mb-6 rounded-md bg-brand-50 p-4 text-[13.5px] text-ink">
          <p className="mb-2 font-semibold">{t('contentForm.csvStructure')}</p>
          <ul className="ml-5 list-disc space-y-1 text-ink-subtle">
            {columnKeys.map((key) => (
              <li key={key}><Trans i18nKey={`contentForm.${key}`} components={csvTags} /></li>
            ))}
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
                message.error(t('contentForm.csvEmpty'));
                return;
              }
              const rows = lines.slice(1).map(parseCSVLine);
              let importedCount = 0;

              if (QUESTION_SKILLS.includes(skill)) {
                let commonPrompt = form.getFieldValue('prompt') || '';
                const importedItems: QuestionFormValue[] = rows.map((values) => {
                  const prompt = values[0];
                  if (prompt && !commonPrompt) commonPrompt = prompt;

                  const type: QuestionFormValue['type'] = values[1] === 'FILL_BLANK' ? 'FILL_BLANK' : 'MULTIPLE_CHOICE';
                  const content = values[2] || '';
                  const options = [values[3], values[4], values[5], values[6]].filter((v): v is string => !!v);

                  const correctStr = values[7] || '';
                  const parsedAnswers = correctStr.split(';').map(s => s.trim()).filter(Boolean);
                  // Trắc nghiệm chỉ có đúng một đáp án đúng.
                  const correctAnswers = type === 'MULTIPLE_CHOICE' ? parsedAnswers.slice(0, 1) : parsedAnswers;

                  return { type, content, options, correctAnswers };
                });
                const currentItems = form.getFieldValue('items') || [];
                form.setFieldsValue({ prompt: commonPrompt, items: [...currentItems, ...importedItems] });
                importedCount = importedItems.length;
              } else if (skill === 'VOCABULARY') {
                const importedItems: CardFormValue[] = rows
                  .filter((values) => values[0])
                  .map((values) => ({
                    word: values[0] || '',
                    phonetic: values[1] || undefined,
                    meaningVi: values[2] || '',
                    example: values[3] || undefined,
                    imageUrl: values[4] || undefined,
                  }));
                const currentItems = form.getFieldValue('items') || [];
                form.setFieldsValue({ items: [...currentItems, ...importedItems] });
                importedCount = importedItems.length;
              } else if (skill === 'SPEAKING') {
                const importedItems: PromptFormValue[] = rows
                  .filter((values) => values[0])
                  .map((values) => ({
                    text: values[0] || '',
                    meaningVi: values[1] || undefined,
                  }));
                const currentItems = form.getFieldValue('items') || [];
                form.setFieldsValue({ items: [...currentItems, ...importedItems] });
                importedCount = importedItems.length;
              } else if (skill === 'WRITING') {
                const importedHints = rows.map((values) => values[0]).filter(Boolean);
                const currentHints = form.getFieldValue('hints') || [];
                form.setFieldsValue({ hints: [...currentHints, ...importedHints] });
                importedCount = importedHints.length;
              }

              message.success(t('contentForm.csvImported', { count: importedCount }));
            };
            reader.readAsText(file);
            return false;
          }}
          showUploadList={false}
        >
          <Button type="primary" className="bg-action">
            <span className="material-symbols-outlined mr-2 text-[20px]">upload_file</span>
            {t('contentForm.chooseCsv')}
          </Button>
        </Upload>
      </div>
    );
  };

  return (
    <Drawer
      title={editingId ? t('admin.editContent') : t('admin.addContent')}
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
              label={t('admin.titleVi')}
              rules={[{ required: true }]}
              className="col-span-1"
            >
              <Input />
            </Form.Item>
            <Form.Item
              name="titleEn"
              label={t('admin.titleEn')}
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
                    label: t('contentForm.tabManual'),
                    children: renderSkillSpecificFields(),
                  },
                  {
                    key: 'csv',
                    label: t('contentForm.tabCsv'),
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
