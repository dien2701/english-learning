import type { Question } from '../../types/practice';

/** Mô tả rút gọn một câu trắc nghiệm khi khai báo dữ liệu mẫu. */
export interface ChoiceSeed {
  text: string;
  /** Các phương án; phương án đúng đánh dấu bằng dấu * ở đầu. */
  options: string[];
  explanation?: string;
}

/** Mô tả rút gọn một câu điền từ. */
export interface BlankSeed {
  text: string;
  answer: string;
  explanation?: string;
}

/**
 * Dựng danh sách câu hỏi có đầy đủ đáp án.
 *
 * Dữ liệu ở đây luôn chứa đáp án đúng; chính handler mới là nơi quyết định
 * có gửi đáp án xuống giao diện hay không. Khi người dùng đang làm bài,
 * handler lược bỏ đáp án đi — giống hệt cách backend thật phải làm.
 */
export function buildQuestions(
  prefix: string,
  seeds: Array<ChoiceSeed | BlankSeed>,
): Question[] {
  return seeds.map((seed, index) => {
    const id = `${prefix}-q${index + 1}`;
    const order = index + 1;

    if ('options' in seed) {
      const options = seed.options.map((raw, i) => ({
        id: `${id}-o${i + 1}`,
        text: raw.replace(/^\*/, ''),
      }));

      const correctIndex = seed.options.findIndex((o) => o.startsWith('*'));

      return {
        id,
        kind: 'SINGLE_CHOICE' as const,
        order,
        text: seed.text,
        options,
        correctOptionId: options[correctIndex === -1 ? 0 : correctIndex].id,
        explanation: seed.explanation,
      };
    }

    return {
      id,
      kind: 'FILL_BLANK' as const,
      order,
      text: seed.text,
      correctText: seed.answer,
      explanation: seed.explanation,
    };
  });
}

/** Bỏ mọi thông tin đáp án trước khi trả về cho người đang làm bài. */
export function withoutAnswers(questions: Question[]): Question[] {
  return questions.map((q) => ({
    id: q.id,
    kind: q.kind,
    order: q.order,
    text: q.text,
    options: q.options,
  }));
}

/** So khớp đáp án điền từ: bỏ qua hoa thường, khoảng trắng thừa và dấu câu cuối. */
export function isTextAnswerCorrect(userText: string, expected: string): boolean {
  const normalize = (v: string) =>
    v.trim().toLowerCase().replace(/[.,!?;:]+$/, '').replace(/\s+/g, ' ');
  return normalize(userText) === normalize(expected);
}
