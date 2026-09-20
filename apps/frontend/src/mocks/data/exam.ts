import type { ExamDetail } from '../../types/practice';
import type { Skill } from '../../types/common';
import { buildQuestions } from './questions';

/**
 * Bài kiểm tra mẫu.
 *
 * Mỗi câu hỏi được gán một kỹ năng để màn hình kết quả tách điểm theo
 * kỹ năng được. Ánh xạ này nằm riêng thay vì nhét vào Question, vì chỉ
 * bài kiểm tra mới cần tới nó.
 */
export const examQuestionSkill: Record<string, Skill> = {};

function assignSkills(prefix: string, skills: Skill[]): void {
  skills.forEach((skill, index) => {
    examQuestionSkill[`${prefix}-q${index + 1}`] = skill;
  });
}

export const exams: ExamDetail[] = [
  {
    id: 'ex-005',
    title: { vi: 'Kiểm tra tổng hợp giữa khoá', en: 'Mid-course Mixed Test' },
    description: {
      vi: 'Bài thi thử bao phủ từ vựng, ngữ pháp, đọc hiểu và nghe hiểu.',
      en: 'A mock test covering vocabulary, grammar, reading and listening.',
    },
    skills: ['VOCABULARY', 'READING', 'LISTENING'],
    level: 'INTERMEDIATE',
    questionCount: 8,
    timeLimitMinutes: 20,
    status: 'COMPLETED',
    lastScore: 8.2,
    questions: buildQuestions('ex-005', [
      {
        text: 'Choose the correct word: The company will ______ the invoice next week.',
        options: ['*issue', 'issued', 'issuing', 'issues'],
        explanation: 'Sau "will" dùng động từ nguyên thể không "to".',
      },
      {
        text: 'Choose the word closest in meaning to "substantial".',
        options: ['small', '*considerable', 'unclear', 'temporary'],
      },
      {
        text: 'Which sentence is grammatically correct?',
        options: [
          'He have finished the report.',
          '*He has finished the report.',
          'He finished have the report.',
          'He has finish the report.',
        ],
      },
      {
        text: 'Fill in the blank: She is responsible ______ managing the budget.',
        answer: 'for',
        explanation: 'Cụm cố định: be responsible for something.',
      },
      {
        text: 'Read: "The shipment arrives Tuesday." When does it arrive?',
        options: ['Monday', '*Tuesday', 'Wednesday', 'Thursday'],
      },
      {
        text: 'Choose the best reply: "Could you send me the agenda?"',
        options: [
          'Yes, I am.',
          '*Sure, I will send it right away.',
          'No, thanks.',
          'It is on Monday.',
        ],
      },
      {
        text: 'Choose the correct preposition: The meeting was moved ______ Wednesday.',
        options: ['at', 'in', '*to', 'on'],
      },
      {
        text: 'Fill in the blank: Prices ______ sharply during the period.',
        answer: 'fluctuated',
      },
    ]),
  },
  {
    id: 'ex-001',
    title: {
      vi: 'Kiểm tra đầu vào — Xác định trình độ',
      en: 'Placement Test — Find Your Level',
    },
    description: {
      vi: 'Bài ngắn giúp hệ thống ước lượng trình độ hiện tại của bạn.',
      en: 'A short test so the system can estimate your current level.',
    },
    skills: ['VOCABULARY', 'READING'],
    level: 'BEGINNER',
    questionCount: 5,
    timeLimitMinutes: 10,
    status: 'NOT_TAKEN',
    questions: buildQuestions('ex-001', [
      {
        text: 'Choose the correct form: She ______ to work by bus every day.',
        options: ['go', '*goes', 'going', 'gone'],
      },
      {
        text: 'What is the opposite of "expensive"?',
        options: ['costly', '*cheap', 'rich', 'valuable'],
      },
      {
        text: 'Choose the correct article: I bought ______ umbrella yesterday.',
        options: ['a', '*an', 'the', 'no article'],
      },
      {
        text: 'Fill in the blank: My brother ______ playing football.',
        answer: 'likes',
      },
      {
        text: 'Read: "The library closes at 8 p.m." What time does it close?',
        options: ['6 p.m.', '7 p.m.', '*8 p.m.', '9 p.m.'],
      },
    ]),
  },
  {
    id: 'ex-012',
    title: {
      vi: 'TOEIC mô phỏng — Phần Reading rút gọn',
      en: 'TOEIC Simulation — Short Reading Section',
    },
    description: {
      vi: 'Tập trung vào ngữ pháp và đọc hiểu theo định dạng TOEIC.',
      en: 'Focused on grammar and reading in the TOEIC format.',
    },
    skills: ['READING', 'VOCABULARY'],
    level: 'ADVANCED',
    questionCount: 6,
    timeLimitMinutes: 15,
    status: 'NOT_TAKEN',
    questions: buildQuestions('ex-012', [
      {
        text: 'The proposal was rejected ______ its high cost.',
        options: ['despite', '*because of', 'although', 'however'],
      },
      {
        text: 'All employees ______ attend the safety briefing.',
        options: ['*must', 'musts', 'to must', 'musting'],
      },
      {
        text: 'Choose the word closest in meaning to "postpone".',
        options: ['cancel', '*delay', 'attend', 'confirm'],
      },
      {
        text: 'Fill in the blank: The report must be submitted ______ Friday.',
        answer: 'by',
      },
      {
        text: 'Neither the manager nor the assistants ______ available today.',
        options: ['is', '*are', 'was', 'has been'],
        explanation: 'Với "neither… nor", động từ chia theo chủ ngữ gần nhất.',
      },
      {
        text: 'Choose the correct sentence.',
        options: [
          'We look forward to hear from you.',
          '*We look forward to hearing from you.',
          'We look forward hear from you.',
          'We look forward for hearing from you.',
        ],
      },
    ]),
  },
];

assignSkills('ex-005', [
  'VOCABULARY',
  'VOCABULARY',
  'READING',
  'VOCABULARY',
  'READING',
  'LISTENING',
  'READING',
  'VOCABULARY',
]);
assignSkills('ex-001', [
  'VOCABULARY',
  'VOCABULARY',
  'VOCABULARY',
  'VOCABULARY',
  'READING',
]);
assignSkills('ex-012', [
  'READING',
  'READING',
  'VOCABULARY',
  'READING',
  'READING',
  'VOCABULARY',
]);

export function findExam(id: string): ExamDetail | undefined {
  return exams.find((e) => e.id === id);
}
