import type {
  WritingFeedback,
  WritingPromptDetail,
  WritingSubmission,
} from '../../types/writing';
import { topicName } from './topics';
import { daysAgo } from '../db';

export const writingPrompts: WritingPromptDetail[] = [
  {
    id: 'wr-014',
    title: { vi: 'Viết email xin nghỉ phép', en: 'Write a Leave Request Email' },
    topicId: 'tp-work',
    topicName: topicName('tp-work'),
    level: 'BEGINNER',
    suggestedMinutes: 20,
    minWords: 80,
    status: 'GRADED',
    lastScore: 7.0,
    prompt:
      'Bạn cần nghỉ ba ngày để giải quyết việc gia đình. Viết một email gửi quản lý trực tiếp để xin nghỉ phép. Email cần nêu lý do, thời gian nghỉ cụ thể và phương án bàn giao công việc.',
    hints: [
      'Mở đầu bằng lời chào trang trọng và nêu ngay mục đích.',
      'Nói rõ ngày bắt đầu và ngày kết thúc kỳ nghỉ.',
      'Đề xuất người nhận bàn giao và cách liên lạc khi cần gấp.',
    ],
  },
  {
    id: 'wr-022',
    title: {
      vi: 'Mô tả một biểu đồ về thói quen đọc sách',
      en: 'Describe a Chart on Reading Habits',
    },
    topicId: 'tp-ielts',
    topicName: topicName('tp-ielts'),
    level: 'ADVANCED',
    suggestedMinutes: 20,
    minWords: 150,
    status: 'NOT_STARTED',
    prompt:
      'Biểu đồ cho thấy tỉ lệ người đọc sách in và sách điện tử ở bốn quốc gia trong giai đoạn 2015–2023. Hãy tóm tắt thông tin bằng cách chọn lọc và nêu bật các đặc điểm chính, đồng thời so sánh khi cần thiết.',
    hints: [
      'Viết câu mở đầu diễn đạt lại đề bài bằng từ của bạn.',
      'Nêu hai đến ba xu hướng nổi bật nhất, kèm số liệu cụ thể.',
      'Dùng từ nối chỉ xu hướng: rose, declined, remained stable, fluctuated.',
    ],
  },
  {
    id: 'wr-031',
    title: {
      vi: 'Ý kiến: Học trực tuyến có thay thế được lớp học truyền thống?',
      en: 'Opinion: Can Online Learning Replace the Classroom?',
    },
    topicId: 'tp-life',
    topicName: topicName('tp-life'),
    level: 'INTERMEDIATE',
    suggestedMinutes: 30,
    minWords: 200,
    status: 'NOT_STARTED',
    prompt:
      'Một số người cho rằng học trực tuyến sẽ thay thế hoàn toàn lớp học truyền thống trong mười năm tới. Bạn đồng ý hay không đồng ý? Nêu quan điểm của bạn kèm lý do và ví dụ cụ thể.',
    hints: [
      'Nêu rõ quan điểm ngay trong đoạn mở bài.',
      'Mỗi đoạn thân bài trình bày một lý do, kèm một ví dụ.',
      'Thừa nhận quan điểm đối lập rồi phản biện để bài chặt chẽ hơn.',
    ],
  },
  {
    id: 'wr-008',
    title: {
      vi: 'Giới thiệu bản thân với lớp học mới',
      en: 'Introduce Yourself to a New Class',
    },
    topicId: 'tp-foundation',
    topicName: topicName('tp-foundation'),
    level: 'BEGINNER',
    suggestedMinutes: 15,
    minWords: 60,
    status: 'NOT_STARTED',
    prompt:
      'Bạn vừa tham gia một lớp tiếng Anh mới. Viết một đoạn ngắn giới thiệu bản thân: tên, công việc hoặc ngành học, sở thích và lý do bạn học tiếng Anh.',
    hints: [
      'Dùng thì hiện tại đơn để nói về bản thân.',
      'Viết câu ngắn, mỗi câu một ý.',
      'Kết bằng một câu về mục tiêu học tập của bạn.',
    ],
  },
  {
    id: 'wr-040',
    title: {
      vi: 'Thư phàn nàn về sản phẩm bị lỗi',
      en: 'Complaint Letter About a Faulty Product',
    },
    topicId: 'tp-toeic',
    topicName: topicName('tp-toeic'),
    level: 'INTERMEDIATE',
    suggestedMinutes: 25,
    minWords: 120,
    status: 'NOT_STARTED',
    prompt:
      'Bạn mua một chiếc máy in trực tuyến nhưng hàng nhận được bị lỗi. Viết thư gửi bộ phận chăm sóc khách hàng, mô tả vấn đề và nêu rõ yêu cầu của bạn.',
    hints: [
      'Nêu thông tin đơn hàng ngay đoạn đầu để người đọc tra cứu được.',
      'Mô tả lỗi một cách khách quan, tránh cảm xúc.',
      'Kết thúc bằng yêu cầu cụ thể: đổi hàng, hoàn tiền hay sửa chữa.',
    ],
  },
];

