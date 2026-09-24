import React, { useState } from 'react';
import { Form, Input } from 'antd';
import type { NamePath } from 'antd/es/form/interface';
import { useTranslation } from 'react-i18next';

interface ImageUrlFieldProps {
  /** Tên trường URL (trong Form.List truyền dạng `[name, 'imageUrl']`). */
  name: NamePath;
  /** Đường dẫn đầy đủ để theo dõi giá trị xem trước (trong Form.List: `['items', name, 'imageUrl']`). */
  watchPath?: NamePath;
  /** Hiện ô ghi công tác giả ảnh. */
  withAuthor?: boolean;
  label?: string;
  compact?: boolean;
}

const URL_PATTERN = /^https?:\/\/\S+$/i;

/** Ô dán URL ảnh minh hoạ + ảnh xem trước (ảnh giữ đường dẫn ngoài). */
export const ImageUrlField: React.FC<ImageUrlFieldProps> = ({ name, watchPath, withAuthor, label, compact }) => {
  const { t } = useTranslation();
  const form = Form.useFormInstance();
  const url = Form.useWatch(watchPath ?? name, form) as string | undefined;
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const trimmed = url?.trim() ?? '';
  const showPreview = URL_PATTERN.test(trimmed);
  const broken = showPreview && failedUrl === trimmed;

  return (
    <div className="flex items-start gap-3">
      <div className="min-w-0 flex-1">
        <Form.Item
          name={name}
          label={label ?? t('contentForm.imageUrl')}
          rules={[{ pattern: URL_PATTERN, message: t('contentForm.imageUrlInvalid') }]}
          className={compact ? 'mb-0' : undefined}
        >
          <Input placeholder="https://..." allowClear />
        </Form.Item>
        {withAuthor && (
          <Form.Item name="imageAuthor" label={t('contentForm.imageAuthor')}>
            <Input />
          </Form.Item>
        )}
      </div>
      <div
        className={`mt-[30px] flex shrink-0 items-center justify-center overflow-hidden rounded border border-hairline bg-surface ${
          compact ? 'h-14 w-14' : 'h-20 w-28'
        }`}
      >
        {showPreview && !broken ? (
          <img
            src={trimmed}
            alt=""
            className="h-full w-full object-cover"
            onError={() => setFailedUrl(trimmed)}
          />
        ) : (
          <span
            className="material-symbols-outlined text-[20px] text-ink-muted"
            title={broken ? t('contentForm.imageBroken') : undefined}
          >
            {broken ? 'broken_image' : 'image'}
          </span>
        )}
      </div>
    </div>
  );
};
