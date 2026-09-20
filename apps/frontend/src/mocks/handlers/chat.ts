/** Mock cho Chat với trợ lý AI (chức năng 9). */

import { del, fail, get, post } from '../router';
import { daysAgo, hoursAgo } from '../db';
import type {
  ChatConversation,
  ChatConversationDetail,
  ChatMessage,
  ChatSuggestionLink,
} from '../../types/chat';

let messageCounter = 100;
const nextId = () => `m-${messageCounter++}`;

const conversations: ChatConversationDetail[] = [
  {
    id: 'cv-1',
    title: 'Phân biệt "make" và "do"',
    preview: 'Bạn dùng "do" cho công việc và nhiệm vụ…',
    updatedAt: hoursAgo(3),
    messageCount: 2,
    messages: [
      {
        id: 'm-1',
        role: 'USER',
        content: 'Khi nào dùng make, khi nào dùng do vậy bạn?',
        createdAt: hoursAgo(3),
      },
      {
        id: 'm-2',
        role: 'ASSISTANT',
        content:
          'Cách nhớ đơn giản: dùng "do" cho công việc, nhiệm vụ và hoạt động chung — do homework, do the dishes, do business. Dùng "make" khi tạo ra thứ gì đó — make a cake, make a decision, make a mistake.\n\nMột số cụm hay gặp cần học thuộc: make an effort, make progress, do a favour, do research.',
        createdAt: hoursAgo(3),
        links: [
          { label: 'Bộ từ Nền tảng — 500 từ đầu tiên', path: '/flashcard/deck-basics', skill: 'VOCABULARY' },
        ],
      },
    ],
  },
  {
    id: 'cv-2',
    title: 'Cách mở bài IELTS Writing Task 2',
    preview: 'Nên diễn đạt lại đề bài bằng từ của mình…',
    updatedAt: daysAgo(2),
    messageCount: 2,
    messages: [
      {
        id: 'm-3',
        role: 'USER',
        content: 'Mở bài Writing Task 2 nên viết thế nào cho an toàn?',
        createdAt: daysAgo(2),
      },
      {
        id: 'm-4',
        role: 'ASSISTANT',
        content:
          'Một mở bài an toàn gồm hai câu. Câu đầu diễn đạt lại đề bài bằng từ ngữ của bạn, tránh chép lại nguyên văn. Câu thứ hai nêu rõ quan điểm và báo trước hướng triển khai.\n\nVí dụ: "It is sometimes argued that online learning will replace traditional classrooms. In my view, while online courses will continue to grow, they are unlikely to replace face-to-face teaching entirely."',
        createdAt: daysAgo(2),
        links: [
          { label: 'Đề viết: Học trực tuyến có thay thế lớp học truyền thống?', path: '/writing/wr-031', skill: 'WRITING' },
        ],
      },
    ],
  },
];

/* -------------------------------------------------------------------
 * Sinh câu trả lời
 * ----------------------------------------------------------------- */

/** Các dấu hiệu cho thấy câu hỏi thuộc phạm vi học tiếng Anh. */
const IN_SCOPE = [
  'english', 'tiếng anh', 'từ vựng', 'vocabulary', 'ngữ pháp', 'grammar',
  'phát âm', 'pronunciation', 'nghe', 'listening', 'đọc', 'reading',
  'viết', 'writing', 'nói', 'speaking', 'ielts', 'toeic', 'thì', 'tense',
  'giới từ', 'preposition', 'động từ', 'verb', 'danh từ', 'noun',
  'tính từ', 'adjective', 'câu điều kiện', 'bị động', 'passive',
  'dịch', 'translate', 'nghĩa', 'meaning', 'flashcard', 'bài học', 'học',
  'luyện', 'sửa lỗi', 'lỗi sai', 'điểm', 'bài kiểm tra', 'exam',
];

function isInScope(text: string): boolean {
  const lower = text.toLowerCase();
  return IN_SCOPE.some((keyword) => lower.includes(keyword));
}

