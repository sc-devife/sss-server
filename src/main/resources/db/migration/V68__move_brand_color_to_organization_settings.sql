-- Brand Primary Color moved from the profile/identity table (organizations)
-- to the behavioral/config table (organization_settings) — it's a document
-- styling default, not who the org is, same rationale as V58's original split.
ALTER TABLE organization_settings ADD COLUMN brand_primary_color VARCHAR(9);

UPDATE organization_settings s
SET brand_primary_color = o.brand_primary_color
FROM organizations o
WHERE o.seqp = s.org_id;

ALTER TABLE organizations DROP COLUMN brand_primary_color;
