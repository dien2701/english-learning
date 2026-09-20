import type { ReadingDetail } from '../../types/practice';
import { buildQuestions } from './questions';
import { topicName } from './topics';

export const readingLessons: ReadingDetail[] = [
  {
    id: 'rd-008',
    title: {
      vi: 'Thói quen của người thành công',
      en: 'The Habits of Productive People',
    },
    description: {
      vi: 'Bài đọc về các thói quen buổi sáng và ảnh hưởng tới năng suất.',
      en: 'A passage on morning routines and how they affect output.',
    },
    topicId: 'tp-life',
    topicName: topicName('tp-life'),
    level: 'ADVANCED',
    wordCount: 186,
    questionCount: 4,
    timeLimitMinutes: 12,
    isCompleted: true,
    lastScore: 6.5,
    passage: [
      'Many highly productive people share a habit that costs nothing: they protect the first hour of their day. Instead of reaching for a phone, they spend that time on a single task chosen the night before.',
      'Researchers at a university in Copenhagen followed 240 office workers for six months. Half of them were asked to plan one priority task each evening. The other half worked as usual. By the end of the study, the planning group reported finishing about thirty percent more meaningful work, even though both groups worked the same number of hours.',
      'The explanation is not willpower. It is the cost of switching. Every time attention moves from one task to another, the brain needs time to rebuild context. A morning spent answering messages can therefore feel busy while producing very little.',
      'Critics point out that the study relied on self-reported data, which tends to be generous. Still, the practical advice survives the criticism: decide the night before, and start before the inbox opens.',
    ],
    questions: buildQuestions('rd-008', [
      {
        text: 'What habit do highly productive people share?',
        options: [
          'Getting up before five',
          '*Protecting the first hour of the day for a single task',
          'Exercising every morning',
          'Answering email as soon as they wake up',
        ],
        explanation:
          'The first paragraph says they protect the first hour for a task chosen the night before.',
      },
      {
        text: 'How many people did the Copenhagen study follow?',
        options: ['120', '*240', '300', '600'],
      },
      {
        text: 'According to the passage, why is switching between tasks inefficient?',
        options: [
          'Because people get tired quickly',
          '*Because the brain needs time to rebuild context each time',
          'Because of weak willpower',
          'Because the work gets broken up',
        ],
        explanation: 'The passage calls this the cost of switching.',
      },
      {
        text: 'Fill in the blank: critics say the study relied on ______ data.',
        answer: 'self-reported',
        explanation: 'The passage uses the phrase self-reported data.',
      },
    ]),
  },
  {
    id: 'rd-014',
    title: { vi: 'Email xác nhận đơn hàng', en: 'Order Confirmation Email' },
    description: {
      vi: 'Bài đọc ngắn dạng email thương mại, hợp với phần 7 của TOEIC.',
      en: 'A short business email in the style of TOEIC Part 7.',
    },
    topicId: 'tp-toeic',
    topicName: topicName('tp-toeic'),
    level: 'INTERMEDIATE',
    wordCount: 98,
    questionCount: 3,
    timeLimitMinutes: 8,
    isCompleted: false,
    passage: [
      'Dear Ms. Tran,',
      'Thank you for your order placed on 3 October. We are pleased to confirm that all twelve items are in stock and will be dispatched from our Hanoi warehouse within two working days.',
      'Your invoice, number INV-20841, is attached to this email. Payment is due within thirty days of the invoice date. If you need a printed copy for your accounting department, simply reply to this message and we will post one to you.',
      'Kind regards, Customer Service Team',
    ],
    questions: buildQuestions('rd-014', [
      {
        text: 'How many items are in the order?',
        options: ['Two', '*Twelve', 'Twenty', 'Thirty'],
      },
      {
        text: 'When is payment due?',
        options: ['Within two days', 'Within ten days', '*Within thirty days', 'Within sixty days'],
      },
      {
        text: 'What should the customer do to get a printed invoice?',
        options: [
          'Call the switchboard',
          '*Reply to this email',
          'Visit the Hanoi warehouse',
          'Fill in a form on the website',
        ],
      },
    ]),
  },
  {
    id: 'rd-003',
    title: {
      vi: 'Lịch sinh hoạt của thư viện thành phố',
      en: 'City Library Opening Hours',
    },
    description: {
      vi: 'Bài đọc cơ bản dạng thông báo, luyện tìm thông tin nhanh.',
      en: 'A simple notice for practising quick information lookup.',
    },
    topicId: 'tp-foundation',
    topicName: topicName('tp-foundation'),
    level: 'BEGINNER',
    wordCount: 74,
    questionCount: 3,
    timeLimitMinutes: 6,
    isCompleted: false,
    passage: [
      'City Library — Opening Hours',
      'Monday to Friday: 8:00 a.m. to 8:00 p.m. Saturday: 9:00 a.m. to 5:00 p.m. The library is closed on Sunday.',
      'The children’s reading club meets every Saturday at 10:00 a.m. in Room B. Membership is free for residents. Please bring proof of address when you register.',
    ],
    questions: buildQuestions('rd-003', [
      {
        text: 'Which day is the library closed?',
        options: ['Saturday', '*Sunday', 'Monday', 'It never closes'],
      },
      {
        text: 'What time does the children’s reading club meet?',
        options: ['8 a.m.', '*10 a.m.', '5 p.m.', '8 p.m.'],
      },
      {
        text: 'Fill in the blank: to register you must bring proof of ______.',
        answer: 'address',
      },
    ]),
  },
  {
    id: 'rd-021',
    title: { vi: 'Xu hướng làm việc từ xa', en: 'The Shift to Remote Work' },
    description: {
      vi: 'Bài đọc học thuật có số liệu, luyện kỹ năng đọc biểu đồ bằng chữ.',
      en: 'An academic passage full of figures, for reading data written as prose.',
    },
    topicId: 'tp-ielts',
    topicName: topicName('tp-ielts'),
    level: 'ADVANCED',
    wordCount: 152,
    questionCount: 3,
    timeLimitMinutes: 15,
    isCompleted: false,
    passage: [
      'Between 2019 and 2023, the proportion of employees working mainly from home rose from eight percent to twenty-six percent across the countries surveyed. The increase was not evenly distributed. In the technology sector the figure reached forty-one percent, while in manufacturing it barely moved.',
      'Employers report mixed results. Some cite substantial savings on office space; others describe a significant drop in informal knowledge sharing, the kind that happens beside a coffee machine rather than in a scheduled meeting.',
      'Employees are clearer. In every country surveyed, a majority said they would accept a smaller pay rise in exchange for keeping two remote days a week. Consequently, flexibility has become a bargaining chip in salary negotiations, a role it did not play five years ago.',
    ],
    questions: buildQuestions('rd-021', [
      {
        text: 'What proportion worked mainly from home in 2023?',
        options: ['8%', '*26%', '41%', '50%'],
      },
      {
        text: 'Which sector had the highest share of remote work?',
        options: ['Manufacturing', 'Education', '*Technology', 'Retail'],
      },
      {
        text: 'What drawback do employers report?',
        options: [
          'Higher office costs',
          '*Less informal knowledge sharing',
          'Staff working fewer hours',
          'Difficulty hiring',
        ],
      },
    ]),
  },
];

export function findReading(id: string): ReadingDetail | undefined {
  return readingLessons.find((l) => l.id === id);
}
