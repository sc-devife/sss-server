-- New Cloudinary-HTML-backed quotation template system. Distinct from the
-- existing hardcoded QuoteTemplateDefinition list (still used by
-- organization_settings.quote_template_id) — this is additive, not a
-- replacement, so that existing feature keeps working untouched.
CREATE TABLE quotation_templates (
    seqp BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cloudinary_url VARCHAR(1000) NOT NULL,
    cloudinary_public_id VARCHAR(500) NOT NULL,
    preview_image_url VARCHAR(1000),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

ALTER TABLE organization_settings
    ADD COLUMN default_quotation_template_id UUID;
