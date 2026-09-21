-- Đợt 11 (11d): Luyện nói chấm từng câu ngay sau khi thu.
-- Lượt IN_PROGRESS gom kết quả từng câu; nộp cuối chỉ tổng hợp thành GRADED.

ALTER TABLE speaking_attempts
    MODIFY COLUMN status ENUM('IN_PROGRESS', 'GRADING', 'GRADED', 'FAILED') NOT NULL DEFAULT 'GRADING';

-- Kết quả một câu trong lượt nói. Âm thanh không được lưu: chỉ giữ transcript, điểm và nhận xét.
-- Thu lại câu thì ghi đè dòng cũ (unique attempt_id + prompt_id).
CREATE TABLE speaking_prompt_results (
    id          BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    attempt_id  BINARY(16)   NOT NULL,
    prompt_id   BINARY(16)   NOT NULL COMMENT 'Câu trong speaking_lessons.prompts (JSON)',
    transcript  TEXT         NOT NULL,
    score       DECIMAL(3,1) NOT NULL,
    word_issues JSON         NOT NULL DEFAULT (JSON_ARRAY())
                COMMENT 'Mảng [{word, heardAs, issue, tip}]: từ đọc sai/thiếu/thừa',
    tips        JSON         NOT NULL DEFAULT (JSON_ARRAY()) COMMENT 'Mảng chuỗi: mẹo cải thiện cho câu này',
    model_name  VARCHAR(100) NULL COMMENT 'Mô hình AI đã chấm, phục vụ truy vết',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_speaking_prompt_results_attempt_prompt (attempt_id, prompt_id),
    CONSTRAINT fk_speaking_prompt_results_attempt FOREIGN KEY (attempt_id) REFERENCES speaking_attempts (id) ON DELETE CASCADE,
    CONSTRAINT chk_speaking_prompt_results_score CHECK (score BETWEEN 0 AND 10)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
