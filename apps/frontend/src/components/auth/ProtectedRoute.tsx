import React from 'react';
import { useTranslation } from 'react-i18next';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import FullPageLoader from '../ui/FullPageLoader';

const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, isBootstrapping } = useAuth();
  const { t } = useTranslation();
  const location = useLocation();

  // Chưa biết người dùng đã đăng nhập hay chưa thì phải chờ, nếu không
  // trang sẽ nháy sang /login rồi mới quay lại.
  if (isBootstrapping) {
    return <FullPageLoader label={t('auth.restoringSession')} />;
  }

  if (!isAuthenticated) {
    // Nhớ trang đang muốn vào để đăng nhập xong quay lại đúng chỗ.
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