/** Phản hồi AI mẫu cho bài đã chấm. */
const SAMPLE_FEEDBACK: WritingFeedback = {
  overallScore: 7.0,
  grammarScore: 6.5,
  vocabularyScore: 7.0,
  expressionScore: 7.5,
  summary:
    'Email có bố cục rõ ràng và nêu đủ ba nội dung đề bài yêu cầu. Giọng văn phù hợp với môi trường công sở. Điểm cần cải thiện nằm ở thì của động từ và một vài cách diễn đạt còn hơi suồng sã so với thư gửi cấp trên.',
  issues: [
    {
      id: 'is-1',
      category: 'GRAMMAR',
      excerpt: 'I will be absent from 12 March to 14 March because I have some family thing.',
      problem:
        '"some family thing" không đúng ngữ pháp và quá thân mật cho email công việc.',
      suggestion: 'I will be absent from 12 to 14 March due to a family matter.',
    },
    {
      id: 'is-2',
      category: 'GRAMMAR',
      excerpt: 'Yesterday I have talked with Mai about my tasks.',
      problem:
        'Đã có trạng từ chỉ thời gian quá khứ xác định "yesterday" thì dùng quá khứ đơn, không dùng hiện tại hoàn thành.',
      suggestion: 'Yesterday I talked with Mai about my tasks.',
    },
    {
      id: 'is-3',
      category: 'VOCABULARY',
      excerpt: 'Please tell me if it is okay.',
      problem: 'Cách nói hơi thân mật khi viết cho quản lý.',
      suggestion: 'Please let me know if this arrangement is acceptable.',
    },
    {
      id: 'is-4',
      category: 'EXPRESSION',
      excerpt: 'Thanks. Bye.',
      problem: 'Phần kết thư quá cộc lốc so với văn phong email công việc.',
      suggestion: 'Thank you for your consideration. Best regards, Diện.',
    },
  ],
};

/** Lịch sử bài đã nộp. Handler được phép thêm bản ghi mới vào đây. */
export const writingSubmissions: WritingSubmission[] = [
  {
    id: 'ws-001',
    promptId: 'wr-014',
    promptTitle: { vi: 'Viết email xin nghỉ phép', en: 'Write a Leave Request Email' },
    content:
      'Dear Mr. Hung,\n\nI am writing to request leave from 12 March to 14 March because I have some family thing to handle at home.\n\nYesterday I have talked with Mai about my tasks and she agreed to cover the weekly report while I am away. I will also keep my phone on in case something urgent happens.\n\nPlease tell me if it is okay.\n\nThanks. Bye.',
    wordCount: 82,
    status: 'GRADED',
    submittedAt: daysAgo(2),
    feedback: SAMPLE_FEEDBACK,
  },
];

export function findWritingPrompt(id: string): WritingPromptDetail | undefined {
  return writingPrompts.find((p) => p.id === id);
}

/**
 * Sinh phản hồi cho một bài mới nộp.
 *
 * Điểm được tính thô theo độ dài và độ đa dạng từ vựng để kết quả thay đổi
 * theo bài viết thật, thay vì lúc nào cũng trả về một con số cố định.
 */
export function generateFeedback(content: string, minWords: number): WritingFeedback {
  const words = content.trim().split(/\s+/).filter(Boolean);
  const unique = new Set(words.map((w) => w.toLowerCase().replace(/[^a-z']/g, '')));

  const lengthRatio = Math.min(1, words.length / Math.max(1, minWords));
  const variety = Math.min(1, unique.size / Math.max(1, words.length * 0.6));

  const base = 4.5 + lengthRatio * 3 + variety * 2;
  const clamp = (v: number) => Math.max(1, Math.min(10, Math.round(v * 10) / 10));

  return {
    overallScore: clamp(base),
    grammarScore: clamp(base - 0.5),
    vocabularyScore: clamp(base + variety * 0.5),
    expressionScore: clamp(base + 0.5),
    summary:
      words.length < minWords
        ? `Bài viết mới có ${words.length} từ, chưa đạt mức tối thiểu ${minWords} từ. Hãy triển khai thêm ý và ví dụ cụ thể để lập luận thuyết phục hơn.`
        : 'Bài viết đủ độ dài và bám sát đề bài. Cần chú ý thì của động từ và chọn từ trang trọng hơn ở một vài chỗ.',
    issues: SAMPLE_FEEDBACK.issues.slice(0, words.length < minWords ? 2 : 4),
  };
}
