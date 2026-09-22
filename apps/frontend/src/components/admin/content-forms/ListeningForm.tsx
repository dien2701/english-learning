import React from 'react';
import { Form, Input, InputNumber } from 'antd';
import type { ListeningAudio } from '../../../types/admin';
import { ListeningAudioPanel } from './ListeningAudioPanel';
import { useTranslation } from 'react-i18next';
import { SharedQuestionList } from './SharedQuestionList';
import { TopicField } from './TopicField';

interface ListeningFormProps {
  lessonId: string | null;
  audio: Pick<ListeningAudio, 'audioUrl' | 'audioSource'>;
  transcriptChanged: boolean;
  onAudioChange: (audio: ListeningAudio) => void;
}

export const ListeningForm: React.FC<ListeningFormProps> = ({
  lessonId,
  audio,
  transcriptChanged,
  onAudioChange,
}) => {
  const { t } = useTranslation();

  return (
    <div className="mt-4">
      <TopicField />
      <Form.Item name="prompt" label={t('contentForm.descOptional')}>
        <Input.TextArea rows={2} />
      </Form.Item>
      <ListeningAudioPanel
        lessonId={lessonId}
        audio={audio}
        transcriptChanged={transcriptChanged}
        onChange={onAudioChange}
      />
      <div className="grid grid-cols-2 gap-4">
        <Form.Item
          name="mediaUrl"
          label={t('contentForm.audioUrl')}
          extra={t('contentForm.audioManualUrlHint')}
        >
          <Input placeholder="https://..." disabled={audio.audioSource !== null} />
        </Form.Item>
        <Form.Item name="durationSeconds" label={t('contentForm.durationSeconds')} initialValue={0}>
          <InputNumber min={0} className="w-full" />
        </Form.Item>
      </div>
      <Form.Item name="contentBody" label={t('contentForm.transcript')} rules={[{ required: true }]}>
        <Input.TextArea rows={4} />
      </Form.Item>

      <SharedQuestionList name="items" label={t('contentForm.listeningQuestions')} />
    </div>
  );
};
