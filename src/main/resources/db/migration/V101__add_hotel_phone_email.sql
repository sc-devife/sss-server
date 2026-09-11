-- Splits the old single free-text "Contact info" field into structured
-- Phone Number / Email fields for the Add/Edit Hotel form. contact_info
-- itself is kept as-is (existing data, and bulk import's "contactInfo"
-- CSV column, both still work unchanged) — just no longer shown/edited
-- from the Hotel form now that these two replace it there.
ALTER TABLE hotels
    ADD COLUMN phone_number VARCHAR(30),
    ADD COLUMN email VARCHAR(255);
