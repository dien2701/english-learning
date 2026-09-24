import type {
  AdminCardInput,
  AdminContentPayload,
  AdminPromptInput,
  AdminQuestionInput,
} from '../../../types/admin';
import type { Level, Skill } from '../../../types/common';

/** Loại câu hỏi trong form: giữ tên cũ, đổi sang `SINGLE_CHOICE` khi gửi BE. */
export type QuestionFormType = 'MULTIPLE_CHOICE' | 'FILL_BLANK';

export interface QuestionFormValue {
  id?: string;
  /** Chỉ dùng cho đề kiểm tra. */
  skill?: Skill;
  type: QuestionFormType;
  content: string;
  explanation?: string;
  options?: string[];
  /** Trắc nghiệm: đúng một phần tử (nội dung lựa chọn đúng). Điền từ: mọi đáp án chấp nhận. */
  correctAnswers?: string[];
}

export interface CardFormValue {
  id?: string;
  word: string;
  meaningVi: string;
  phonetic?: string;
  example?: string;
  imageUrl?: string;
}

export interface PromptFormValue {
  id?: string;
  text: string;
  meaningVi?: string;
}

export interface ContentFormValues {
  skill: Skill;
  level: Level;
  titleVi: string;
  titleEn?: string;
  topicId?: string;
  /** Mô tả ngắn, lưu vào `descriptionVi`. */
  prompt?: string;
  /** Ảnh minh hoạ (bộ từ: ảnh bìa) và ghi công. */
  imageUrl?: string;
  imageAuthor?: string;
  /** Đường dẫn audio (Nghe). */
  mediaUrl?: string;
  durationSeconds?: number | null;
  timeLimitMinutes?: number | null;
  /** Nghe: transcript. Đọc: các đoạn văn, cách nhau bằng một dòng trống. */
  contentBody?: string;
  instructions?: string;
  suggestedMinutes?: number | null;
  minWords?: number | null;
  hints?: string[];
  items?: unknown[];
}

const num = (value: number | null | undefined): number | undefined => value ?? undefined;

const splitParagraphs = (text: string | undefined): string[] =>
  (text ?? '')
    .split(/\n\s*\n/)
    .map((part) => part.trim())
    .filter(Boolean);

function toQuestion(q: QuestionFormValue, withSkill: boolean): AdminQuestionInput {
  const base = {
    id: q.id,
    skill: withSkill ? q.skill : undefined,
    content: q.content,
    explanation: q.explanation,
  };
  if (q.type === 'FILL_BLANK') {
    return { ...base, kind: 'FILL_BLANK', acceptedAnswers: q.correctAnswers ?? [] };
  }
  const answer = q.correctAnswers?.[0];
  const options = (q.options ?? []).filter((text) => text.trim() !== '');
  const correctIndex = options.findIndex((text) => text === answer);
  return {
    ...base,
    kind: 'SINGLE_CHOICE',
    options: options.map((content, index) => ({ content, correct: index === correctIndex })),
  };
}

function fromQuestion(q: AdminQuestionInput): QuestionFormValue {
  if (q.kind === 'FILL_BLANK') {
    return { id: q.id, skill: q.skill, type: 'FILL_BLANK', content: q.content, explanation: q.explanation, correctAnswers: q.acceptedAnswers ?? [] };
  }
  const options = q.options ?? [];
  return {
    id: q.id,
    skill: q.skill,
    type: 'MULTIPLE_CHOICE',
    content: q.content,
    explanation: q.explanation,
    options: options.map((o) => o.content),
    correctAnswers: options.filter((o) => o.correct).map((o) => o.content),
  };
}

/**
 * Đổi giá trị form sang body gửi BE (`AdminContentPayload`). PUT thay toàn bộ nên trộn với `original`
 * (payload lúc tải) để giữ các trường form không hiển thị (mô tả EN, nghĩa EN, audio thẻ...).
 */
export function toPayload(values: ContentFormValues, original?: AdminContentPayload): AdminContentPayload {
  const mapped = mapPayload(values);
  if (!original || original.skill !== mapped.skill) return mapped;
  if (mapped.skill === 'VOCABULARY' && original.skill === 'VOCABULARY') {
    return { ...original, ...mapped, cards: mergeById(original.cards, mapped.cards) };
  }
  if (mapped.skill === 'SPEAKING' && original.skill === 'SPEAKING') {
    return { ...original, ...mapped, prompts: mergeById(original.prompts, mapped.prompts) };
  }
  return { ...original, ...mapped } as AdminContentPayload;
}

