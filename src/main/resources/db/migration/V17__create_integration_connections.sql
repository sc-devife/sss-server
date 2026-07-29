-- Section 15: Integration Connection — one row per org per channel they've
-- configured. `config` holds a webhook secret / future OAuth token; storing
-- it as plain text here since there's no KMS/secrets-manager wired into
-- this app yet — flagged as a gap to close before this goes to real
-- production traffic with real customer credentials.
CREATE TABLE IF NOT EXISTS integration_connections (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    channel_code VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'disconnected',
    config TEXT,
    auto_create_leads BOOLEAN NOT NULL DEFAULT true,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (org_id, channel_code)
);

CREATE INDEX IF NOT EXISTS idx_integration_connections_org_id ON integration_connections (org_id);
