-- Provider-specific lead metadata, split out of `leads` on purpose (Lead/
-- LeadRepository/LeadsHelper must stay provider-agnostic — this is the join
-- point future providers (Google Ads, LinkedIn, CSV Import) use instead of
-- adding their own columns to leads). 1:1 with leads; nothing here is ever
-- read by core lead-creation/assignment logic.
CREATE TABLE IF NOT EXISTS lead_source_metadata (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lead_id BIGINT NOT NULL UNIQUE REFERENCES leads (seqp) ON DELETE CASCADE,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    provider VARCHAR(30) NOT NULL,         -- 'facebook' | 'instagram' (future: 'google_ads', 'linkedin', ...)
    platform_lead_id VARCHAR(100),         -- Meta's leadgen_id (== leads.source_ref_id, duplicated here for provider-scoped queries)
    campaign_id VARCHAR(64),
    campaign_name VARCHAR(255),
    adset_id VARCHAR(64),
    ad_id VARCHAR(64),
    ad_name VARCHAR(255),
    form_id VARCHAR(64),
    form_name VARCHAR(255),
    page_id VARCHAR(64),
    received_at TIMESTAMP,                 -- Meta's created_time for the leadgen event
    raw_field_data TEXT,                   -- the untouched field_data[] JSON, for support/debugging
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_lead_source_metadata_org_id ON lead_source_metadata (org_id);
CREATE INDEX IF NOT EXISTS idx_lead_source_metadata_form_id ON lead_source_metadata (form_id);
CREATE INDEX IF NOT EXISTS idx_lead_source_metadata_platform_lead_id ON lead_source_metadata (platform_lead_id);
