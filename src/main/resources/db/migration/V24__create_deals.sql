-- Section 8/15/16: "exactly one accepted quote (from one itinerary) per
-- trip converts to a Deal; all sibling itineraries/quotes remain as
-- history, marked superseded/rejected." UNIQUE(escape_id) enforces the
-- "exactly one Deal per trip" rule at the DB level, not just in app code.
CREATE TABLE deals (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    escape_id BIGINT NOT NULL UNIQUE REFERENCES escapes (seqp),
    accepted_quote_id BIGINT NOT NULL REFERENCES quotes (seqp),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_deals_org_id ON deals (org_id);
