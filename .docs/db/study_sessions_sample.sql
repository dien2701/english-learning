-- Dữ liệu mẫu thời gian học: 60 ngày gần nhất cho hocvien@ và admin@. Chỉ INSERT, không xoá gì.
-- Bỏ qua ngày đã có phiên học của người dùng đó nên chạy lại không nhân đôi. Chạy trên DB en_learning.
INSERT INTO study_sessions (user_id, skill, started_at, last_heartbeat_at, active_seconds)
WITH RECURSIVE d(n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM d WHERE n < 59),
k(i) AS (SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3),
x AS (
  SELECT u.id AS uid, u.email, d.n, k.i,
         TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL d.n DAY)) + INTERVAL (7 + k.i * 4) HOUR AS st,
         600 + MOD(CRC32(CONCAT(u.email, d.n, k.i, 's')), 2400) AS secs
  FROM users u CROSS JOIN d CROSS JOIN k
  WHERE u.email IN ('hocvien@enlearning.vn', 'admin@enlearning.vn')
    AND MOD(CRC32(CONCAT(u.email, d.n)), 10) >= 2
    AND k.i <= 1 + MOD(CRC32(CONCAT(u.email, 'c', d.n)), 3)
)
SELECT uid,
       ELT(1 + MOD(CRC32(CONCAT(email, n, i)), 7), 'VOCABULARY', 'LISTENING', 'READING', 'WRITING', 'SPEAKING', 'EXAM', 'CHAT'),
       st, st + INTERVAL secs SECOND, secs
FROM x
WHERE st + INTERVAL secs SECOND <= NOW()
  AND NOT EXISTS (SELECT 1 FROM study_sessions s WHERE s.user_id = x.uid AND DATE(s.started_at) = DATE(x.st));
