-- Reusing the pre-existing (previously unfinished/unused) inclusion_exclusion_items
-- table rather than building a new one. Extends it to also cover TERMS (Terms &
-- Conditions), rich HTML content, an optional destination link, and standard audit
-- columns matching every other Library entity.

-- Postgres has no RENAME COLUMN IF EXISTS — guard it explicitly, since this
-- table (like escape_points/leads) predates Flyway tracking and its exact
-- current shape isn't guaranteed. Only rename if the old column is there and
-- the new one isn't yet; the ADD COLUMN IF NOT EXISTS right after covers
-- content_html either way (fresh add if there was never a description to
-- rename from).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inclusion_exclusion_items' AND column_name = 'description')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'inclusion_exclusion_items' AND column_name = 'content_html') THEN
        ALTER TABLE inclusion_exclusion_items RENAME COLUMN description TO content_html;
    END IF;
END $$;

ALTER TABLE inclusion_exclusion_items
    ADD COLUMN IF NOT EXISTS content_html TEXT,
    ADD COLUMN IF NOT EXISTS destination_id BIGINT REFERENCES escape_points (seqp),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

UPDATE inclusion_exclusion_items SET created_at = now(), updated_at = now() WHERE created_at IS NULL;
UPDATE inclusion_exclusion_items SET is_active = true WHERE is_active IS NULL;

ALTER TABLE inclusion_exclusion_items
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT now(),
    ALTER COLUMN is_active SET DEFAULT true;

CREATE INDEX IF NOT EXISTS idx_inclusion_exclusion_items_org_id ON inclusion_exclusion_items (org_id);
CREATE INDEX IF NOT EXISTS idx_inclusion_exclusion_items_destination_id ON inclusion_exclusion_items (destination_id);
