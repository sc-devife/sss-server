-- Org Id: a short, unique, human-readable identifier derived from
-- display_name at creation time (distinct from the internal uid/seqp).
-- Nullable at the DB level (non-destructive for the one known pre-existing
-- org row) — the app always sets it going forward; this backfills existing
-- rows best-effort so the column isn't blank in the UI.
ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS org_code VARCHAR(160);

UPDATE organizations
SET org_code = lower(regexp_replace(regexp_replace(coalesce(display_name, registered_name, 'org'), '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g'))
WHERE org_code IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_organizations_org_code ON organizations (org_code);
