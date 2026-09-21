/** Mock cho phân hệ Quản trị hệ thống (chức năng 10). */

import { del, fail, get, matches, num, paginate, patch, post, put } from '../router';
import { accounts, daysAgo, hoursAgo, minutesAgo } from '../db';
import { decks } from '../data/flashcards';
import { listeningLessons } from '../data/listening';
import { readingLessons } from '../data/reading';
import { writingPrompts } from '../data/writing';
import { speakingLessons } from '../data/speaking';
import { exams } from '../data/exam';
import { topics } from '../data/topics';
import type {
  AdminContentChild,
  AdminContentItem,
  AdminContentPayload,
  AdminDashboardData,
  AdminNotification,
  AdminUser,
  AudienceKey,
  ContentStatus,
} from '../../types/admin';
import type { AccountStatus, Skill } from '../../types/common';
import type { L10n } from '../../types/l10n';

/* -------------------------------------------------------------------
 * Bảng điều khiển
 * ----------------------------------------------------------------- */

get('/admin/dashboard', (): AdminDashboardData => {
  const totalContent =
    decks.length +
    listeningLessons.length +
    readingLessons.length +
    writingPrompts.length +
    speakingLessons.length +
    exams.length;

  return {
    overview: {
      totalUsers: 1284,
      activeUsers: 617,
      studySessions: 8342,
      totalContent,
    },
    signups: [
      { label: { vi: 'T4', en: 'Apr' }, count: 42 },
      { label: { vi: 'T5', en: 'May' }, count: 58 },
      { label: { vi: 'T6', en: 'Jun' }, count: 51 },
      { label: { vi: 'T7', en: 'Jul' }, count: 76 },
      { label: { vi: 'T8', en: 'Aug' }, count: 94 },
      { label: { vi: 'T9', en: 'Sep' }, count: 88 },
    ],
    contentCounts: [
      { skill: 'VOCABULARY', count: decks.length },
      { skill: 'LISTENING', count: listeningLessons.length },
      { skill: 'READING', count: readingLessons.length },
      { skill: 'WRITING', count: writingPrompts.length },
      { skill: 'SPEAKING', count: speakingLessons.length },
      { skill: 'EXAM', count: exams.length },
    ],
    activities: [
      {
        id: 'aa-1',
        actor: 'ADMIN',
        action: {
          vi: 'Thêm bộ từ "Du lịch — Sân bay và khách sạn"',
          en: 'Added the deck "Travel — Airports and hotels"',
        },
        occurredAt: minutesAgo(25),
      },
      {
        id: 'aa-2',
        actor: 'SYSTEM',
        action: {
          vi: 'Gửi 617 thông báo nhắc ôn tập hằng ngày',
          en: 'Sent 617 daily review reminders',
        },
        occurredAt: hoursAgo(6),
      },
      {
        id: 'aa-3',
        actor: 'ADMIN',
        action: {
          vi: 'Khoá tài khoản khoa@enlearning.vn',
          en: 'Locked the account khoa@enlearning.vn',
        },
        occurredAt: hoursAgo(20),
      },
      {
        id: 'aa-4',
        actor: 'ADMIN',
        action: {
          vi: 'Cập nhật bài nghe "Hội thoại nơi công sở — Part 3"',
          en: 'Updated the listening lesson "Workplace conversations — Part 3"',
        },
        occurredAt: daysAgo(2),
      },
      {
        id: 'aa-5',
        actor: 'SYSTEM',
        action: {
          vi: 'Sao lưu dữ liệu định kỳ hoàn tất',
          en: 'Scheduled data backup completed',
        },
        occurredAt: daysAgo(3),
      },
    ],
  };
});

/* -------------------------------------------------------------------
 * Quản lý người dùng
 * ----------------------------------------------------------------- */

/** Số liệu học tập kèm theo, chỉ để bảng quản lý có thêm thông tin. */
const userStats: Record<string, { completed: number; lastActive: string }> = {
  'u-001': { completed: 37, lastActive: hoursAgo(5) },
  'u-002': { completed: 0, lastActive: hoursAgo(1) },
  'u-003': { completed: 12, lastActive: daysAgo(21) },
};

