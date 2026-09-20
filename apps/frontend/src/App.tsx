import React from 'react';
import { App as AntdApp, ConfigProvider } from 'antd';
import { StyleProvider } from '@ant-design/cssinjs';
import { Navigate, Route, Routes } from 'react-router-dom';

import AppShell from './components/layout/AppShell';
import AdminLayout from './components/layout/AdminLayout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import PublicRoute from './components/auth/PublicRoute';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider, useThemeMode } from './contexts/ThemeContext';
import { buildThemeConfig } from './theme/themeConfig';
import './App.css';

// Xác thực
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import ResetPasswordPage from './pages/auth/ResetPasswordPage';

// Tổng quan
import DashboardPage from './pages/DashboardPage';

// Flashcard
import FlashcardListPage from './pages/flashcard/FlashcardListPage';
import FlashcardDetailPage from './pages/flashcard/FlashcardDetailPage';
import FlashcardStudyPage from './pages/flashcard/FlashcardStudyPage';
import FlashcardResultPage from './pages/flashcard/FlashcardResultPage';

// Luyện viết
import WritingListPage from './pages/writing/WritingListPage';
import WritingPracticePage from './pages/writing/WritingPracticePage';
import WritingResultPage from './pages/writing/WritingResultPage';
import WritingHistoryPage from './pages/writing/WritingHistoryPage';

// Luyện nghe
import ListeningListPage from './pages/listening/ListeningListPage';
import ListeningPracticePage from './pages/listening/ListeningPracticePage';
import ListeningResultPage from './pages/listening/ListeningResultPage';

// Luyện đọc
import ReadingListPage from './pages/reading/ReadingListPage';
import ReadingPracticePage from './pages/reading/ReadingPracticePage';
import ReadingResultPage from './pages/reading/ReadingResultPage';

// Luyện nói
import SpeakingListPage from './pages/speaking/SpeakingListPage';
import SpeakingPracticePage from './pages/speaking/SpeakingPracticePage';
import SpeakingResultPage from './pages/speaking/SpeakingResultPage';

// Bài kiểm tra
import ExamListPage from './pages/exam/ExamListPage';
import ExamPracticePage from './pages/exam/ExamPracticePage';
import ExamResultPage from './pages/exam/ExamResultPage';
import ExamHistoryPage from './pages/exam/ExamHistoryPage';

// Thống kê, trợ lý, thông báo
import StatisticsPage from './pages/statistics/StatisticsPage';
import ChatPage from './pages/chat/ChatPage';
import NotificationCenterPage from './pages/notification/NotificationCenterPage';

// Hồ sơ
import ProfilePage from './pages/profile/ProfilePage';
import SettingsPage from './pages/profile/SettingsPage';

// Quản trị
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import ManageUsersPage from './pages/admin/ManageUsersPage';
import ManageContentPage from './pages/admin/ManageContentPage';
import ManageTopicsPage from './pages/admin/ManageTopicsPage';
import ManageNotificationsPage from './pages/admin/ManageNotificationsPage';

// Trang hệ thống
import ForbiddenPage from './pages/system/ForbiddenPage';
import NotFoundPage from './pages/system/NotFoundPage';
import ServerErrorPage from './pages/system/ServerErrorPage';

/**
 * Khu vực dành cho người học.
 * AppShell lo sidebar, header, footer và cửa sổ trợ lý AI.
 */
