import React, { useState } from 'react';
import { Alert, App, Button, Popconfirm, Space, Upload } from 'antd';
import { useTranslation } from 'react-i18next';
import { adminService } from '../../../services/adminService';
import { useApiError } from '../../../hooks/useApiError';
import type { ListeningAudio } from '../../../types/admin';

const MAX_BYTES = 20 * 1024 * 1024;
const ACCEPTED = /\.(mp3|m4a|wav)$/i;

interface ListeningAudioPanelProps {
  /** Null khi đang tạo mới: chưa có id nên chưa gọi được API audio. */
  lessonId: string | null;
  audio: Pick<ListeningAudio, 'audioUrl' | 'audioSource'>;
  /** Bài TTS mà transcript đã bị sửa so với lúc mở. */
  transcriptChanged: boolean;
  onChange: (audio: ListeningAudio) => void;
}

export const ListeningAudioPanel: React.FC<ListeningAudioPanelProps> = ({
  lessonId,
  audio,
  transcriptChanged,
  onChange,
}) => {
  const { t } = useTranslation();
  const { message } = App.useApp();
  const { describe } = useApiError();
  const [busy, setBusy] = useState<'generate' | 'upload' | 'remove' | null>(null);

  const run = async (
    kind: 'generate' | 'upload' | 'remove',
    action: () => Promise<ListeningAudio>,
    successKey: string,
  ) => {
    try {
      setBusy(kind);
      onChange(await action());
      message.success(t(successKey));
    } catch (err) {
      message.error(describe(err));
    } finally {
      setBusy(null);
    }
  };

  const sourceLabel =
    audio.audioSource === 'TTS'
      ? t('contentForm.audioSourceTts')
      : audio.audioSource === 'UPLOAD'
        ? t('contentForm.audioSourceUpload')
        : audio.audioUrl
          ? audio.audioUrl
          : t('contentForm.audioSourceNone');

  const hasAudio = Boolean(audio.audioUrl);

  return (
    <div className="mb-4 rounded-md border border-hairline bg-surface-muted p-4">
      <p className="mb-2 font-semibold">{t('contentForm.audioTitle')}</p>
      <p className="mb-3 text-caption text-ink-muted">
        {t('contentForm.audioSourceLabel')}: <strong>{sourceLabel}</strong>
      </p>

      {hasAudio && (
        <audio controls preload="none" src={audio.audioUrl ?? undefined} className="mb-3 w-full" />
      )}

      {transcriptChanged && audio.audioSource === 'TTS' && (
        <Alert type="warning" showIcon className="mb-3" message={t('contentForm.audioTranscriptChanged')} />
      )}

      {lessonId ? (
        <Space wrap>
          <Button
            type="primary"
            className="bg-action"
            loading={busy === 'generate'}
            disabled={busy !== null && busy !== 'generate'}
            onClick={() =>
              run('generate', () => adminService.generateListeningAudio(lessonId), 'contentForm.audioGenerated')
            }
          >
            {audio.audioSource === 'TTS' ? t('contentForm.audioRegenerate') : t('contentForm.audioGenerate')}
          </Button>

          <Upload
            accept=".mp3,.m4a,.wav"
            showUploadList={false}
            disabled={busy !== null}
            beforeUpload={(file) => {
              if (!ACCEPTED.test(file.name)) {
                message.error(t('contentForm.audioFileType'));
              } else if (file.size > MAX_BYTES) {
                message.error(t('contentForm.audioFileTooLarge'));
              } else {
                void run('upload', () => adminService.uploadListeningAudio(lessonId, file), 'contentForm.audioUploaded');
              }
              return false;
            }}
          >
            <Button loading={busy === 'upload'} disabled={busy !== null && busy !== 'upload'}>
              {t('contentForm.audioUpload')}
            </Button>
          </Upload>

          {hasAudio && (
            <Popconfirm
              title={t('contentForm.audioRemove')}
              okText={t('common.delete')}
              cancelText={t('common.cancel')}
              onConfirm={() => run('remove', () => adminService.removeListeningAudio(lessonId), 'contentForm.audioRemoved')}
            >
              <Button danger loading={busy === 'remove'} disabled={busy !== null && busy !== 'remove'}>
                {t('contentForm.audioRemove')}
              </Button>
            </Popconfirm>
          )}
        </Space>
      ) : (
        <p className="text-caption text-ink-muted">{t('contentForm.audioSaveFirst')}</p>
      )}
    </div>
  );
};
