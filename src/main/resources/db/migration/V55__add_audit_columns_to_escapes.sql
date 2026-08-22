-- Escape never extended Auditable (a pre-existing gap, not introduced here) —
-- existing rows have no real creation timestamp, so backfill with now() as
-- the least-wrong default rather than leaving them NULL.
ALTER TABLE escapes ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE escapes ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE escapes ADD COLUMN created_by BIGINT;
ALTER TABLE escapes ADD COLUMN updated_by BIGINT;
