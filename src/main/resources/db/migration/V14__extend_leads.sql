-- Extends the pre-existing leads table (Section 15/7): reactivates the
-- commented-out "source" concept as a real source_code column, adds a
-- destination_id FK alongside the existing free-text destination column
-- (same non-destructive pattern as V9's Hotel.destination_id — no reliable
-- automatic mapping from historical free text to a destination code),
-- assignment fields for the Lead Assignment Engine, a priority flag, and
-- standard audit columns (leads had none before).
ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS source_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS source_ref_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS destination_id BIGINT REFERENCES escape_points (seqp),
    ADD COLUMN IF NOT EXISTS assigned_to_user_id BIGINT REFERENCES users (seqp),
    ADD COLUMN IF NOT EXISTS assignment_reason TEXT,
    ADD COLUMN IF NOT EXISTS is_priority BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

CREATE INDEX IF NOT EXISTS idx_leads_org_id ON leads (org_id);
CREATE INDEX IF NOT EXISTS idx_leads_assigned_to ON leads (assigned_to_user_id);
