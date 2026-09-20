import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import FullPageLoader from '../ui/FullPageLoader';

const PublicRoute: React.FC = () => {
  const { isAuthenticated, isAdmin, isBootstrapping } = useAuth();

  if (isBootstrapping) {
    return <FullPageLoader />;
  }

  if (isAuthenticated) {
    return <Navigate to={isAdmin ? '/admin' : '/dashboard'} replace />;
  }

  return <Outlet />;
};

export default PublicRoute;