/** Gợi ý bài học bám theo nội dung người dùng hỏi. */
function suggestLinks(text: string): ChatSuggestionLink[] {
  const lower = text.toLowerCase();
  const links: ChatSuggestionLink[] = [];

  if (/nghe|listening/.test(lower)) {
    links.push({ label: 'Bài nghe: Nghe hiểu số liệu và lịch hẹn', path: '/listening/ls-030', skill: 'LISTENING' });
  }
  if (/viết|writing|email/.test(lower)) {
    links.push({ label: 'Đề viết: Email xin nghỉ phép', path: '/writing/wr-014', skill: 'WRITING' });
  }
  if (/đọc|reading/.test(lower)) {
    links.push({ label: 'Bài đọc: Xu hướng làm việc từ xa', path: '/reading/rd-021', skill: 'READING' });
  }
  if (/nói|speaking|phát âm|pronunciation/.test(lower)) {
    links.push({ label: 'Luyện nói: Giới thiệu bản thân', path: '/speaking/sp-003', skill: 'SPEAKING' });
  }
  if (/từ vựng|vocabulary|từ mới|toeic/.test(lower)) {
    links.push({ label: 'Bộ từ TOEIC 600 — Chủ đề Văn phòng', path: '/flashcard/deck-toeic-600', skill: 'VOCABULARY' });
  }

  return links.slice(0, 2);
}

function buildReply(question: string): ChatMessage {
  if (!isInScope(question)) {
    return {
      id: nextId(),
      role: 'ASSISTANT',
      content:
        'Mình chỉ hỗ trợ các câu hỏi liên quan tới việc học tiếng Anh thôi nhé. Bạn có thể hỏi mình về từ vựng, ngữ pháp, phát âm, hoặc nhờ mình gợi ý bài luyện tập phù hợp.',
      createdAt: new Date().toISOString(),
      isRefusal: true,
    };
  }

  const lower = question.toLowerCase();
  let content: string;

  if (/nghe|listening/.test(lower)) {
    content =
      'Để cải thiện kỹ năng nghe, hãy nghe cùng một đoạn ba lần: lần đầu nắm ý chính, lần hai bắt chi tiết như số liệu và tên riêng, lần ba vừa nghe vừa đọc transcript để đối chiếu.\n\nKết quả gần đây của bạn cho thấy phần câu hỏi về thời gian và con số hay mất điểm, nên ưu tiên luyện dạng này trước.';
  } else if (/ngữ pháp|grammar|thì|tense/.test(lower)) {
    content =
      'Bạn nên học ngữ pháp theo tình huống thay vì học rời từng công thức. Ví dụ với các thì quá khứ: quá khứ đơn dùng cho việc đã xong và có mốc thời gian rõ, hiện tại hoàn thành dùng khi việc đó còn liên quan tới hiện tại.\n\nSo sánh: "I lost my keys yesterday" (đã xong) và "I have lost my keys" (giờ vẫn chưa tìm thấy).';
  } else if (/phát âm|pronunciation|nói|speaking/.test(lower)) {
    content =
      'Người Việt thường bỏ âm cuối, nên hãy tập trung vào các âm /s/, /z/, /t/ và /d/ ở cuối từ. Thu âm lại giọng mình rồi nghe đối chiếu với bản gốc là cách nhanh nhất để nhận ra khác biệt.\n\nNgoài ra, hãy nhấn trọng âm vào từ mang nội dung chính thay vì đọc đều đều tất cả các từ.';
  } else if (/viết|writing|email/.test(lower)) {
    content =
      'Với email công việc, giữ ba phần rõ ràng: nêu mục đích ngay câu đầu, trình bày chi tiết ở giữa, và kết bằng một yêu cầu hoặc bước tiếp theo cụ thể.\n\nTránh các cách nói quá thân mật như "some stuff", "okay?" hay "Thanks. Bye." khi viết cho cấp trên hoặc khách hàng.';
  } else if (/từ vựng|vocabulary|từ mới/.test(lower)) {
    content =
      'Học từ vựng theo cụm sẽ nhớ lâu hơn học từ đơn lẻ. Thay vì học riêng "decision", hãy học luôn "make a decision", "a tough decision", "reach a decision".\n\nMỗi từ mới nên tự đặt một câu gắn với công việc hoặc cuộc sống của bạn, vì thông tin có liên hệ cá nhân thì dễ nhớ hơn.';
  } else {
    content =
      'Mình hiểu câu hỏi của bạn. Bạn có thể nói rõ hơn một chút được không — bạn đang gặp khó ở kỹ năng nào, hay muốn mình giải thích một điểm ngữ pháp, một từ cụ thể?\n\nMình cũng có thể gợi ý bài luyện tập dựa trên kết quả học gần đây của bạn.';
  }

  const links = suggestLinks(question);

  return {
    id: nextId(),
    role: 'ASSISTANT',
    content,
    createdAt: new Date().toISOString(),
    links: links.length > 0 ? links : undefined,
  };
}

