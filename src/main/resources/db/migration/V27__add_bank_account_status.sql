ALTER TABLE organization_bank_accounts
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';
