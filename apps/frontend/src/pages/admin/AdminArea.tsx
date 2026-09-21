import React from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';

import AdminLayout from '../../components/layout/AdminLayout';
import AdminDashboardPage from './AdminDashboardPage';
import ManageUsersPage from './ManageUsersPage';
import ManageContentPage from './ManageContentPage';
import ManageTopicsPage from './ManageTopicsPage';
import ManageNotificationsPage from './ManageNotificationsPage';

/** Khu vực quản trị, tách hẳn khỏi khu vực người học (nạp động từ App). */
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

export default AdminArea;
