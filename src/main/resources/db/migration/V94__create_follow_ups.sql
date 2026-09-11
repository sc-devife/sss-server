CREATE TABLE follow_ups (
    seqp BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT NOT NULL,
    lead_id BIGINT REFERENCES leads(seqp),
    escape_id BIGINT REFERENCES escapes(seqp),
    comment VARCHAR(2000) NOT NULL,
    actionable BOOLEAN NOT NULL DEFAULT TRUE,
    due_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    assigned_to_user_id BIGINT NOT NULL REFERENCES users(seqp),
    completed_at TIMESTAMP,
    reminder_30_sent_at TIMESTAMP,
    reminder_15_sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT follow_up_one_parent CHECK (
        (lead_id IS NOT NULL AND escape_id IS NULL) OR (lead_id IS NULL AND escape_id IS NOT NULL)
    )
);

CREATE INDEX idx_follow_ups_assigned_org ON follow_ups(assigned_to_user_id, org_id);
CREATE INDEX idx_follow_ups_lead ON follow_ups(lead_id);
CREATE INDEX idx_follow_ups_escape ON follow_ups(escape_id);
CREATE INDEX idx_follow_ups_due_at ON follow_ups(due_at);
