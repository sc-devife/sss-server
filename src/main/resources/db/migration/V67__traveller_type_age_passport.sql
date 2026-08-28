-- Traveller: wire up the previously-unused type/age columns into a real
-- adult/child/infant classification (for future per-traveller quote
-- pricing — neither was surfaced anywhere in the UI before this), fix age's
-- type from free text to a real number, and add passport/document fields —
-- the only new traveller data this pass adds (dietary/medical and
-- emergency-contact were explicitly deferred to a later pass).
ALTER TABLE traveller ALTER COLUMN age TYPE INTEGER USING NULLIF(age, '')::INTEGER;
ALTER TABLE traveller ADD COLUMN passport_number VARCHAR(50);
ALTER TABLE traveller ADD COLUMN passport_expiry DATE;
ALTER TABLE traveller ADD COLUMN passport_issuing_country VARCHAR(4);