function toAdminUser(id: string): AdminUser {
  const account = accounts.find((a) => a.id === id)!;
  const stats = userStats[id] ?? { completed: 0, lastActive: daysAgo(1) };

  return {
    id: account.id,
    fullName: account.fullName,
    email: account.email,
    role: account.role,
    status: account.status,
    createdAt: account.createdAt,
    lastActiveAt: stats.lastActive,
    completedLessons: stats.completed,
  };
}

get('/admin/users', ({ query }) => {
  const search = query.get('search') ?? '';
  const role = query.get('role') ?? '';
  const status = query.get('status') ?? '';

  const filtered = accounts.filter((account) => {
    if (role && account.role !== role) return false;
    if (status && account.status !== status) return false;
    if (search && !matches(`${account.fullName} ${account.email}`, search)) return false;
    return true;
  });

  return paginate(
    filtered.map((a) => toAdminUser(a.id)),
    num(query, 'page', 1),
    num(query, 'pageSize', 10),
  );
});

get('/admin/users/:id', ({ params }): AdminUser => {
  const account = accounts.find((a) => a.id === params.id);
  if (!account) fail(404, 'errors.userNotFound', 'USER_NOT_FOUND');
  return toAdminUser(account.id);
});

/** Đổi trạng thái tài khoản hoặc phân quyền cơ bản. */
patch('/admin/users/:id', ({ params, body }): AdminUser => {
  const account = accounts.find((a) => a.id === params.id);
  if (!account) fail(404, 'errors.userNotFound', 'USER_NOT_FOUND');

  const { status, role } = (body ?? {}) as {
    status?: AccountStatus;
    role?: 'USER' | 'ADMIN';
  };

  if (status) account.status = status;
  if (role) account.role = role;

  return toAdminUser(account.id);
});

/* -------------------------------------------------------------------
 * Quản lý nội dung
 * ----------------------------------------------------------------- */

/** Trạng thái hoạt động do quản trị viên đặt, lưu riêng theo id nội dung. */
const contentStatus = new Map<string, ContentStatus>();

/** Nội dung đã xuất hiện trong lịch sử học thì chỉ được ngừng, không được xoá. */
const CONTENT_IN_USE = new Set([
  'deck-toeic-600',
  'deck-ielts-food',
  'ls-021',
  'rd-008',
  'wr-014',
  'sp-003',
  'ex-005',
]);

/** Các nội dung đã bị quản trị viên xoá trong phiên hiện tại. */
const deletedContent = new Set<string>();

function buildContentList(): AdminContentItem[] {
  const items: AdminContentItem[] = [];

  const push = (
    id: string,
    title: L10n,
    skill: Skill,
    topicName: L10n,
    level: string,
    itemCount: number,
    updatedAt: string,
  ) => {
    if (deletedContent.has(id)) return;
    items.push({
      id,
      title,
      skill,
      topicName,
      level,
      itemCount,
      updatedAt,
      status: contentStatus.get(id) ?? 'ACTIVE',
      inUse: CONTENT_IN_USE.has(id),
    });
  };

  decks.forEach((d, i) =>
    push(d.id, d.title, 'VOCABULARY', d.topicName, d.level, d.totalCards, daysAgo(i + 1)),
  );
  listeningLessons.forEach((l, i) =>
    push(l.id, l.title, 'LISTENING', l.topicName, l.level, l.questionCount, daysAgo(i + 2)),
  );
  readingLessons.forEach((l, i) =>
    push(l.id, l.title, 'READING', l.topicName, l.level, l.questionCount, daysAgo(i + 3)),
  );
  writingPrompts.forEach((p, i) =>
    push(p.id, p.title, 'WRITING', p.topicName, p.level, 1, daysAgo(i + 4)),
  );
  speakingLessons.forEach((l, i) =>
    push(l.id, l.title, 'SPEAKING', l.topicName, l.level, l.promptCount, daysAgo(i + 5)),
  );
  exams.forEach((e, i) =>
    push(
      e.id,
      e.title,
      'EXAM',
      { vi: 'Tổng hợp', en: 'Mixed' },
      e.level,
      e.questionCount,
      daysAgo(i + 6),
    ),
  );

  return items;
}

