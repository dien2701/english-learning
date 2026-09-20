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

import type { ThemeMode } from '../theme/palette';

const STORAGE_KEY = 'theme';

interface ThemeContextType {
  mode: ThemeMode;
  isDark: boolean;
  setMode: (mode: ThemeMode) => void;
  toggle: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

function readStoredMode(): ThemeMode {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'dark' ? 'dark' : 'light';
  } catch {
    // Chế độ ẩn danh có thể chặn localStorage.
    return 'light';
  }
}

/**
 * Nguồn duy nhất quyết định giao diện đang sáng hay tối.
 *
 * Trước đây trạng thái này nằm rải rác trong Header và trang Cài đặt, nên
 * đổi ở chỗ này thì chỗ kia không biết. Giờ gom về một nơi: context vừa
 * bật class .dark cho Tailwind, vừa cấp mode để App dựng lại theme của
 * Ant Design — hai hệ màu luôn đi cùng nhau.
 */
export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [mode, setModeState] = useState<ThemeMode>(readStoredMode);

  // Đồng bộ class trên thẻ html để Tailwind đổi toàn bộ biến màu.
  useEffect(() => {
    document.documentElement.classList.toggle('dark', mode === 'dark');
    document.documentElement.style.colorScheme = mode;

    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch {
      /* bỏ qua */
    }
  }, [mode]);

  const setMode = useCallback((next: ThemeMode) => setModeState(next), []);
  const toggle = useCallback(
    () => setModeState((prev) => (prev === 'dark' ? 'light' : 'dark')),
    [],
  );

  const value = useMemo<ThemeContextType>(
    () => ({ mode, isDark: mode === 'dark', setMode, toggle }),
    [mode, setMode, toggle],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};

export const useThemeMode = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useThemeMode phải được dùng bên trong ThemeProvider');
  }
  return context;
};
