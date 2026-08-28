-- UserCredential: replace the loose seqa/seqa_type polymorphic FK (seqa_type
-- was always the literal 'users' — never actually polymorphic in practice)
-- with a real FK, and add account-lockout tracking. No brute-force
-- protection existed anywhere in the login flow before this.
ALTER TABLE user_credentials ADD COLUMN user_id BIGINT REFERENCES users (seqp);
UPDATE user_credentials SET user_id = seqa WHERE seqa_type = 'users';
ALTER TABLE user_credentials ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE user_credentials ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE user_credentials ADD COLUMN locked_until TIMESTAMP;
ALTER TABLE user_credentials DROP COLUMN seqa;
ALTER TABLE user_credentials DROP COLUMN seqa_type;
CREATE UNIQUE INDEX idx_user_credentials_user_id ON user_credentials (user_id);

-- UserSession: replace username-as-primary-key (one session per user — a
-- second login silently invalidated the first) with a real session_id PK +
-- user FK, so a user can be logged in on multiple devices at once. Adds
-- device/IP visibility and per-session revocation for a "these are your
-- active sessions, log out others" feature.
ALTER TABLE user_sessions ADD COLUMN session_id UUID;
UPDATE user_sessions SET session_id = gen_random_uuid();
ALTER TABLE user_sessions ADD COLUMN user_id BIGINT REFERENCES users (seqp);
UPDATE user_sessions us SET user_id = u.seqp FROM users u WHERE u.email = us.username;
-- Orphaned rows (a session whose username no longer matches any user's
-- email) can't satisfy the NOT NULL FK below — safe to drop, they're stale.
DELETE FROM user_sessions WHERE user_id IS NULL;
ALTER TABLE user_sessions ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE user_sessions ADD COLUMN device_info VARCHAR(255);
ALTER TABLE user_sessions ADD COLUMN ip_address VARCHAR(64);
ALTER TABLE user_sessions ADD COLUMN created_at TIMESTAMP;
UPDATE user_sessions SET created_at = COALESCE(last_accessed, now());
ALTER TABLE user_sessions ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE user_sessions ADD COLUMN revoked_at TIMESTAMP;
ALTER TABLE user_sessions DROP CONSTRAINT user_sessions_pkey;
ALTER TABLE user_sessions ADD PRIMARY KEY (session_id);
ALTER TABLE user_sessions DROP COLUMN username;

CREATE INDEX idx_user_sessions_user_id ON user_sessions (user_id);
