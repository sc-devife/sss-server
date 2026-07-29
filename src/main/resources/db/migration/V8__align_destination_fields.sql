-- Aligns escape_points (the canonical Destination table, per V1) with the
-- data dictionary (Section 15): adds code-based country/region/city columns
-- per the Section 14 store-code/resolve-label-at-render convention, plus
-- description/images/status/soft-delete/audit columns that Library entities
-- need generally (Section 16). The existing free-text country/province/city
-- columns are left in place rather than dropped/renamed — they hold
-- pre-existing historical data and nothing here guarantees a clean 1:1
-- mapping from a free-text value to an ISO code, so backfilling them
-- automatically risks silently wrong codes. New rows should populate the
-- *_code columns going forward; reconciling old rows is a data-cleanup task,
-- not a schema one.

-- IF NOT EXISTS on every clause: this table already had created_at/updated_at/
-- created_by/updated_by from an out-of-band change made before Flyway's
-- history caught up (this ran against the real DB for the first time with
-- the schema at version 7, and errored on the first already-existing column
-- inside this multi-column ALTER TABLE, atomically rolling back the whole
-- statement — see Postgres transactional DDL). Idempotent per-column adds so
-- this only creates what's genuinely still missing.
ALTER TABLE escape_points
    ADD COLUMN IF NOT EXISTS country_code VARCHAR(4),
    ADD COLUMN IF NOT EXISTS region_code VARCHAR(10),
    ADD COLUMN IF NOT EXISTS city_code VARCHAR(160),
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS images TEXT[],
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'active',
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;
