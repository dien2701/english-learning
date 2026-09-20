/* eslint-disable react-refresh/only-export-components */
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import {
  authService,
  type LoginPayload,
  type RegisterPayload,
} from '../services/authService';
import { setUnauthorizedHandler, tokenStore } from '../shared/api/client';
import type { User } from '../types/common';

interface AuthContextType {
  /** true khi đã có người dùng hợp lệ. */
  isAuthenticated: boolean;
  /** true trong lúc khôi phục phiên từ token đã lưu, lúc mới tải trang. */
  isBootstrapping: boolean;
  user: User | null;
  isAdmin: boolean;
  login: (payload: LoginPayload) => Promise<User>;
  register: (payload: RegisterPayload) => Promise<User>;
  logout: () => Promise<void>;
  updateUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isBootstrapping, setIsBootstrapping] = useState<boolean>(
    () => tokenStore.get() !== null,
  );

  /* Khôi phục phiên khi tải lại trang: nếu còn token thì hỏi lại server
     xem token có còn hiệu lực không, thay vì tin vào dữ liệu trong
     localStorage. */
  useEffect(() => {
    // Không có token thì isBootstrapping đã là false ngay từ giá trị khởi tạo.
    if (tokenStore.get() === null) return;

    let cancelled = false;

    authService
      .me()
      .then((me) => {
        if (!cancelled) setUser(me);
      })
      .catch(() => {
        // Token hỏng hoặc đã hết hạn — coi như chưa đăng nhập.
        tokenStore.clear();
        if (!cancelled) setUser(null);
      })
      .finally(() => {
        if (!cancelled) setIsBootstrapping(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  /* Khi bất kỳ request nào nhận 401, lớp API sẽ gọi vào đây để dọn phiên. */
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
  }, []);

  const login = useCallback(async (payload: LoginPayload) => {
    const session = await authService.login(payload);
    setUser(session.user);
    return session.user;
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    const session = await authService.register(payload);
    setUser(session.user);
    return session.user;
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setUser(null);
  }, []);

  const updateUser = useCallback((updatedUser: User) => {
    setUser(updatedUser);
  }, []);

  const value = useMemo<AuthContextType>(
    () => ({
      isAuthenticated: user !== null,
      isBootstrapping,
      user,
      isAdmin: user?.role === 'ADMIN',
      login,
      register,
      logout,
      updateUser,
    }),
    [user, isBootstrapping, login, register, logout, updateUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth phải được dùng bên trong AuthProvider');
  }
  return context;
};
