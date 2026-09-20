import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Phát âm một từ hoặc câu tiếng Anh bằng giọng đọc của trình duyệt.
 *
 * Bản chạy thử không có tệp mp3 phát âm, nên dùng Web Speech API để người
 * học vẫn nghe được. Khi backend có đường dẫn âm thanh thật, chỉ cần đổi
 * chỗ gọi hàm này sang phát tệp.
 */
export function useSpeech() {
  const [isSpeaking, setIsSpeaking] = useState(false);

  // Khả năng hỗ trợ không đổi trong suốt vòng đời trang nên tính một lần
  // lúc khởi tạo, thay vì đặt lại state trong effect.
  const [isSupported] = useState(
    () => typeof window !== 'undefined' && 'speechSynthesis' in window,
  );

  const currentRef = useRef<SpeechSynthesisUtterance | null>(null);

  const stop = useCallback(() => {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) return;
    window.speechSynthesis.cancel();
    currentRef.current = null;
    setIsSpeaking(false);
  }, []);

  // Rời khỏi màn hình thì tắt tiếng, tránh giọng đọc còn chạy ở trang khác.
  useEffect(() => stop, [stop]);

  const speak = useCallback(
    (text: string, rate = 0.95) => {
      if (typeof window === 'undefined' || !('speechSynthesis' in window)) return;
      if (!text.trim()) return;

      window.speechSynthesis.cancel();

      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.rate = rate;
      utterance.onend = () => setIsSpeaking(false);
      utterance.onerror = () => setIsSpeaking(false);

      currentRef.current = utterance;
      window.speechSynthesis.speak(utterance);
      setIsSpeaking(true);
    },
    [],
  );

  return { speak, stop, isSpeaking, isSupported };
}

export default useSpeech;