get('/admin/content', ({ query }) => {
  const search = query.get('search') ?? '';
  const skill = query.get('skill') ?? '';
  const status = query.get('status') ?? '';

  const filtered = buildContentList().filter((item) => {
    if (skill && item.skill !== skill) return false;
    if (status && item.status !== status) return false;
    if (search && !matches(item.title, search)) return false;
    return true;
  });

  return paginate(filtered, num(query, 'page', 1), num(query, 'pageSize', 10));
});

get('/admin/content/:id', ({ params }) => {
  const allItems = [
    ...decks.map(d => ({ ...d, skill: 'VOCABULARY' })),
    ...listeningLessons.map(d => ({ ...d, skill: 'LISTENING' })),
    ...readingLessons.map(d => ({ ...d, skill: 'READING' })),
    ...writingPrompts.map(d => ({ ...d, skill: 'WRITING' })),
    ...speakingLessons.map(d => ({ ...d, skill: 'SPEAKING' })),
    ...exams.map(d => ({ ...d, skill: 'EXAM' })),
  ];
  const item = allItems.find(i => i.id === params.id);
  if (!item) fail(404, 'errors.contentNotFound', 'CONTENT_NOT_FOUND');
  
  // Fake payload structure cho edit form
  const loose = item as unknown as Record<string, unknown>;
  const children = (key: string) => (loose[key] as AdminContentChild[] | undefined) || [];
  const payload = {
    skill: item.skill,
    title: item.title,
    topicName: loose.topicName as L10n | undefined,
    level: item.level,
    items: [] as AdminContentChild[], // Trong thực tế lấy từ db
  };

  if (item.skill === 'VOCABULARY') payload.items = children('cards');
  if (item.skill === 'LISTENING') payload.items = children('parts');
  if (item.skill === 'READING') payload.items = children('questions');
  if (item.skill === 'WRITING') payload.items = [loose.prompt as AdminContentChild]; // Ví dụ
  if (item.skill === 'SPEAKING') payload.items = children('prompts');
  if (item.skill === 'EXAM') payload.items = children('questions');

  return {
    id: item.id,
    title: item.title,
    skill: item.skill,
    level: item.level,
    status: contentStatus.get(item.id) ?? 'ACTIVE',
    payload
  };
});

post('/admin/content', ({ body }) => {
  const { skill, title, topicName, level, items } = (body ?? {}) as Partial<AdminContentPayload>;
  if (!title?.vi || !skill) fail(400, 'errors.invalidPayload', 'VALIDATION');
  
  const newId = `new-${skill.toLowerCase()}-${Date.now().toString(36)}`;
  
  // Fake push vào list tương ứng
  const newContent = {
    id: newId,
    title,
    topicName: topicName || { vi: 'Chưa có', en: 'None' },
    level: level || 'BEGINNER',
    totalCards: items?.length || 0,
    questionCount: items?.length || 0,
    promptCount: items?.length || 0,
  };

  if (skill === 'VOCABULARY') (decks as unknown[]).unshift(newContent);
  if (skill === 'LISTENING') (listeningLessons as unknown[]).unshift(newContent);
  if (skill === 'READING') (readingLessons as unknown[]).unshift(newContent);
  if (skill === 'WRITING') (writingPrompts as unknown[]).unshift(newContent);
  if (skill === 'SPEAKING') (speakingLessons as unknown[]).unshift(newContent);
  if (skill === 'EXAM') (exams as unknown[]).unshift(newContent);

  contentStatus.set(newId, 'ACTIVE');

  return {
    id: newId,
    title,
    skill,
    level,
    status: 'ACTIVE',
    itemCount: items?.length || 0,
    updatedAt: new Date().toISOString()
  };
});

