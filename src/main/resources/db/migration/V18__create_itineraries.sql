-- Section 8/15/16: Trip (escapes) -> many Itineraries. Versioned, never
-- overwritten in place — a new draft is a new row with an incremented
-- version, old ones marked 'superseded' rather than deleted.
CREATE TABLE IF NOT EXISTS itineraries (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    escape_id BIGINT NOT NULL REFERENCES escapes (seqp),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_itineraries_escape_id ON itineraries (escape_id);
CREATE INDEX IF NOT EXISTS idx_itineraries_org_id ON itineraries (org_id);
