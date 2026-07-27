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

ALTER TABLE escape_points
    ADD COLUMN country_code VARCHAR(4),
    ADD COLUMN region_code VARCHAR(10),
    ADD COLUMN city_code VARCHAR(160),
    ADD COLUMN description TEXT,
    ADD COLUMN images TEXT[],
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active',
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now(),
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;