put('/admin/content/:id', ({ params, body }) => {
  const { skill, title, level, items } = (body ?? {}) as Partial<AdminContentPayload>;
  
  return {
    id: params.id,
    title,
    skill,
    level,
    status: contentStatus.get(params.id) ?? 'ACTIVE',
    itemCount: items?.length || 0,
    updatedAt: new Date().toISOString()
  };
});

/** Bật hoặc tắt trạng thái hoạt động của một nội dung. */
patch('/admin/content/:id', ({ params, body }) => {
  const { status } = (body ?? {}) as { status?: ContentStatus };
  if (status !== 'ACTIVE' && status !== 'INACTIVE') {
    fail(400, 'errors.invalidStatus', 'VALIDATION');
  }

  contentStatus.set(params.id, status);
  return { id: params.id, status };
});

/**
 * Xoá nội dung.
 *
 * Nội dung đang nằm trong lịch sử học của người dùng thì bị từ chối —
 * quản trị viên phải chuyển sang ngừng hoạt động, đúng như quy định
 * trong mô tả chức năng 10.
 */
del('/admin/content/:id', ({ params }) => {
  if (CONTENT_IN_USE.has(params.id)) {
    fail(
      409,
      'errors.contentInUse',
      'CONTENT_IN_USE',
    );
  }

  deletedContent.add(params.id);
  return { deleted: true };
});

/* -------------------------------------------------------------------
 * Quản lý chủ đề
 * ----------------------------------------------------------------- */

get('/admin/topics', () =>
  topics.map((t) => ({
    ...t,
    itemCount: buildContentList().filter((c) => c.topicName.vi === t.name.vi).length,
  })),
);

post('/admin/topics', ({ body }) => {
  const { name } = (body ?? {}) as { name?: string };
  if (!name?.trim()) {
    fail(400, 'errors.topicNameEmpty', 'VALIDATION', { name: 'errors.field.topicNameRequired' });
  }

  const trimmed = name.trim();
  if (topics.some((t) => t.name.vi.toLowerCase() === trimmed.toLowerCase())) {
    fail(409, 'errors.topicExists', 'TOPIC_EXISTS', { name: 'errors.topicExists' });
  }

  // Chủ đề tạo từ trang quản trị chỉ có một tên, dùng chung cho cả hai ngôn ngữ.
  const topic = { id: `tp-${Date.now().toString(36)}`, name: { vi: trimmed, en: trimmed } };
  topics.push(topic);
  return topic;
});

put('/admin/topics/:id', ({ params, body }) => {
  const topic = topics.find((t) => t.id === params.id);
  if (!topic) fail(404, 'errors.topicNotFound', 'TOPIC_NOT_FOUND');

  const { name } = (body ?? {}) as { name?: string };
  if (!name?.trim()) {
    fail(400, 'errors.topicNameEmpty', 'VALIDATION', { name: 'errors.field.topicNameRequired' });
  }

  topic.name = { vi: name.trim(), en: name.trim() };
  return topic;
});

del('/admin/topics/:id', ({ params }) => {
  const topicIndex = topics.findIndex((t) => t.id === params.id);
  if (topicIndex === -1) fail(404, 'errors.topicNotFound', 'TOPIC_NOT_FOUND');

  const topic = topics[topicIndex];
  
  // Kiểm tra xem có nội dung nào dùng topic này đang inUse (có trong lịch sử học) không
  const allContent = buildContentList();
  const contentsUnderTopic = allContent.filter((c) => c.topicName.vi === topic.name.vi);
  const inUse = contentsUnderTopic.some((c) => c.inUse);
  
  if (inUse) {
    fail(409, 'errors.topicInUse', 'TOPIC_IN_USE');
  }

  // Xóa chủ đề
  topics.splice(topicIndex, 1);
  
  // Xóa liên kết (cập nhật các nội dung đang dùng topic này về 'Khác')
  // Do mock dùng các array tách rời nên ta cần loop qua để gán lại. 
  const reassignTopic = (items: unknown[]) => {
    (items as { topicId?: string; topicName?: L10n }[]).forEach(item => {
      if (item.topicId === params.id || (item.topicName && item.topicName.vi === topic.name.vi)) {
        item.topicId = undefined; // Hoặc 'tp-other' nếu có
        item.topicName = { vi: 'Khác', en: 'Other' };
      }
    });
  };

  reassignTopic(decks);
  reassignTopic(listeningLessons);
  reassignTopic(readingLessons);
  reassignTopic(writingPrompts);
  reassignTopic(speakingLessons);
  reassignTopic(exams);

  return { deleted: true };
});

