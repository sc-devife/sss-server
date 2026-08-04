-- Per-org (org-level default) or per-form (override) mapping from a Meta
-- field_data question key to a CRM Lead field. form_id NULL = org default,
-- applied to any form without its own row. A form-specific row always wins
-- over the org default, which always wins over the hardcoded fallback in
-- MetaFieldMappingResolver (so this works before any admin configures
-- anything).
CREATE TABLE IF NOT EXISTS meta_field_mappings (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    form_id VARCHAR(64),                   -- NULL = org-wide default
    meta_field_key VARCHAR(100) NOT NULL,  -- Meta's field_data[].name, e.g. "full_name", "q_which_destination"
    crm_field VARCHAR(30) NOT NULL,        -- name/email/phone/destination_hint/travel_date/number_of_people/duration_days/notes/ignore
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_meta_field_mappings_org_id ON meta_field_mappings (org_id);
-- Postgres UNIQUE treats NULL form_id as always distinct, so a plain column
-- constraint wouldn't stop duplicate org-default rows for the same key —
-- two partial indexes instead, one per case.
CREATE UNIQUE INDEX IF NOT EXISTS uq_meta_field_mappings_form
    ON meta_field_mappings (org_id, form_id, meta_field_key) WHERE form_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_meta_field_mappings_org_default
    ON meta_field_mappings (org_id, meta_field_key) WHERE form_id IS NULL;
