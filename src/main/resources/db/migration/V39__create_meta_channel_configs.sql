-- Meta (Facebook Page / Instagram Business Account) connection details.
-- Split out from integration_connections (which stays channel-agnostic per
-- its own V17 design) so provider-specific structure never leaks into the
-- generic connection registry. One row per integration_connections row.
CREATE TABLE IF NOT EXISTS meta_channel_configs (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    connection_id BIGINT NOT NULL UNIQUE REFERENCES integration_connections (seqp) ON DELETE CASCADE,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    platform VARCHAR(20) NOT NULL,           -- 'facebook' | 'instagram'
    page_id VARCHAR(64),                     -- Facebook Page ID (facebook platform)
    ig_account_id VARCHAR(64),               -- Instagram Business Account ID (instagram platform)
    page_name VARCHAR(255),                  -- display only, never a secret
    token_type VARCHAR(20) NOT NULL DEFAULT 'manual_long_lived', -- Stage 2 adds 'oauth'
    encrypted_access_token TEXT NOT NULL,    -- AES-256-GCM ciphertext, base64
    token_iv VARCHAR(64) NOT NULL,           -- base64 GCM nonce, unique per encryption
    token_last_verified_at TIMESTAMP,        -- last time a Graph API call using this token succeeded
    connected_by_user_id BIGINT REFERENCES users (seqp),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_meta_channel_configs_page_id
    ON meta_channel_configs (page_id) WHERE page_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_meta_channel_configs_ig_account_id
    ON meta_channel_configs (ig_account_id) WHERE ig_account_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_meta_channel_configs_org_id ON meta_channel_configs (org_id);
