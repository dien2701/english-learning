import type { ListeningDetail } from '../../types/practice';
import { buildQuestions } from './questions';
import { topicName } from './topics';

/**
 * Bài nghe mẫu.
 *
 * Câu hỏi và phương án luôn bằng tiếng Anh ở cả hai chế độ ngôn ngữ —
 * đây là phần bài tập, không phải nhãn giao diện. Chỉ tiêu đề và mô tả
 * mới có hai bản dịch.
 *
 * Bản chạy thử chưa có tệp mp3 nên `audioUrl` để trống và trình phát đọc
 * transcript bằng giọng của trình duyệt. Khi nối backend thật, chỉ cần
 * điền đường dẫn Cloudinary vào đây.
 */
export const listeningLessons: ListeningDetail[] = [
  {
    id: 'ls-021',
    title: {
      vi: 'Hội thoại nơi công sở — Part 3',
      en: 'Workplace Conversation — Part 3',
    },
    description: {
      vi: 'Hai đồng nghiệp trao đổi về lịch họp và tài liệu cần chuẩn bị.',
      en: 'Two colleagues rearrange a meeting and agree who prepares what.',
    },
    topicId: 'tp-work',
    topicName: topicName('tp-work'),
    level: 'INTERMEDIATE',
    durationSeconds: 95,
    questionCount: 4,
    isCompleted: true,
    lastScore: 8.5,
    audioUrl: '',
    questions: buildQuestions('ls-021', [
      {
        text: 'What day is the meeting moved to?',
        options: ['Monday', '*Wednesday', 'Thursday', 'Friday'],
        explanation: 'The woman says "let us move it to Wednesday morning".',
      },
      {
        text: 'Which document does the man need to prepare?',
        options: [
          'The client contract',
          '*The quarterly sales report',
          'The timesheet',
          'The hiring plan',
        ],
        explanation: 'He mentions "the quarterly sales report".',
      },
      {
        text: 'What time does the meeting start?',
        options: ['Eight o’clock', '*Half past nine', 'Ten o’clock', 'Eleven o’clock'],
      },
      {
        text: 'Fill in the blank: "I will send you the ______ before noon."',
        answer: 'agenda',
        explanation: 'She promises to send the meeting agenda before lunch.',
      },
    ]),
  },
  {
    id: 'ls-030',
    title: {
      vi: 'Nghe hiểu số liệu và lịch hẹn',
      en: 'Understanding Numbers and Appointments',
    },
    description: {
      vi: 'Luyện nghe con số, ngày giờ và cách xác nhận lại thông tin.',
      en: 'Practise catching figures, dates and the phrases used to confirm them.',
    },
    topicId: 'tp-toeic',
    topicName: topicName('tp-toeic'),
    level: 'INTERMEDIATE',
    durationSeconds: 78,
    questionCount: 3,
    isCompleted: false,
    audioUrl: '',
    questions: buildQuestions('ls-030', [
      {
        text: 'How many units does the customer order?',
        options: ['15', '*50', '55', '150'],
        explanation: 'The caller spells it out: "five zero, fifty units".',
      },
      {
        text: 'When will the goods be delivered?',
        options: ['*12 March', '2 March', '20 March', '12 May'],
      },
      {
        text: 'Fill in the missing digit of the phone number: "0___ 555 018".',
        answer: '9',
      },
    ]),
  },
  {
    id: 'ls-012',
    title: {
      vi: 'Gọi món tại nhà hàng',
      en: 'Ordering at a Restaurant',
    },
    description: {
      vi: 'Hội thoại ngắn giữa khách và nhân viên phục vụ.',
      en: 'A short exchange between a diner and a waiter.',
    },
    topicId: 'tp-life',
    topicName: topicName('tp-life'),
    level: 'BEGINNER',
    durationSeconds: 62,
    questionCount: 3,
    isCompleted: false,
    audioUrl: '',
    questions: buildQuestions('ls-012', [
      {
        text: 'What does the customer order as a main course?',
        options: ['Pasta', '*Grilled salmon', 'Steak', 'Salad'],
      },
      {
        text: 'What special request does the customer make?',
        options: ['*No onions', 'Extra cheese', 'Less salt', 'To take away'],
      },
      {
        text: 'What does the customer drink?',
        options: ['Coffee', 'Beer', '*Still water', 'Orange juice'],
      },
    ]),
  },
  {
    id: 'ls-045',
    title: {
      vi: 'Bài giảng ngắn về biến đổi khí hậu',
      en: 'Short Lecture on Climate Change',
    },
    description: {
      vi: 'Bài nói học thuật, tốc độ nhanh, nhiều thuật ngữ chuyên ngành.',
      en: 'An academic talk delivered at speed, with subject-specific terms.',
    },
    topicId: 'tp-ielts',
    topicName: topicName('tp-ielts'),
    level: 'ADVANCED',
    durationSeconds: 140,
    questionCount: 3,
    isCompleted: false,
    audioUrl: '',
    questions: buildQuestions('ls-045', [
      {
        text: 'What does the speaker name as the main cause?',
        options: [
          'Deforestation',
          '*Emissions from fossil fuels',
          'Plastic waste',
          'Livestock farming',
        ],
      },
      {
        text: 'Which solution does the speaker put first?',
        options: [
          '*Shifting to renewable energy',
          'Taxing carbon',
          'Planting more trees',
          'Eating less meat',
        ],
      },
      {
        text: 'Fill in the blank: "We need to cut emissions by ______ percent."',
        answer: 'forty',
      },
    ]),
  },
];

/** Lời thoại, chỉ được trả về sau khi người dùng đã nộp bài. */
export const listeningTranscripts: Record<string, string> = {
  'ls-021':
    'A: Hi Mark, are we still meeting on Monday? ' +
    'B: Actually, something came up. Let us move it to Wednesday morning, around nine thirty. ' +
    'A: That works for me. Should I bring anything? ' +
    'B: Yes, please prepare the quarterly sales report. ' +
    'A: Sure. I will send you the agenda before noon.',
  'ls-030':
    'A: Good morning, I would like to confirm our order. ' +
    'B: Of course. How many units do you need? ' +
    'A: Five zero, fifty units. ' +
    'B: Fifty units, noted. Delivery is scheduled for March twelfth. ' +
    'A: Perfect. You can reach me at zero nine, five five five, zero one eight.',
  'ls-012':
    'A: Good evening. Are you ready to order? ' +
    'B: Yes. I will have the grilled salmon, please. No onions. ' +
    'A: Certainly. And to drink? ' +
    'B: Just still water, thank you.',
  'ls-045':
    'Today we examine the main driver of global warming. ' +
    'Emissions from fossil fuels account for the largest share of greenhouse gases. ' +
    'The first and most urgent step is shifting to renewable energy. ' +
    'Scientists agree we need to cut emissions by forty percent within the decade.',
};

export function findListening(id: string): ListeningDetail | undefined {
  return listeningLessons.find((l) => l.id === id);
}
