-- Lead source: replaces the flat source_code (manual/whatsapp/instagram/
-- youtube/google_ads/agency) with a real sourceType (DIRECT/AGENCY) plus a
-- sourceChannel that only applies to direct inbound channels. Agency-sourced
-- leads get a proper LeadAgencyDetails record (billing/contact info) instead
-- of nothing.
ALTER TABLE leads ADD COLUMN source_type VARCHAR(20) NOT NULL DEFAULT 'DIRECT';
UPDATE leads SET source_type = 'AGENCY' WHERE source_code = 'agency';
ALTER TABLE leads RENAME COLUMN source_code TO source_channel;
UPDATE leads SET source_channel = NULL WHERE source_type = 'AGENCY';

-- Leads are never individually assigned — any eligible user can work any
-- lead. Assignment happens exactly once, at Escape-conversion time (see
-- escapes.assigned_to_user_id in V63).
ALTER TABLE leads DROP COLUMN assigned_to_user_id;
ALTER TABLE leads DROP COLUMN assignment_reason;

CREATE TABLE lead_agency_details (
    lead_id BIGINT PRIMARY KEY REFERENCES leads(seqp),
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(30),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    pincode VARCHAR(20),
    street_address VARCHAR(255),
    locality VARCHAR(255),
    landmark VARCHAR(255),
    billing_name VARCHAR(255),
    additional_billing_details TEXT
);
