-- Mã OTP xác minh email khi đăng ký. Tài khoản chỉ được tạo khi mã đúng nên mã gắn với email, không với user.
CREATE TABLE email_verification_codes (
    id          BINARY(16)   NOT NULL DEFAULT (UUID_TO_BIN(UUID())),
    email       VARCHAR(255) NOT NULL,
    code_hash   VARCHAR(64)  NOT NULL COMMENT 'HMAC-SHA256 hex của mã OTP 6 số (khoá RESET_CODE_SECRET, băm kèm email); không lưu mã thô',
    expires_at  DATETIME(6)  NOT NULL,
    used_at     DATETIME(6)  NULL,
    attempts    INT          NOT NULL DEFAULT 0 COMMENT 'Số lần nhập sai mã; đủ 5 lần thì Service huỷ mã',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_email_verification_codes_email (email, used_at),
    KEY idx_email_verification_codes_expires_at (expires_at),
    CONSTRAINT chk_email_verification_codes_attempts CHECK (attempts BETWEEN 0 AND 10)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

ALTER TABLE email_logs
    MODIFY type ENUM('STUDY_REMINDER', 'PASSWORD_RESET', 'NOTIFICATION', 'EMAIL_VERIFICATION') NOT NULL;