/* -------------------------------------------------------------------
 * Route
 * ----------------------------------------------------------------- */

function toSummary(c: ChatConversationDetail): ChatConversation {
  const { messages: _m, ...rest } = c;
  return rest;
}

get('/chat/conversations', (): ChatConversation[] =>
  [...conversations]
    .sort((a, b) => b.updatedAt.localeCompare(a.updatedAt))
    .map(toSummary),
);

get('/chat/conversations/:id', ({ params }): ChatConversationDetail => {
  const conversation = conversations.find((c) => c.id === params.id);
  if (!conversation) fail(404, 'errors.conversationNotFound', 'CONVERSATION_NOT_FOUND');
  return conversation;
});

post('/chat/conversations', (): ChatConversationDetail => {
  const conversation: ChatConversationDetail = {
    id: `cv-${Date.now().toString(36)}`,
    title: 'Hội thoại mới',
    preview: '',
    updatedAt: new Date().toISOString(),
    messageCount: 0,
    messages: [],
  };

  conversations.unshift(conversation);
  return conversation;
});

del('/chat/conversations/:id', ({ params }) => {
  const index = conversations.findIndex((c) => c.id === params.id);
  if (index === -1) fail(404, 'errors.conversationNotFound', 'CONVERSATION_NOT_FOUND');
  conversations.splice(index, 1);
  return { deleted: true };
});

/**
 * Gửi một tin nhắn và nhận câu trả lời.
 * Trả về cả hai tin để giao diện chỉ cần nối thêm vào danh sách.
 */
post('/chat/conversations/:id/messages', ({ params, body }) => {
  const conversation = conversations.find((c) => c.id === params.id);
  if (!conversation) fail(404, 'errors.conversationNotFound', 'CONVERSATION_NOT_FOUND');

  const { content } = (body ?? {}) as { content?: string };
  if (!content?.trim()) fail(400, 'errors.emptyMessage', 'VALIDATION');

  const userMessage: ChatMessage = {
    id: nextId(),
    role: 'USER',
    content: content.trim(),
    createdAt: new Date().toISOString(),
  };

  const reply = buildReply(content);

  conversation.messages.push(userMessage, reply);
  conversation.messageCount = conversation.messages.length;
  conversation.updatedAt = reply.createdAt;
  conversation.preview = reply.content.slice(0, 80);

  // Hội thoại mới lấy luôn câu hỏi đầu tiên làm tiêu đề.
  if (conversation.title === 'Hội thoại mới') {
    conversation.title =
      userMessage.content.length > 48
        ? `${userMessage.content.slice(0, 48)}…`
        : userMessage.content;
  }

  return { userMessage, reply };
});

/** Câu hỏi gợi ý hiển thị khi hội thoại còn trống. */
get('/chat/starters', () => [
  'Phân biệt "make" và "do" như thế nào?',
  'Làm sao để cải thiện kỹ năng nghe?',
  'Mở bài IELTS Writing Task 2 nên viết ra sao?',
  'Gợi ý cho mình bài luyện tập phù hợp với trình độ hiện tại',
]);
