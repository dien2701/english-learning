import type { SpeakingDetail, SpeakingResult } from '../../types/speaking';
import { topicName } from './topics';

export const speakingLessons: SpeakingDetail[] = [
  {
    id: 'sp-003',
    title: { vi: 'Giới thiệu bản thân', en: 'Introducing Yourself' },
    description: {
      vi: 'Đọc to năm câu giới thiệu cơ bản, luyện trọng âm và ngữ điệu.',
      en: 'Read five simple lines aloud to practise stress and intonation.',
    },
    topicId: 'tp-foundation',
    topicName: topicName('tp-foundation'),
    level: 'BEGINNER',
    promptCount: 5,
    isCompleted: true,
    lastScore: 6.8,
    prompts: [
      {
        id: 'sp-003-p1',
        order: 1,
        text: 'Hello, my name is Linh and I work as a software engineer.',
        phonetic: '/həˈloʊ maɪ neɪm ɪz lɪŋ/',
        meaning: 'Xin chào, tôi tên Linh và tôi làm kỹ sư phần mềm.',
      },
      {
        id: 'sp-003-p2',
        order: 2,
        text: 'I have been learning English for about three years.',
        phonetic: null,
        meaning: 'Tôi đã học tiếng Anh được khoảng ba năm.',
      },
      {
        id: 'sp-003-p3',
        order: 3,
        text: 'In my free time, I enjoy reading and cycling around the lake.',
        phonetic: null,
        meaning: 'Lúc rảnh tôi thích đọc sách và đạp xe quanh hồ.',
      },
      {
        id: 'sp-003-p4',
        order: 4,
        text: 'My goal is to communicate confidently with international colleagues.',
        phonetic: null,
        meaning: 'Mục tiêu của tôi là giao tiếp tự tin với đồng nghiệp nước ngoài.',
      },
      {
        id: 'sp-003-p5',
        order: 5,
        text: 'Thank you for listening, and I look forward to working with you.',
        phonetic: null,
        meaning: 'Cảm ơn đã lắng nghe, tôi mong được làm việc cùng bạn.',
      },
    ],
  },
  {
    id: 'sp-010',
    title: { vi: 'Mô tả một địa điểm bạn yêu thích', en: 'Describe a Place You Love' },
    description: {
      vi: 'Luyện nói theo chủ đề Speaking Part 2 với các câu dài hơn.',
      en: 'Longer sentences in the style of Speaking Part 2.',
    },
    topicId: 'tp-ielts',
    topicName: topicName('tp-ielts'),
    level: 'INTERMEDIATE',
    promptCount: 4,
    isCompleted: false,
    prompts: [
      {
        id: 'sp-010-p1',
        order: 1,
        text: 'The place I would like to talk about is a small bookshop near my house.',
        phonetic: null,
        meaning: 'Nơi tôi muốn nói tới là một hiệu sách nhỏ gần nhà tôi.',
      },
      {
        id: 'sp-010-p2',
        order: 2,
        text: 'It occupies the ground floor of an old building with wooden shutters.',
        phonetic: null,
        meaning: 'Nó nằm ở tầng trệt một toà nhà cũ có cửa chớp gỗ.',
      },
      {
        id: 'sp-010-p3',
        order: 3,
        text: 'What makes it special is the quiet atmosphere and the smell of paper.',
        phonetic: null,
        meaning: 'Điều đặc biệt là bầu không khí yên tĩnh và mùi giấy.',
      },
      {
        id: 'sp-010-p4',
        order: 4,
        text: 'I usually spend an entire Sunday morning browsing the shelves there.',
        phonetic: null,
        meaning: 'Tôi thường dành trọn sáng Chủ nhật để lục lọi các kệ sách ở đó.',
      },
    ],
  },
  {
    id: 'sp-018',
    title: { vi: 'Trình bày ý kiến trong cuộc họp', en: 'Giving an Opinion in a Meeting' },
    description: {
      vi: 'Câu dài, nhiều cụm từ trang trọng dùng nơi công sở.',
      en: 'Long sentences with the formal phrases used at work.',
    },
    topicId: 'tp-work',
    topicName: topicName('tp-work'),
    level: 'ADVANCED',
    promptCount: 4,
    isCompleted: false,
    prompts: [
      {
        id: 'sp-018-p1',
        order: 1,
        text: 'If I may, I would like to raise a concern about the current timeline.',
        phonetic: null,
        meaning: 'Nếu được, tôi muốn nêu một lo ngại về tiến độ hiện tại.',
      },
      {
        id: 'sp-018-p2',
        order: 2,
        text: 'Based on the figures we reviewed, the deadline seems unrealistic.',
        phonetic: null,
        meaning: 'Dựa trên số liệu vừa xem, hạn chót có vẻ không khả thi.',
      },
      {
        id: 'sp-018-p3',
        order: 3,
        text: 'I would suggest postponing the launch by two weeks.',
        phonetic: null,
        meaning: 'Tôi đề xuất lùi ngày ra mắt thêm hai tuần.',
      },
      {
        id: 'sp-018-p4',
        order: 4,
        text: 'That said, I am happy to be persuaded if the team disagrees.',
        phonetic: null,
        meaning: 'Dù vậy, tôi sẵn sàng nghe nếu cả nhóm không đồng tình.',
      },
    ],
  },
];

