-- Vendor contact number and rich-text (HTML) "Rules and Policies" for an
-- activity, edited from the Add/Edit Activity form.
ALTER TABLE activities ADD COLUMN contact_number TEXT;
ALTER TABLE activities ADD COLUMN rules_and_policies TEXT;
