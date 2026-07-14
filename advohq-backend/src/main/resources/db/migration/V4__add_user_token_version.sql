-- Bumping token_version invalidates every JWT issued before the bump —
-- used on password change so a stolen token dies with the old password.
ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
