-- Reusing the pre-existing (previously unfinished/unused) inclusion_exclusion_items
-- table rather than building a new one. Extends it to also cover TERMS (Terms &
-- Conditions), rich HTML content, an optional destination link, and standard audit
-- columns matching every other Library entity.

ALTER TABLE inclusion_exclusion_items RENAME COLUMN description TO content_html;

ALTER TABLE inclusion_exclusion_items
    ADD COLUMN destination_id BIGINT REFERENCES escape_points (seqp),
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;

UPDATE inclusion_exclusion_items SET created_at = now(), updated_at = now() WHERE created_at IS NULL;
UPDATE inclusion_exclusion_items SET is_active = true WHERE is_active IS NULL;

ALTER TABLE inclusion_exclusion_items
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT now(),
    ALTER COLUMN is_active SET DEFAULT true;

CREATE INDEX idx_inclusion_exclusion_items_org_id ON inclusion_exclusion_items (org_id);
CREATE INDEX idx_inclusion_exclusion_items_destination_id ON inclusion_exclusion_items (destination_id);
