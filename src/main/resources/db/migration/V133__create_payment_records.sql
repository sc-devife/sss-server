-- Phase 4 of multi-currency: every payment is its own record, in the currency
-- actually received. A milestone's paid total (amount_paid_inr, base currency)
-- is still kept on the milestone; each record says how much of it this
-- payment covered and what exchange rates were involved.
--   fx_rate:               "1 base = fx_rate <received_currency>" at receipt (1 when the payment was in base)
--   applied_amount_base:   what is credited against the milestone. When the customer pays in the
--                          currency the quote was issued in, that is received / quote rate, so the
--                          milestone settles at the quoted price.
--   base_value_received:   what the money is actually worth in base at the receipt rate
--   fx_difference_base:    base_value_received - applied_amount_base (positive = gain, negative = loss)
CREATE TABLE payment_records (
    seqp                 BIGSERIAL PRIMARY KEY,
    uid                  UUID           NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    org_id               BIGINT         NOT NULL,
    milestone_id         BIGINT         NOT NULL REFERENCES payment_milestones (seqp) ON DELETE CASCADE,
    received_amount      NUMERIC(16, 2) NOT NULL,
    received_currency    VARCHAR(10)    NOT NULL,
    fx_rate              NUMERIC(24, 10) NOT NULL DEFAULT 1,
    applied_amount_base  NUMERIC(16, 2) NOT NULL,
    base_value_received  NUMERIC(16, 2) NOT NULL,
    fx_difference_base   NUMERIC(16, 2) NOT NULL DEFAULT 0,
    payment_method       VARCHAR(50),
    payment_reference    VARCHAR(255),
    recorded_by          BIGINT,
    recorded_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payment_records_milestone ON payment_records (milestone_id);

-- Plan feature: online payment gateway. Off until a vendor's plan enables it
-- (set by the platform, not editable in Organization settings).
ALTER TABLE organization_settings ADD COLUMN payment_gateway_enabled BOOLEAN NOT NULL DEFAULT FALSE;
