-- Splits behavioral/configuration settings out of `organizations` into a
-- dedicated 1:1 table, so future settings (notification prefs, reminder
-- cadences, etc.) have a proper home instead of piling onto the profile row.
CREATE TABLE organization_settings (
    org_id BIGINT PRIMARY KEY REFERENCES organizations(seqp),
    auto_assign_enabled BOOLEAN NOT NULL DEFAULT true,
    default_currency_code VARCHAR(10),
    quote_template_id UUID,
    invoice_template_id UUID,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    default_locale VARCHAR(10) NOT NULL DEFAULT 'en',
    default_payment_terms_days INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO organization_settings (org_id, auto_assign_enabled, default_currency_code, quote_template_id, invoice_template_id)
SELECT seqp, COALESCE(auto_assign_enabled, true), default_currency_code, quote_template_id, invoice_template_id
FROM organizations;

ALTER TABLE organizations DROP COLUMN auto_assign_enabled;
ALTER TABLE organizations DROP COLUMN default_currency_code;
ALTER TABLE organizations DROP COLUMN quote_template_id;
ALTER TABLE organizations DROP COLUMN invoice_template_id;
