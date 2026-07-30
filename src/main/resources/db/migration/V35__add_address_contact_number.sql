-- The pre-existing organization_address table never had a contact number
-- column (was commented out in the entity/DTO) — adding it for real now
-- that the Contact Address panel needs it.
ALTER TABLE organization_address
    ADD COLUMN IF NOT EXISTS contact_number VARCHAR(50);