const UserArea: React.FC = () => (
  <AppShell>
    <Routes>
      <Route path="/dashboard" element={<DashboardPage />} />

      {/* Flashcard */}
      <Route path="/flashcard" element={<FlashcardListPage />} />
      <Route path="/flashcard/:id" element={<FlashcardDetailPage />} />
      <Route path="/flashcard/:id/study" element={<FlashcardStudyPage />} />
      <Route path="/flashcard/:id/result" element={<FlashcardResultPage />} />

      {/* Luyện viết */}
      <Route path="/writing" element={<WritingListPage />} />
      <Route path="/writing/history" element={<WritingHistoryPage />} />
      <Route path="/writing/:id" element={<WritingPracticePage />} />
      <Route path="/writing/:id/result" element={<WritingResultPage />} />

      {/* Luyện nghe */}
      <Route path="/listening" element={<ListeningListPage />} />
      <Route path="/listening/result/:attemptId" element={<ListeningResultPage />} />
      <Route path="/listening/:id" element={<ListeningPracticePage />} />

      {/* Luyện đọc */}
      <Route path="/reading" element={<ReadingListPage />} />
      <Route path="/reading/result/:attemptId" element={<ReadingResultPage />} />
      <Route path="/reading/:id" element={<ReadingPracticePage />} />

      {/* Luyện nói */}
      <Route path="/speaking" element={<SpeakingListPage />} />
      <Route path="/speaking/result/:attemptId" element={<SpeakingResultPage />} />
      <Route path="/speaking/:id" element={<SpeakingPracticePage />} />

      {/* Bài kiểm tra */}
      <Route path="/exam" element={<ExamListPage />} />
      <Route path="/exam/history" element={<ExamHistoryPage />} />
      <Route path="/exam/result/:attemptId" element={<ExamResultPage />} />
      <Route path="/exam/:id" element={<ExamPracticePage />} />

      {/* Thống kê, trợ lý, thông báo */}
      <Route path="/statistics" element={<StatisticsPage />} />
      <Route path="/chat" element={<ChatPage />} />
      <Route path="/notifications" element={<NotificationCenterPage />} />

      {/* Hồ sơ */}
      <Route path="/profile" element={<ProfilePage />} />
      <Route path="/settings" element={<SettingsPage />} />

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  </AppShell>
);

/** Khu vực quản trị, tách hẳn khỏi khu vực người học. */
const AdminArea: React.FC = () => (
  <AdminLayout>
    <Routes>
      <Route index element={<AdminDashboardPage />} />
      <Route path="users" element={<ManageUsersPage />} />
      <Route path="content" element={<ManageContentPage />} />
      <Route path="topics" element={<ManageTopicsPage />} />
      <Route path="notifications" element={<ManageNotificationsPage />} />
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  </AdminLayout>
);

/**
 * Lớp bọc theme: dựng lại cấu hình Ant Design mỗi khi đổi sáng/tối, để
 * component của AntD đổi màu cùng lúc với phần dựng bằng Tailwind.
 */
const ThemedApp: React.FC = () => {
  const { mode } = useThemeMode();
  const antdTheme = React.useMemo(() => buildThemeConfig(mode), [mode]);

  return (
    <ConfigProvider theme={antdTheme}>
      {/* AntdApp cấp context cho message, notification và Modal,
          nhờ đó chúng dùng đúng theme thay vì bản static mặc định. */}
      <AntdApp>
        <AuthProvider>
          <Routes>
            {/* Chỉ vào được khi chưa đăng nhập */}
            <Route element={<PublicRoute />}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
            </Route>

            {/* Trang hệ thống, ai cũng xem được */}
            <Route path="/403" element={<ForbiddenPage />} />
            <Route path="/404" element={<NotFoundPage />} />
            <Route path="/500" element={<ServerErrorPage />} />

            {/* Cần đăng nhập */}
            <Route element={<ProtectedRoute />}>
              <Route path="/admin/*" element={<AdminArea />} />
              <Route path="/*" element={<UserArea />} />
            </Route>
          </Routes>
        </AuthProvider>
      </AntdApp>
    </ConfigProvider>
  );
};

const App: React.FC = () => (
  /* StyleProvider layer đẩy toàn bộ CSS-in-JS của Ant Design vào một
     cascade layer. Không có nó, phần reset chạy lúc runtime của AntD
     (ví dụ `:where(.css-...) a { color: var(--ant-color-link) }`) nằm
     ngoài mọi lớp và đè lên utility của Tailwind, làm hỏng màu chữ và
     màu nền của thẻ a. */
  <StyleProvider layer>
    <ThemeProvider>
      <ThemedApp />
    </ThemeProvider>
  </StyleProvider>
);

export default App;
