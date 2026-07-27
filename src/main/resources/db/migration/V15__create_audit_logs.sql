-- Polymorphic audit log (Section 16): one shared table for Lead/Trip
-- (Escape)/Itinerary/Quote history rather than four separate tables, keyed
-- by entity_type + entity_id. Immutable — no updated_at/deleted_at, rows are
-- never edited after insert.
CREATE TABLE audit_logs (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT REFERENCES organizations (seqp),
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    performed_by BIGINT REFERENCES users (seqp),
    previous_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_org_id ON audit_logs (org_id);