/* -------------------------------------------------------------------
 * Quản lý thông báo
 * ----------------------------------------------------------------- */

const notifications: AdminNotification[] = [
  {
    id: 'an-1',
    title: 'Nhắc ôn tập hằng ngày',
    content: 'Bạn có từ vựng đến hạn ôn tập hôm nay. Dành 10 phút để giữ chuỗi học nhé!',
    audience: 'ACTIVE',
    status: 'SENT',
    recipientCount: 617,
    createdAt: daysAgo(1),
    sentAt: hoursAgo(6),
  },
  {
    id: 'an-2',
    title: 'Bộ đề TOEIC mới đã sẵn sàng',
    content: 'Ba bộ đề mô phỏng TOEIC vừa được thêm vào kho nội dung. Vào thử ngay.',
    audience: 'ALL',
    status: 'SENT',
    recipientCount: 1284,
    createdAt: daysAgo(6),
    sentAt: daysAgo(5),
  },
  {
    id: 'an-3',
    title: 'Bảo trì hệ thống cuối tuần',
    content: 'Hệ thống sẽ tạm ngừng từ 2 giờ tới 4 giờ sáng Chủ nhật để nâng cấp.',
    audience: 'ALL',
    status: 'DRAFT',
    createdAt: hoursAgo(3),
  },
];

const AUDIENCE_SIZE: Record<AudienceKey, number> = {
  ALL: 1284,
  ACTIVE: 617,
  INACTIVE: 412,
  ADMIN: 4,
};

get('/admin/notifications', ({ query }) =>
  paginate(notifications, num(query, 'page', 1), num(query, 'pageSize', 10)),
);

post('/admin/notifications', ({ body }): AdminNotification => {
  const { title, content, audience, send } = (body ?? {}) as {
    title?: string;
    content?: string;
    audience?: AudienceKey;
    send?: boolean;
  };

  const fieldErrors: Record<string, string> = {};
  if (!title?.trim()) fieldErrors.title = 'Vui lòng nhập tiêu đề.';
  if (!content?.trim()) fieldErrors.content = 'Vui lòng nhập nội dung.';
  if (!audience) fieldErrors.audience = 'Vui lòng chọn nhóm người nhận.';

  if (Object.keys(fieldErrors).length > 0) {
    fail(400, 'errors.checkNotificationInfo', 'VALIDATION', fieldErrors);
  }

  const notification: AdminNotification = {
    id: `an-${Date.now().toString(36)}`,
    title: title!.trim(),
    content: content!.trim(),
    audience: audience!,
    status: send ? 'SENT' : 'DRAFT',
    recipientCount: send ? AUDIENCE_SIZE[audience!] : undefined,
    createdAt: new Date().toISOString(),
    sentAt: send ? new Date().toISOString() : undefined,
  };

  notifications.unshift(notification);
  return notification;
});

/** Gửi một thông báo đang ở dạng nháp. */
post('/admin/notifications/:id/send', ({ params }): AdminNotification => {
  const notification = notifications.find((n) => n.id === params.id);
  if (!notification) fail(404, 'errors.notificationNotFound', 'NOTIFICATION_NOT_FOUND');

  if (notification.status === 'SENT') {
    fail(409, 'errors.alreadySent', 'ALREADY_SENT');
  }

  notification.status = 'SENT';
  notification.sentAt = new Date().toISOString();
  notification.recipientCount = AUDIENCE_SIZE[notification.audience];

  return notification;
});
