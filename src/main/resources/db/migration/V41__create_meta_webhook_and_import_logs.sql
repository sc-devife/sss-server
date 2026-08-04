-- Delivery-level audit: every HTTP POST Meta sends, whether it produced a
-- lead or not (invalid signature, unknown page, disabled auto-create,
-- Meta's own retries of an already-processed event). Deliberately allows
-- multiple rows per leadgen_id — Meta retries deliveries, and each attempt
-- is worth keeping for support/debugging. Idempotency for lead CREATION
-- lives in lead_import_attempts below, not here.
CREATE TABLE IF NOT EXISTS meta_webhook_events (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT REFERENCES organizations (seqp),      -- nullable: unresolved page_id means org unknown
    platform VARCHAR(20),
    page_id VARCHAR(64),
    leadgen_id VARCHAR(100),
    form_id VARCHAR(64),
    ad_id VARCHAR(64),
    signature_valid BOOLEAN NOT NULL,
    processing_status VARCHAR(30) NOT NULL,             -- received/processed/duplicate_skipped/rejected/error
    error_message TEXT,
    raw_payload TEXT NOT NULL,                          -- exact webhook body, for replay/debugging
    received_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_meta_webhook_events_org_id ON meta_webhook_events (org_id);
CREATE INDEX IF NOT EXISTS idx_meta_webhook_events_leadgen_id ON meta_webhook_events (leadgen_id);

-- One logical row per (org, leadgen_id) — the idempotency guard AND the
-- resync source of truth. Updated in place on each processing/retry attempt
-- rather than appended, so "read the failed row, retry it" is a single
-- lookup by id with no need to find "the latest attempt among many."
CREATE TABLE IF NOT EXISTS lead_import_attempts (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    connection_id BIGINT NOT NULL REFERENCES integration_connections (seqp),
    provider VARCHAR(30) NOT NULL,
    leadgen_id VARCHAR(100) NOT NULL,
    form_id VARCHAR(64),
    page_id VARCHAR(64),
    ad_id VARCHAR(64),
    campaign_id VARCHAR(64),
    status VARCHAR(30) NOT NULL,           -- success/failed/duplicate_matched
    lead_id BIGINT REFERENCES leads (seqp),            -- set on success or duplicate_matched
    failure_reason TEXT,
    raw_merged_payload TEXT,               -- webhook envelope + fetched field_data, reused verbatim on resync
    retry_count INT NOT NULL DEFAULT 0,
    last_attempted_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (org_id, leadgen_id)
);
CREATE INDEX IF NOT EXISTS idx_lead_import_attempts_org_id ON lead_import_attempts (org_id);
CREATE INDEX IF NOT EXISTS idx_lead_import_attempts_status ON lead_import_attempts (org_id, status);
