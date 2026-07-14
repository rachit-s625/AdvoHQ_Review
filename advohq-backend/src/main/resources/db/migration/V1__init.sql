-- AdvoHQ initial schema
-- Working data lives in PostgreSQL; the binary documents themselves live in AWS S3
-- (only their metadata + S3 object key are stored here).

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- provides gen_random_uuid()

-- ── USERS ────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username            VARCHAR(60)  NOT NULL UNIQUE,
    password_hash       VARCHAR(100) NOT NULL,
    full_name           VARCHAR(120) NOT NULL,
    display_name        VARCHAR(120),
    phone               VARCHAR(20),
    email               VARCHAR(160),
    two_factor_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── PER-USER SETTINGS (toggle key/value, e.g. notifications, theme) ───
CREATE TABLE user_settings (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    setting_key   VARCHAR(80)  NOT NULL,
    setting_value VARCHAR(500) NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_setting UNIQUE (user_id, setting_key)
);
CREATE INDEX idx_user_settings_user ON user_settings(user_id);

-- ── CUSTOM CASE STAGES (advohq_custom_stages_v1) ─────────────────────
CREATE TABLE custom_stages (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(80) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_custom_stages_user ON custom_stages(user_id);

-- ── CASES ────────────────────────────────────────────────────────────
CREATE TABLE cases (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title        VARCHAR(300) NOT NULL,
    stage        VARCHAR(80),
    points       TEXT,                       -- free-text case notes / in-depth points
    case_number  VARCHAR(120),
    court        VARCHAR(200),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_cases_user ON cases(user_id);

-- ── IMPORTANT DATES (per case) ───────────────────────────────────────
CREATE TABLE important_dates (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id    UUID         NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    date_iso   DATE         NOT NULL,
    label      VARCHAR(160) NOT NULL,
    notified   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_important_dates_case ON important_dates(case_id);
CREATE INDEX idx_important_dates_date ON important_dates(date_iso);

-- ── SCHEDULE EVENTS (calendar) ───────────────────────────────────────
CREATE TABLE schedule_events (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(40)  NOT NULL DEFAULT 'other',  -- hearing|meeting|deadline|filing|other
    case_name  VARCHAR(300) NOT NULL,
    event_date DATE         NOT NULL,
    case_no    VARCHAR(120),
    location   VARCHAR(200),
    hall       VARCHAR(120),
    notes      TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_events_user ON schedule_events(user_id);
CREATE INDEX idx_events_date ON schedule_events(event_date);

-- ── DOCUMENTS (metadata only; bytes stored in S3) ────────────────────
CREATE TABLE documents (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    case_id      UUID         REFERENCES cases(id) ON DELETE SET NULL,
    file_name    VARCHAR(300) NOT NULL,
    content_type VARCHAR(160),
    kind         VARCHAR(20)  NOT NULL DEFAULT 'other',  -- pdf|docx|image|other
    size_bytes   BIGINT       NOT NULL DEFAULT 0,
    s3_key       VARCHAR(600) NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_documents_user ON documents(user_id);
CREATE INDEX idx_documents_case ON documents(case_id);
