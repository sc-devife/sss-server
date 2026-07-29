-- Matches the user's Excel "Tax Style" module (Name, Display Name,
-- Description, Value; Add/Deactivate) and spec Section 6's "pluggable tax
-- rules, not hardcoded percentages" — each org defines its own named tax
-- rates (GST 18%, VAT 20%, No Tax, ...) and picks one per quote via
-- Quote.tax_profile_id (already a placeholder UUID column since V20).
CREATE TABLE IF NOT EXISTS tax_profiles (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    description TEXT,
    rate_percent NUMERIC(6, 3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX IF NOT EXISTS idx_tax_profiles_org_id ON tax_profiles (org_id);
