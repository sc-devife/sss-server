-- quotes.tax_profile_id was a placeholder UUID with no FK (V20, before
-- tax_profiles existed). Now that it does, connect them properly rather
-- than leaving a dangling placeholder.
-- Postgres has no ADD CONSTRAINT IF NOT EXISTS — guard via pg_constraint.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_quotes_tax_profile') THEN
        ALTER TABLE quotes
            ADD CONSTRAINT fk_quotes_tax_profile FOREIGN KEY (tax_profile_id) REFERENCES tax_profiles (uid);
    END IF;
END $$;
