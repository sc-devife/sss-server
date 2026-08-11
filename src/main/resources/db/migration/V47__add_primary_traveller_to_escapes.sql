-- Escapes have no existing way to identify which of their travellers is the
-- lead's original/primary contact (Lead has no FK to Traveller, and
-- escape_traveller is a flat, unordered join table). This column is set
-- explicitly at escape-creation time (the first traveller uid submitted in
-- EscapeCreateRequestDTO.travellerUids) rather than inferred from array
-- order at read time. ON DELETE SET NULL since TravellerHelper.deleteTraveller
-- is a hard delete with no in-use check today.
ALTER TABLE escapes ADD COLUMN primary_traveller_uid UUID NULL REFERENCES traveller(uid) ON DELETE SET NULL;
