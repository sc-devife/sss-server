-- quotes.tax_profile_id was a placeholder UUID with no FK (V20, before
-- tax_profiles existed). Now that it does, connect them properly rather
-- than leaving a dangling placeholder.
ALTER TABLE quotes
    ADD CONSTRAINT fk_quotes_tax_profile FOREIGN KEY (tax_profile_id) REFERENCES tax_profiles (uid);