/** Phần tử con có `id` giữ lại các trường cũ mà form không sửa. */
function mergeById<T extends { id?: string }>(original: T[], edited: T[]): T[] {
  const byId = new Map(original.filter((item) => item.id).map((item) => [item.id, item]));
  return edited.map((item) => ({ ...(item.id ? byId.get(item.id) : undefined), ...item }));
}

function mapPayload(values: ContentFormValues): AdminContentPayload {
  const base = { titleVi: values.titleVi, titleEn: values.titleEn, level: values.level };
  const topic = { ...base, topicId: values.topicId ?? '' };
  const description = { descriptionVi: values.prompt };
  const image = { imageUrl: values.imageUrl?.trim() || undefined, imageAuthor: values.imageAuthor?.trim() || undefined };
  const items = values.items ?? [];

  switch (values.skill) {
    case 'VOCABULARY':
      return {
        ...topic,
        ...description,
        skill: 'VOCABULARY',
        coverImageUrl: image.imageUrl,
        coverImageAuthor: image.imageAuthor,
        cards: items as AdminCardInput[],
      };
    case 'LISTENING':
      return {
        ...topic,
        ...description,
        ...image,
        skill: 'LISTENING',
        audioUrl: values.mediaUrl,
        durationSeconds: num(values.durationSeconds),
        transcript: values.contentBody ?? '',
        questions: (items as QuestionFormValue[]).map((q) => toQuestion(q, false)),
      };
    case 'READING':
      return {
        ...topic,
        ...description,
        ...image,
        skill: 'READING',
        timeLimitMinutes: num(values.timeLimitMinutes),
        paragraphs: splitParagraphs(values.contentBody),
        questions: (items as QuestionFormValue[]).map((q) => toQuestion(q, false)),
      };
    case 'WRITING':
      return {
        ...topic,
        ...image,
        skill: 'WRITING',
        instructions: values.instructions ?? '',
        suggestedMinutes: num(values.suggestedMinutes),
        minWords: num(values.minWords),
        hints: (values.hints ?? []).filter((hint) => hint?.trim()),
      };
    case 'SPEAKING':
      return { ...topic, ...description, ...image, skill: 'SPEAKING', prompts: items as AdminPromptInput[] };
    case 'EXAM':
      return {
        ...base,
        ...description,
        skill: 'EXAM',
        timeLimitMinutes: num(values.timeLimitMinutes),
        questions: (items as QuestionFormValue[]).map((q) => toQuestion(q, true)),
      };
  }
}

/** Đổi payload BE (từ GET chi tiết) sang giá trị để điền form. */
export function toFormValues(payload: AdminContentPayload): ContentFormValues {
  const common = { skill: payload.skill, level: payload.level, titleVi: payload.titleVi, titleEn: payload.titleEn };
  switch (payload.skill) {
    case 'VOCABULARY':
      return {
        ...common,
        topicId: payload.topicId,
        prompt: payload.descriptionVi,
        imageUrl: payload.coverImageUrl,
        imageAuthor: payload.coverImageAuthor,
        items: payload.cards,
      };
    case 'LISTENING':
      return {
        ...common,
        topicId: payload.topicId,
        imageUrl: payload.imageUrl,
        imageAuthor: payload.imageAuthor,
        prompt: payload.descriptionVi,
        mediaUrl: payload.audioUrl,
        durationSeconds: payload.durationSeconds,
        contentBody: payload.transcript,
        items: payload.questions.map(fromQuestion),
      };
    case 'READING':
      return {
        ...common,
        topicId: payload.topicId,
        imageUrl: payload.imageUrl,
        imageAuthor: payload.imageAuthor,
        prompt: payload.descriptionVi,
        timeLimitMinutes: payload.timeLimitMinutes,
        contentBody: payload.paragraphs.join('\n\n'),
        items: payload.questions.map(fromQuestion),
      };
    case 'WRITING':
      return {
        ...common,
        topicId: payload.topicId,
        imageUrl: payload.imageUrl,
        imageAuthor: payload.imageAuthor,
        instructions: payload.instructions,
        suggestedMinutes: payload.suggestedMinutes,
        minWords: payload.minWords,
        hints: payload.hints ?? [],
      };
    case 'SPEAKING':
      return {
        ...common,
        topicId: payload.topicId,
        prompt: payload.descriptionVi,
        imageUrl: payload.imageUrl,
        imageAuthor: payload.imageAuthor,
        items: payload.prompts,
      };
    case 'EXAM':
      return {
        ...common,
        prompt: payload.descriptionVi,
        timeLimitMinutes: payload.timeLimitMinutes,
        items: payload.questions.map(fromQuestion),
      };
  }
}
