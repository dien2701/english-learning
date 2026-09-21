import { useCallback, useEffect, useRef, useState } from 'react';

export type RecorderStatus =
  | 'IDLE'
  | 'REQUESTING'
  | 'RECORDING'
  | 'DENIED'
  | 'UNSUPPORTED';

export interface Recording {
  /** Đường dẫn blob để phát lại trong thẻ audio. */
  url: string;
  blob: Blob;
  durationSeconds: number;
}

/** Trình duyệt có đủ API để thu âm hay không. */
function isRecordingSupported(): boolean {
  return (
    typeof window !== 'undefined' &&
    typeof navigator !== 'undefined' &&
    Boolean(navigator.mediaDevices?.getUserMedia) &&
    typeof MediaRecorder !== 'undefined'
  );
}

/**
 * Thu âm bằng micro thật của người dùng qua MediaRecorder.
 *
 * Bản ghi nằm trong trình duyệt để người học nghe lại; chỉ khi nộp bài mới
 * được gửi lên backend (multipart), backend chuyển thành văn bản rồi bỏ đi.
 *
 * Quyền micro do trình duyệt hỏi, người dùng có toàn quyền từ chối; khi đó
 * hook trả về trạng thái DENIED để giao diện hướng dẫn cách bật lại.
 */
export function useRecorder() {
  const [status, setStatus] = useState<RecorderStatus>(() =>
    isRecordingSupported() ? 'IDLE' : 'UNSUPPORTED',
  );
  const [elapsed, setElapsed] = useState(0);
  /** Mức âm lượng 0–1, dùng vẽ sóng âm. */
  const [level, setLevel] = useState(0);

  const recorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<BlobPart[]>([]);
  const streamRef = useRef<MediaStream | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const rafRef = useRef<number | null>(null);
  const timerRef = useRef<number | null>(null);
  const startedAtRef = useRef(0);
  const resolveRef = useRef<((recording: Recording | null) => void) | null>(null);

  /** Dọn sạch stream, bộ phân tích và các bộ đếm. */
  const cleanup = useCallback(() => {
    if (rafRef.current !== null) cancelAnimationFrame(rafRef.current);
    if (timerRef.current !== null) window.clearInterval(timerRef.current);
    rafRef.current = null;
    timerRef.current = null;

    streamRef.current?.getTracks().forEach((track) => track.stop());
    streamRef.current = null;

    void audioContextRef.current?.close();
    audioContextRef.current = null;

    setLevel(0);
  }, []);

  useEffect(() => cleanup, [cleanup]);

  const start = useCallback(async (): Promise<boolean> => {
    if (status === 'UNSUPPORTED') return false;

    setStatus('REQUESTING');

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      // Đo mức âm lượng để vẽ sóng âm theo thời gian thực.
      const audioContext = new AudioContext();
      audioContextRef.current = audioContext;

      const analyser = audioContext.createAnalyser();
      analyser.fftSize = 256;
      audioContext.createMediaStreamSource(stream).connect(analyser);

      const data = new Uint8Array(analyser.frequencyBinCount);

      const tick = () => {
        analyser.getByteTimeDomainData(data);

        // Độ lệch trung bình so với mức im lặng (128) cho ra biên độ thô.
        let sum = 0;
        for (const value of data) sum += Math.abs(value - 128);
        setLevel(Math.min(1, sum / data.length / 40));

        rafRef.current = requestAnimationFrame(tick);
      };
      tick();

      chunksRef.current = [];
      const recorder = new MediaRecorder(stream);
      recorderRef.current = recorder;

      recorder.ondataavailable = (event) => {
        if (event.data.size > 0) chunksRef.current.push(event.data);
      };

      recorder.onstop = () => {
        // Giữ đúng định dạng trình duyệt thu (webm, ogg hoặc mp4) để backend nhận diện được.
        const blob = new Blob(chunksRef.current, {
          type: recorder.mimeType || 'audio/webm',
        });
        const durationSeconds = (Date.now() - startedAtRef.current) / 1000;

        cleanup();
        setStatus('IDLE');

        resolveRef.current?.(
          blob.size > 0
            ? { url: URL.createObjectURL(blob), blob, durationSeconds }
            : null,
        );
        resolveRef.current = null;
      };

      startedAtRef.current = Date.now();
      setElapsed(0);
      timerRef.current = window.setInterval(() => {
        setElapsed((prev) => prev + 1);
      }, 1000);

      recorder.start();
      setStatus('RECORDING');
      return true;
    } catch {
      cleanup();
      setStatus('DENIED');
      return false;
    }
  }, [status, cleanup]);

  /** Dừng thu và trả về bản ghi, hoặc null nếu không thu được gì. */
  const stop = useCallback((): Promise<Recording | null> => {
    const recorder = recorderRef.current;
    if (!recorder || recorder.state === 'inactive') {
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
      resolveRef.current = resolve;
      recorder.stop();
    });
  }, []);

  return { status, elapsed, level, start, stop };
}

export default useRecorder;
