-- =====================================================================
-- EN-LEARNING · V1 · Schema khởi tạo (MySQL 8.x, InnoDB, utf8mb4)
--
-- Quy ước
--  * Khoá chính  : BINARY(16) chứa UUID, ứng dụng tự sinh (UUID v7). Mặc định
--                  ở DB chỉ phục vụ chèn tay/seed.
--  * Tên cột     : snake_case; entity dùng camelCase và được Spring ánh xạ.
--  * Song ngữ    : cặp cột xxx_vi (bắt buộc) / xxx_en (có thể trống).
--  * Xoá mềm     : cột deleted_at (User, nội dung, thông báo, hội thoại chat).
--  * Cột JSON    : danh sách nhỏ không truy vấn riêng (hints, paragraphs,
--                  accepted_answers, issues, prompts, improvements,
--                  prompt_feedback, links) nằm ngay trong bảng cha.
--  * Hành vi xoá : CASCADE cho dữ liệu phụ thuộc; RESTRICT cho nội dung đã
--                  nằm trong lịch sử học (nội dung đang dùng không được xoá,
--                  chỉ chuyển INACTIVE); SET NULL cho người tạo.
--  * Điểm số     : DECIMAL(3,1), thang 0.0–10.0.
--  * Kiểu ENUM   : ENUM gốc của MySQL, khớp @Enumerated(STRING) của Hibernate.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. XÁC THỰC & NGƯỜI DÙNG
-- ---------------------------------------------------------------------