export function findSpeaking(id: string): SpeakingDetail | undefined {
  return speakingLessons.find((l) => l.id === id);
}

/**
 * Sinh kết quả chấm nói.
 *
 * Bản chạy thử không phân tích âm thanh thật. Điểm được suy ra từ số câu
 * người học đã thu và độ dài bản ghi, đủ để kết quả thay đổi theo hành vi
 * thay vì luôn cố định.
 */
export function generateSpeakingResult(
  lesson: SpeakingDetail,
  recordedPromptIds: string[],
  totalDurationSeconds: number,
): Omit<SpeakingResult, 'attemptId' | 'submittedAt'> {
  const coverage = recordedPromptIds.length / Math.max(1, lesson.prompts.length);

  // Người đọc quá nhanh hoặc quá chậm đều bị trừ điểm trôi chảy.
  const expectedSeconds = lesson.prompts.length * 6;
  const pace = totalDurationSeconds / Math.max(1, expectedSeconds);
  const pacePenalty = Math.min(1.5, Math.abs(1 - pace) * 2);

  const clamp = (v: number) => Math.max(1, Math.min(10, Math.round(v * 10) / 10));
  const base = 4 + coverage * 4;

  const scores = {
    pronunciation: clamp(base + 0.4),
    vocabulary: clamp(base + 0.8),
    grammar: clamp(base + 0.6),
    fluency: clamp(base - pacePenalty + 0.5),
    relevance: clamp(base + coverage),
  };

  const overallScore = clamp(
    (scores.pronunciation +
      scores.vocabulary +
      scores.grammar +
      scores.fluency +
      scores.relevance) /
      5,
  );

  const improvements: string[] = [];
  if (coverage < 1) {
    improvements.push(
      `Bạn mới thu ${recordedPromptIds.length}/${lesson.prompts.length} câu. Hoàn thành hết để có đánh giá chính xác hơn.`,
    );
  }
  if (pace > 1.3) improvements.push('Bạn đọc hơi chậm, thử giữ nhịp đều và liền mạch hơn.');
  if (pace < 0.7) improvements.push('Bạn đọc khá nhanh, hãy ngắt nghỉ rõ ở dấu phẩy và dấu chấm.');
  improvements.push('Chú ý âm cuối /s/ và /t/ — đây là lỗi thường gặp với người Việt.');
  improvements.push('Nhấn trọng âm vào từ mang nội dung chính thay vì đọc đều đều.');

  const promptFeedback = lesson.prompts
    .filter((p) => recordedPromptIds.includes(p.id))
    .map((p, index) => ({
      promptId: p.id,
      text: p.text,
      score: clamp(base + (index % 3) * 0.4),
      mispronounced: pickTrickyWords(p.text),
      comment:
        index % 2 === 0
          ? 'Ngữ điệu tự nhiên, cần rõ âm cuối hơn.'
          : 'Phát âm rõ ràng, thử nối âm giữa các từ cho trôi chảy.',
    }));

  return { lessonId: lesson.id, lessonTitle: lesson.title, overallScore, scores, improvements, promptFeedback };
}

/** Chọn vài từ khó trong câu để minh hoạ phần phát âm cần chú ý. */
function pickTrickyWords(text: string): string[] {
  const tricky = [
    'three',
    'thank',
    'months',
    'colleagues',
    'confidently',
    'atmosphere',
    'shutters',
    'unrealistic',
    'postponing',
    'persuaded',
    'engineer',
    'browsing',
  ];

  const words = text.toLowerCase().replace(/[^a-z\s]/g, '').split(/\s+/);
  return tricky.filter((t) => words.includes(t)).slice(0, 3);
}
