-- Assignment moves from Lead to Escape — it's decided once, at conversion
-- time, by the assignment engine, not carried over from whoever worked the
-- lead.
ALTER TABLE escapes ADD COLUMN assigned_to_user_id BIGINT;
ALTER TABLE escapes ADD COLUMN assignment_reason VARCHAR(255);

-- EscapeSource removed entirely: Escape.source duplicated what Lead already
-- tracks (see the new leads.source_type/source_channel + lead_agency_details
-- in V62) — Escape already has a real FK to Lead, so the source now flows
-- transitively through that instead of being stored twice.
ALTER TABLE escapes DROP COLUMN source_id;
DROP TABLE IF EXISTS escape_source_b2b_details;
DROP TABLE IF EXISTS trip_source_direct_details;
DROP TABLE IF EXISTS escape_sources;
