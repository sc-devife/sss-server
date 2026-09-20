-- Free-text "About" blurb and rich-text (HTML) "Rules and Policies" for a hotel,
-- edited from the Add/Edit Hotel form.
ALTER TABLE hotels ADD COLUMN about TEXT;
ALTER TABLE hotels ADD COLUMN rules_and_policies TEXT;
