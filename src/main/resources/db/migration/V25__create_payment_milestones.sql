-- Section 15/16: Deal -> many PaymentMilestone. amount_paid_usd isn't in
-- the dictionary explicitly but is required to make "partially_paid"
-- actually mean something; label isn't in the dictionary either but staff
-- need a name for each milestone (Deposit, Balance, ...).
CREATE TABLE payment_milestones (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    deal_id BIGINT NOT NULL REFERENCES deals (seqp),
    label VARCHAR(255) NOT NULL,
    due_date DATE NOT NULL,
    amount_usd NUMERIC(14, 2) NOT NULL,
    amount_paid_usd NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    marked_paid_by BIGINT REFERENCES users (seqp),
    marked_paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_payment_milestones_deal_id ON payment_milestones (deal_id);
CREATE INDEX idx_payment_milestones_due_date ON payment_milestones (due_date);
