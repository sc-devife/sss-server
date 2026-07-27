-- Organization fields per the data dictionary, plus standard audit columns
-- now that Spring Data JPA auditing (@CreatedBy/@LastModifiedBy) is wired up
-- via AuditorAwareConfig, reading the authenticated principal set by
-- JwtAuthenticationFilter.
--
-- quote_template_id / invoice_template_id are forward-looking only — the
-- template tables themselves don't exist until Phase 5/6, so no FK yet.

ALTER TABLE organizations
    ADD COLUMN country_code VARCHAR(255),
    ADD COLUMN default_currency_code VARCHAR(255),
    ADD COLUMN logo_file VARCHAR(255),
    ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN quote_template_id UUID,
    ADD COLUMN invoice_template_id UUID,
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP,
    ADD COLUMN created_by BIGINT REFERENCES users (seqp),
    ADD COLUMN updated_by BIGINT REFERENCES users (seqp);
