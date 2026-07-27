-- Extends the pre-existing leads table (Section 15/7): reactivates the
-- commented-out "source" concept as a real source_code column, adds a
-- destination_id FK alongside the existing free-text destination column
-- (same non-destructive pattern as V9's Hotel.destination_id — no reliable
-- automatic mapping from historical free text to a destination code),
-- assignment fields for the Lead Assignment Engine, a priority flag, and
-- standard audit columns (leads had none before).
ALTER TABLE leads
    ADD COLUMN source_code VARCHAR(20),
    ADD COLUMN source_ref_id VARCHAR(255),
    ADD COLUMN destination_id BIGINT REFERENCES escape_points (seqp),
    ADD COLUMN assigned_to_user_id BIGINT REFERENCES users (seqp),
    ADD COLUMN assignment_reason TEXT,
    ADD COLUMN is_priority BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;

CREATE INDEX idx_leads_org_id ON leads (org_id);
CREATE INDEX idx_leads_assigned_to ON leads (assigned_to_user_id);