CREATE TABLE users (
    id             BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL COMMENT 'BCrypt cost 12, không bao giờ trả ra API',
    full_name      VARCHAR(100) NOT NULL,
    phone_number   VARCHAR(20)  NULL,
    avatar_url     VARCHAR(500) NULL,
    role           ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    status         ENUM('ACTIVE', 'LOCKED', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    last_active_at DATETIME(6)  NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at     DATETIME(6)  NULL,
    PRIMARY KEY (id),
    -- Collation *_ci nên email không phân biệt hoa/thường.
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_role_status (role, status),
    KEY idx_users_full_name (full_name),
    KEY idx_users_created_at (created_at),
    KEY idx_users_last_active_at (last_active_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_settings (
    id                 BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id            BINARY(16)  NOT NULL,
    language           ENUM('VI', 'EN')       NOT NULL DEFAULT 'VI',
    theme              ENUM('LIGHT', 'DARK')  NOT NULL DEFAULT 'LIGHT',
    email_reminders    TINYINT(1)  NOT NULL DEFAULT 1,
    reminder_time      TIME        NOT NULL DEFAULT '20:00:00',
    daily_goal_minutes INT         NOT NULL DEFAULT 30,
    created_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_settings_user (user_id),
    -- Job nhắc học quét theo giờ nhắc của người bật nhắc.
    KEY idx_user_settings_reminder (email_reminders, reminder_time),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_settings_goal CHECK (daily_goal_minutes BETWEEN 1 AND 1440)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE refresh_tokens (
    id          BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id     BINARY(16)   NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hex của token; không lưu token thô',
    expires_at  DATETIME(6)  NOT NULL,
    revoked_at  DATETIME(6)  NULL,
    user_agent  VARCHAR(255) NULL,
    ip_address  VARCHAR(45)  NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id, revoked_at),
    KEY idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE password_reset_tokens (
    id          BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id     BINARY(16)   NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hex của token gửi qua email',
    expires_at  DATETIME(6)  NOT NULL,
    used_at     DATETIME(6)  NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_password_reset_tokens_hash (token_hash),
    KEY idx_password_reset_tokens_user (user_id, used_at),
    KEY idx_password_reset_tokens_expires_at (expires_at),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 2. NỘI DUNG HỌC
-- ---------------------------------------------------------------------

CREATE TABLE topics (
    id         BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    slug       VARCHAR(100) NOT NULL,
    name_vi    VARCHAR(100) NOT NULL,
    name_en    VARCHAR(100) NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_topics_slug (slug),
    KEY idx_topics_name_vi (name_vi),
    KEY idx_topics_name_en (name_en)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Flashcard ------------------------------------------------------------

CREATE TABLE flashcard_decks (
    id              BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    topic_id        BINARY(16)   NOT NULL,
    title_vi        VARCHAR(200) NOT NULL,
    title_en        VARCHAR(200) NULL,
    description_vi  VARCHAR(1000) NULL,
    description_en  VARCHAR(1000) NULL,
    cover_image_url VARCHAR(500) NULL,
    level           ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    status          ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by      BINARY(16)   NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6)  NULL,
    PRIMARY KEY (id),
    -- Danh sách lọc theo trạng thái + trình độ + chủ đề.
    KEY idx_flashcard_decks_filter (status, level, topic_id),
    KEY idx_flashcard_decks_topic (topic_id),
    KEY idx_flashcard_decks_title_vi (title_vi),
    KEY idx_flashcard_decks_title_en (title_en),
    KEY idx_flashcard_decks_created_by (created_by),
    CONSTRAINT fk_flashcard_decks_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE RESTRICT,
    CONSTRAINT fk_flashcard_decks_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE flashcards (
    id                 BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    deck_id            BINARY(16)   NOT NULL,
    word               VARCHAR(100) NOT NULL,
    phonetic           VARCHAR(100) NULL,
    meaning_vi         VARCHAR(500) NOT NULL,
    meaning_en         VARCHAR(500) NULL,
    part_of_speech_vi  VARCHAR(50)  NULL,
    part_of_speech_en  VARCHAR(50)  NULL,
    example            VARCHAR(500) NULL,
    example_meaning    VARCHAR(500) NULL,
    image_url          VARCHAR(500) NULL,
    audio_url          VARCHAR(500) NULL,
    sort_order         INT          NOT NULL DEFAULT 0,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at         DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_flashcards_deck_order (deck_id, sort_order),
    KEY idx_flashcards_word (word),
    CONSTRAINT fk_flashcards_deck FOREIGN KEY (deck_id) REFERENCES flashcard_decks (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện viết -----------------------------------------------------------

CREATE TABLE writing_prompts (
    id                BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    topic_id          BINARY(16)   NOT NULL,
    title_vi          VARCHAR(200) NOT NULL,
    title_en          VARCHAR(200) NULL,
    instructions      TEXT         NOT NULL COMMENT 'Yêu cầu đầy đủ của đề bài (tiếng Anh)',
    level             ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    suggested_minutes INT          NOT NULL DEFAULT 30,
    min_words         INT          NOT NULL DEFAULT 0,
    hints             JSON         NOT NULL DEFAULT (JSON_ARRAY()) COMMENT 'Mảng gợi ý hướng triển khai, giữ đúng thứ tự',
    status            ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by        BINARY(16)   NULL,
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_writing_prompts_filter (status, level, topic_id),
    KEY idx_writing_prompts_topic (topic_id),
    KEY idx_writing_prompts_title_vi (title_vi),
    KEY idx_writing_prompts_title_en (title_en),
    KEY idx_writing_prompts_created_by (created_by),
    CONSTRAINT fk_writing_prompts_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE RESTRICT,
    CONSTRAINT fk_writing_prompts_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_writing_prompts_limits CHECK (suggested_minutes >= 0 AND min_words >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện nghe -----------------------------------------------------------

CREATE TABLE listening_lessons (
    id               BINARY(16)    NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    topic_id         BINARY(16)    NOT NULL,
    title_vi         VARCHAR(200)  NOT NULL,
    title_en         VARCHAR(200)  NULL,
    description_vi   VARCHAR(1000) NULL,
    description_en   VARCHAR(1000) NULL,
    level            ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    audio_url        VARCHAR(500)  NULL COMMENT 'Tệp MP3 (Cloudinary); trống thì đọc transcript bằng TTS',
    duration_seconds INT           NOT NULL DEFAULT 0,
    transcript       TEXT          NOT NULL,
    status           ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by       BINARY(16)    NULL,
    created_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at       DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_listening_lessons_filter (status, level, topic_id),
    KEY idx_listening_lessons_topic (topic_id),
    KEY idx_listening_lessons_title_vi (title_vi),
    KEY idx_listening_lessons_title_en (title_en),
    KEY idx_listening_lessons_created_by (created_by),
    CONSTRAINT fk_listening_lessons_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE RESTRICT,
    CONSTRAINT fk_listening_lessons_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_listening_lessons_duration CHECK (duration_seconds >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện đọc ------------------------------------------------------------

CREATE TABLE reading_lessons (
    id                 BINARY(16)    NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    topic_id           BINARY(16)    NOT NULL,
    title_vi           VARCHAR(200)  NOT NULL,
    title_en           VARCHAR(200)  NULL,
    description_vi     VARCHAR(1000) NULL,
    description_en     VARCHAR(1000) NULL,
    level              ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    word_count         INT           NOT NULL DEFAULT 0 COMMENT 'Backend tính lại mỗi lần lưu đoạn văn',
    time_limit_minutes INT           NOT NULL DEFAULT 0 COMMENT '0 = không giới hạn',
    paragraphs         JSON          NOT NULL DEFAULT (JSON_ARRAY()) COMMENT 'Mảng đoạn văn, mỗi phần tử một đoạn, giữ đúng thứ tự',
    status             ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by         BINARY(16)    NULL,
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at         DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_reading_lessons_filter (status, level, topic_id),
    KEY idx_reading_lessons_topic (topic_id),
    KEY idx_reading_lessons_title_vi (title_vi),
    KEY idx_reading_lessons_title_en (title_en),
    KEY idx_reading_lessons_created_by (created_by),
    CONSTRAINT fk_reading_lessons_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reading_lessons_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_reading_lessons_limits CHECK (word_count >= 0 AND time_limit_minutes >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện nói ------------------------------------------------------------
-- Không có câu hỏi trong ngân hàng câu hỏi: người học đọc to từng câu/đoạn
-- (prompts) rồi AI chấm phát âm, từ vựng, ngữ pháp, độ trôi chảy.

CREATE TABLE speaking_lessons (
    id             BINARY(16)    NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    topic_id       BINARY(16)    NOT NULL,
    title_vi       VARCHAR(200)  NOT NULL,
    title_en       VARCHAR(200)  NULL,
    description_vi VARCHAR(1000) NULL,
    description_en VARCHAR(1000) NULL,
    level          ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    prompts        JSON          NOT NULL DEFAULT (JSON_ARRAY())
                   COMMENT 'Mảng câu cần đọc [{id, text, phonetic, meaningVi}], thứ tự mảng là thứ tự hiển thị; id ổn định để attempt tham chiếu',
    status         ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by     BINARY(16)    NULL,
    created_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at     DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_speaking_lessons_filter (status, level, topic_id),
    KEY idx_speaking_lessons_topic (topic_id),
    KEY idx_speaking_lessons_title_vi (title_vi),
    KEY idx_speaking_lessons_title_en (title_en),
    KEY idx_speaking_lessons_created_by (created_by),
    CONSTRAINT fk_speaking_lessons_topic FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE RESTRICT,
    CONSTRAINT fk_speaking_lessons_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Bài kiểm tra ---------------------------------------------------------

CREATE TABLE exams (
    id                 BINARY(16)    NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    title_vi           VARCHAR(200)  NOT NULL,
    title_en           VARCHAR(200)  NULL,
    description_vi     VARCHAR(1000) NULL,
    description_en     VARCHAR(1000) NULL,
    level              ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    time_limit_minutes INT           NOT NULL DEFAULT 0 COMMENT '0 = không giới hạn',
    status             ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by         BINARY(16)    NULL,
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at         DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_exams_filter (status, level),
    KEY idx_exams_title_vi (title_vi),
    KEY idx_exams_title_en (title_en),
    KEY idx_exams_created_by (created_by),
    CONSTRAINT fk_exams_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_exams_time_limit CHECK (time_limit_minutes >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Ngân hàng câu hỏi dùng chung cho Nghe, Đọc và Kiểm tra ----------------
-- Mỗi câu hỏi thuộc ĐÚNG MỘT trong ba cột listening_lesson_id,
-- reading_lesson_id, exam_id. MySQL cấm CHECK trên cột có FK CASCADE nên
-- quy tắc "đúng một chủ sở hữu" do tầng Service bảo đảm.

CREATE TABLE questions (
    id                  BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    listening_lesson_id BINARY(16)  NULL,
    reading_lesson_id   BINARY(16)  NULL,
    exam_id             BINARY(16)  NULL,
    skill               ENUM('VOCABULARY', 'WRITING', 'LISTENING', 'READING', 'EXAM') NOT NULL
                        COMMENT 'Kỹ năng của câu hỏi; dùng tách điểm theo kỹ năng trong bài kiểm tra',
    kind                ENUM('SINGLE_CHOICE', 'FILL_BLANK') NOT NULL,
    sort_order          INT         NOT NULL,
    content             TEXT        NOT NULL,
    explanation         TEXT        NULL COMMENT 'Chỉ trả về sau khi người dùng nộp bài',
    accepted_answers    JSON        NOT NULL DEFAULT (JSON_ARRAY())
                        COMMENT 'Câu điền từ: mảng đáp án chấp nhận được; Service so khớp không phân biệt hoa/thường',
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6) NULL,
    PRIMARY KEY (id),
    KEY idx_questions_listening (listening_lesson_id, sort_order),
    KEY idx_questions_reading (reading_lesson_id, sort_order),
    KEY idx_questions_exam (exam_id, sort_order),
    KEY idx_questions_skill (skill),
    CONSTRAINT fk_questions_listening_lesson FOREIGN KEY (listening_lesson_id) REFERENCES listening_lessons (id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_reading_lesson FOREIGN KEY (reading_lesson_id) REFERENCES reading_lessons (id) ON DELETE CASCADE,
    CONSTRAINT fk_questions_exam FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE question_options (
    id          BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    question_id BINARY(16)   NOT NULL,
    sort_order  INT          NOT NULL,
    content     VARCHAR(500) NOT NULL,
    is_correct  TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_question_options_order (question_id, sort_order),
    CONSTRAINT fk_question_options_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 3. TIẾN ĐỘ & KẾT QUẢ HỌC (luôn lưu MySQL, Redis chỉ cache)
-- ---------------------------------------------------------------------

CREATE TABLE user_flashcard_progress (
    id               BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id          BINARY(16)  NOT NULL,
    flashcard_id     BINARY(16)  NOT NULL,
    recall_level     ENUM('NOT_REMEMBERED', 'ALMOST_REMEMBERED', 'REMEMBERED') NOT NULL,
    review_count     INT         NOT NULL DEFAULT 1,
    interval_days    INT         NOT NULL DEFAULT 0,
    last_reviewed_at DATETIME(6) NOT NULL,
    next_review_at   DATETIME(6) NOT NULL COMMENT 'Backend tự tính, frontend không gửi',
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_flashcard_progress (user_id, flashcard_id),
    KEY idx_user_flashcard_progress_due (user_id, next_review_at),
    KEY idx_user_flashcard_progress_recent (user_id, last_reviewed_at),
    KEY idx_user_flashcard_progress_flashcard (flashcard_id),
    CONSTRAINT fk_user_flashcard_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_flashcard_progress_flashcard FOREIGN KEY (flashcard_id) REFERENCES flashcards (id) ON DELETE RESTRICT,
    CONSTRAINT chk_user_flashcard_progress_counts CHECK (review_count >= 0 AND interval_days >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện viết -----------------------------------------------------------

CREATE TABLE writing_submissions (
    id           BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id      BINARY(16)  NOT NULL,
    prompt_id    BINARY(16)  NOT NULL,
    content      TEXT        NOT NULL,
    word_count   INT         NOT NULL DEFAULT 0,
    status       ENUM('DRAFT', 'GRADING', 'GRADED', 'NEEDS_RETRY') NOT NULL DEFAULT 'DRAFT',
    submitted_at DATETIME(6) NULL COMMENT 'Trống khi còn là bản nháp',
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_writing_submissions_user (user_id, submitted_at),
    KEY idx_writing_submissions_user_prompt (user_id, prompt_id, status),
    KEY idx_writing_submissions_prompt (prompt_id),
    -- Job chấm lại quét bài NEEDS_RETRY / GRADING treo.
    KEY idx_writing_submissions_status (status, updated_at),
    CONSTRAINT fk_writing_submissions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_writing_submissions_prompt FOREIGN KEY (prompt_id) REFERENCES writing_prompts (id) ON DELETE RESTRICT,
    CONSTRAINT chk_writing_submissions_words CHECK (word_count >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE ai_feedbacks (
    id               BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    submission_id    BINARY(16)   NOT NULL,
    overall_score    DECIMAL(3,1) NOT NULL,
    grammar_score    DECIMAL(3,1) NOT NULL,
    vocabulary_score DECIMAL(3,1) NOT NULL,
    expression_score DECIMAL(3,1) NOT NULL,
    summary          TEXT         NOT NULL,
    issues           JSON         NOT NULL DEFAULT (JSON_ARRAY())
                     COMMENT 'Mảng lỗi [{category, excerpt, problem, suggestion}], category: GRAMMAR|VOCABULARY|EXPRESSION',
    model_name       VARCHAR(100) NULL COMMENT 'Mô hình AI đã chấm, phục vụ truy vết',
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_ai_feedbacks_submission (submission_id),
    CONSTRAINT fk_ai_feedbacks_submission FOREIGN KEY (submission_id) REFERENCES writing_submissions (id) ON DELETE CASCADE,
    CONSTRAINT chk_ai_feedbacks_scores CHECK (
        overall_score BETWEEN 0 AND 10 AND grammar_score BETWEEN 0 AND 10
        AND vocabulary_score BETWEEN 0 AND 10 AND expression_score BETWEEN 0 AND 10)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Làm bài Nghe / Đọc / Kiểm tra ----------------------------------------
-- Đúng một trong ba cột lesson/exam có giá trị. FK dùng RESTRICT nên
-- CHECK được phép và nội dung đã có lượt làm không thể bị xoá.

CREATE TABLE practice_attempts (
    id                  BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id             BINARY(16)   NOT NULL,
    listening_lesson_id BINARY(16)   NULL,
    reading_lesson_id   BINARY(16)   NULL,
    exam_id             BINARY(16)   NULL,
    status              ENUM('IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'IN_PROGRESS',
    started_at          DATETIME(6)  NOT NULL,
    submitted_at        DATETIME(6)  NULL,
    duration_seconds    INT          NULL,
    correct_count       INT          NULL,
    total_questions     INT          NULL,
    score               DECIMAL(3,1) NULL,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_practice_attempts_user_submitted (user_id, submitted_at),
    KEY idx_practice_attempts_user_status (user_id, status),
    KEY idx_practice_attempts_listening (listening_lesson_id, user_id),
    KEY idx_practice_attempts_reading (reading_lesson_id, user_id),
    KEY idx_practice_attempts_exam (exam_id, user_id),
    CONSTRAINT fk_practice_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_practice_attempts_listening FOREIGN KEY (listening_lesson_id) REFERENCES listening_lessons (id) ON DELETE RESTRICT,
    CONSTRAINT fk_practice_attempts_reading FOREIGN KEY (reading_lesson_id) REFERENCES reading_lessons (id) ON DELETE RESTRICT,
    CONSTRAINT fk_practice_attempts_exam FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE RESTRICT,
    CONSTRAINT chk_practice_attempts_owner CHECK (
        (listening_lesson_id IS NOT NULL) + (reading_lesson_id IS NOT NULL) + (exam_id IS NOT NULL) = 1),
    CONSTRAINT chk_practice_attempts_score CHECK (score IS NULL OR score BETWEEN 0 AND 10),
    CONSTRAINT chk_practice_attempts_counts CHECK (
        (correct_count IS NULL OR correct_count >= 0)
        AND (total_questions IS NULL OR total_questions >= 0)
        AND (duration_seconds IS NULL OR duration_seconds >= 0))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE practice_attempt_answers (
    id                 BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    attempt_id         BINARY(16)   NOT NULL,
    question_id        BINARY(16)   NOT NULL,
    selected_option_id BINARY(16)   NULL COMMENT 'Câu trắc nghiệm',
    answer_text        VARCHAR(500) NULL COMMENT 'Câu điền từ',
    is_correct         TINYINT(1)   NULL COMMENT 'Backend chấm khi nộp bài',
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_practice_attempt_answers (attempt_id, question_id),
    KEY idx_practice_attempt_answers_question (question_id),
    KEY idx_practice_attempt_answers_option (selected_option_id),
    CONSTRAINT fk_practice_attempt_answers_attempt FOREIGN KEY (attempt_id) REFERENCES practice_attempts (id) ON DELETE CASCADE,
    CONSTRAINT fk_practice_attempt_answers_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE RESTRICT,
    CONSTRAINT fk_practice_attempt_answers_option FOREIGN KEY (selected_option_id) REFERENCES question_options (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Luyện nói ------------------------------------------------------------
-- Một dòng cho mỗi lần nộp bài. Âm thanh KHÔNG được lưu: backend nhận bản
-- ghi, chuyển thành văn bản và chấm ngay, rồi bỏ âm thanh; DB chỉ giữ
-- transcript, điểm và nhận xét. Vì không còn âm thanh nên không có chế độ
-- chấm lại: AI lỗi thì lượt này là FAILED và người học ghi âm lại.
-- Điểm để NULL khi chưa chấm xong (GRADING) hoặc chấm hỏng (FAILED).

CREATE TABLE speaking_attempts (
    id                  BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id             BINARY(16)   NOT NULL,
    lesson_id           BINARY(16)   NOT NULL,
    status              ENUM('GRADING', 'GRADED', 'FAILED') NOT NULL DEFAULT 'GRADING',
    started_at          DATETIME(6)  NOT NULL,
    submitted_at        DATETIME(6)  NOT NULL,
    duration_seconds    INT          NOT NULL DEFAULT 0,
    overall_score       DECIMAL(3,1) NULL,
    pronunciation_score DECIMAL(3,1) NULL,
    vocabulary_score    DECIMAL(3,1) NULL,
    grammar_score       DECIMAL(3,1) NULL,
    fluency_score       DECIMAL(3,1) NULL,
    relevance_score     DECIMAL(3,1) NULL COMMENT 'Mức độ bám sát chủ đề',
    improvements        JSON         NOT NULL DEFAULT (JSON_ARRAY()) COMMENT 'Mảng chuỗi: những điểm cần cải thiện, viết ngắn gọn',
    prompt_feedback     JSON         NOT NULL DEFAULT (JSON_ARRAY())
                        COMMENT 'Mảng [{promptId, transcript, score, mispronounced[], comment}], mỗi câu đã đọc một phần tử',
    model_name          VARCHAR(100) NULL COMMENT 'Mô hình AI đã chấm, phục vụ truy vết',
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_speaking_attempts_user (user_id, submitted_at),
    KEY idx_speaking_attempts_user_lesson (user_id, lesson_id, status),
    KEY idx_speaking_attempts_lesson (lesson_id),
    -- Job đánh dấu FAILED cho lượt GRADING bị treo.
    KEY idx_speaking_attempts_status (status, updated_at),
    CONSTRAINT fk_speaking_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_speaking_attempts_lesson FOREIGN KEY (lesson_id) REFERENCES speaking_lessons (id) ON DELETE RESTRICT,
    CONSTRAINT chk_speaking_attempts_scores CHECK (
        (overall_score IS NULL OR overall_score BETWEEN 0 AND 10)
        AND (pronunciation_score IS NULL OR pronunciation_score BETWEEN 0 AND 10)
        AND (vocabulary_score IS NULL OR vocabulary_score BETWEEN 0 AND 10)
        AND (grammar_score IS NULL OR grammar_score BETWEEN 0 AND 10)
        AND (fluency_score IS NULL OR fluency_score BETWEEN 0 AND 10)
        AND (relevance_score IS NULL OR relevance_score BETWEEN 0 AND 10)),
    CONSTRAINT chk_speaking_attempts_graded CHECK (
        status <> 'GRADED' OR (overall_score IS NOT NULL AND pronunciation_score IS NOT NULL
            AND vocabulary_score IS NOT NULL AND grammar_score IS NOT NULL
            AND fluency_score IS NOT NULL AND relevance_score IS NOT NULL)),
    CONSTRAINT chk_speaking_attempts_time CHECK (duration_seconds >= 0 AND submitted_at >= started_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 4. THÔNG BÁO & EMAIL
-- ---------------------------------------------------------------------

CREATE TABLE notifications (
    id              BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    title           VARCHAR(200) NOT NULL,
    content         TEXT         NOT NULL,
    audience        ENUM('ALL', 'ACTIVE', 'INACTIVE', 'ADMIN') NOT NULL DEFAULT 'ALL',
    status          ENUM('DRAFT', 'SENT') NOT NULL DEFAULT 'DRAFT',
    recipient_count INT          NULL COMMENT 'Chỉ có khi đã gửi',
    sent_at         DATETIME(6)  NULL,
    created_by      BINARY(16)   NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6)  NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_status_sent (status, sent_at),
    KEY idx_notifications_audience (audience),
    KEY idx_notifications_created_by (created_by),
    CONSTRAINT fk_notifications_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_notifications (
    id              BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    notification_id BINARY(16)  NOT NULL,
    user_id         BINARY(16)  NOT NULL,
    read_at         DATETIME(6) NULL,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_notifications (notification_id, user_id),
    -- Trung tâm thông báo: lọc chưa đọc, mới nhất trước.
    KEY idx_user_notifications_inbox (user_id, read_at, created_at),
    CONSTRAINT fk_user_notifications_notification FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Nhật ký email. Giữ lại sau khi xoá người dùng (user_id -> NULL) để đối soát.
-- reminder_date chỉ đặt cho email nhắc học; UNIQUE (user_id, reminder_date)
-- bảo đảm "không gửi trùng email nhắc học trong cùng ngày" (NULL không xung đột).
CREATE TABLE email_logs (
    id              BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id         BINARY(16)   NULL,
    type            ENUM('STUDY_REMINDER', 'PASSWORD_RESET', 'NOTIFICATION') NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    status          ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    error_message   VARCHAR(500) NULL,
    reminder_date   DATE         NULL,
    sent_at         DATETIME(6)  NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uq_email_logs_daily_reminder (user_id, reminder_date),
    KEY idx_email_logs_status (status, created_at),
    KEY idx_email_logs_user_type (user_id, type, created_at),
    CONSTRAINT fk_email_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_email_logs_reminder_date CHECK (reminder_date IS NULL OR type = 'STUDY_REMINDER')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 5. CHAT VỚI TRỢ LÝ AI
-- ---------------------------------------------------------------------

-- Tiêu đề do backend đặt từ tin đầu tiên. Đoạn xem trước và số tin nhắn
-- của danh sách hội thoại không lưu cột riêng mà tính khi đọc, dựa vào
-- chỉ mục (conversation_id, created_at) của chat_messages.
-- Xoá hội thoại là xoá mềm: tin nhắn giữ lại để đối soát chi phí AI.
CREATE TABLE chat_conversations (
    id              BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id         BINARY(16)   NOT NULL,
    title           VARCHAR(200) NOT NULL,
    last_message_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6)  NULL,
    PRIMARY KEY (id),
    -- Danh sách hội thoại của người dùng, mới nhất trước.
    KEY idx_chat_conversations_user (user_id, last_message_at),
    CONSTRAINT fk_chat_conversations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Tin nhắn không sửa sau khi tạo nên không có updated_at.
-- model_name và số token chỉ có ở câu trả lời của trợ lý (theo dõi chi phí OpenAI).
CREATE TABLE chat_messages (
    id                BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    conversation_id   BINARY(16)   NOT NULL,
    role              ENUM('USER', 'ASSISTANT') NOT NULL,
    content           TEXT         NOT NULL,
    links             JSON         NOT NULL DEFAULT (JSON_ARRAY())
                      COMMENT 'Gợi ý bài học kèm theo [{label, path, skill}], chỉ có ở câu trả lời của trợ lý',
    is_refusal        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Từ chối vì câu hỏi ngoài phạm vi học tiếng Anh',
    model_name        VARCHAR(100) NULL,
    prompt_tokens     INT          NULL,
    completion_tokens INT          NULL,
    created_at        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_chat_messages_conversation (conversation_id, created_at),
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES chat_conversations (id) ON DELETE CASCADE,
    CONSTRAINT chk_chat_messages_assistant_only CHECK (
        role = 'ASSISTANT'
        OR (is_refusal = 0 AND model_name IS NULL AND prompt_tokens IS NULL AND completion_tokens IS NULL)),
    CONSTRAINT chk_chat_messages_tokens CHECK (
        (prompt_tokens IS NULL OR prompt_tokens >= 0) AND (completion_tokens IS NULL OR completion_tokens >= 0))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------
-- 6. THỜI GIAN HỌC (nguồn số liệu biểu đồ Dashboard)
-- ---------------------------------------------------------------------

-- Một dòng cho mỗi phiên học liên tục. Frontend gửi heartbeat khoảng 30 giây
-- một lần khi tab đang mở; backend cộng vào active_seconds nhưng chỉ tính
-- tối đa 60 giây cho mỗi heartbeat, và nếu im lặng quá 2 phút thì phiên kết
-- thúc, heartbeat sau đó mở phiên mới. Thời gian của một phiên tính vào
-- ngày của started_at. Biểu đồ Tuần/Tháng gom theo ngày ở múi giờ ứng dụng
-- (Asia/Ho_Chi_Minh), không gom theo ngày UTC.
-- ref_id trỏ tới bộ thẻ, đề viết, bài nghe/đọc/nói hoặc đề kiểm tra tuỳ theo
-- skill (CHAT: hội thoại, hoặc NULL). Không có FK vì trỏ nhiều bảng.
CREATE TABLE study_sessions (
    id                BINARY(16)  NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    user_id           BINARY(16)  NOT NULL,
    skill             ENUM('VOCABULARY', 'WRITING', 'LISTENING', 'READING', 'SPEAKING', 'EXAM', 'CHAT') NOT NULL,
    ref_id            BINARY(16)  NULL,
    started_at        DATETIME(6) NOT NULL,
    last_heartbeat_at DATETIME(6) NOT NULL,
    active_seconds    INT         NOT NULL DEFAULT 0,
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    -- Biểu đồ: tổng active_seconds của người dùng trong một khoảng ngày.
    KEY idx_study_sessions_user_started (user_id, started_at),
    -- Heartbeat: tìm phiên gần nhất còn mở của người dùng.
    KEY idx_study_sessions_user_heartbeat (user_id, last_heartbeat_at),
    CONSTRAINT fk_study_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_study_sessions_time CHECK (active_seconds >= 0 AND last_heartbeat_at >= started_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
