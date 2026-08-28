-- PAN is a single company-level identifier, not a per-address one (GSTIN is
-- legitimately per-state and stays on organization_address). Best-effort
-- backfill: take the first non-null pan already recorded on any address for
-- an org before dropping the column.
UPDATE organizations o
SET pan = sub.pan
FROM (
    SELECT DISTINCT ON (organization_id) organization_id, pan
    FROM organization_address
    WHERE pan IS NOT NULL
    ORDER BY organization_id, seqp
) sub
WHERE o.seqp = sub.organization_id;

ALTER TABLE organization_address DROP COLUMN pan;
