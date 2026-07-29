-- Section 8: "define a configurable cadence per organization... implement
-- this as a scheduled job system so cadence/rules are configurable, not
-- hardcoded." offset_days is relative to the milestone's due date
-- (negative = before due, 0 = on due date, positive = after due).
-- recurring=true means "fire every offset_days days from due date, for as
-- long as the milestone stays unpaid" (the overdue case); recurring=false
-- fires exactly once at that offset. Orgs with zero rules configured fall
-- back to the spec's recommended default cadence at send time, rather than
-- needing every org to set this up before reminders work at all.
CREATE TABLE IF NOT EXISTS reminder_rules (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    label VARCHAR(255) NOT NULL,
    offset_days INTEGER NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_reminder_rules_org_id ON reminder_rules (org_id);

-- Tracks which (milestone, calendar date) reminders have already gone out,
-- so a re-run of the daily job (or a milestone matching >1 rule the same
-- day) never double-sends.
CREATE TABLE IF NOT EXISTS payment_reminder_log (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    milestone_id BIGINT NOT NULL REFERENCES payment_milestones (seqp),
    sent_date DATE NOT NULL,
    UNIQUE (milestone_id, sent_date)
);
