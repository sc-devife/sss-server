-- Aligns hotels with the data dictionary (Section 15): adds a single
-- destination_id FK (dictionary wants Hotel -> one Destination) alongside
-- the existing hotel_escape_points many-to-many table, which is left in
-- place rather than dropped. Backfills destination_id only where a hotel
-- has exactly one linked escape_point via the old M2M table — for hotels
-- linked to more than one, there's no principled way to pick "the" one
-- automatically, so destination_id stays null for those and needs a manual
-- pick (flagged, not silently guessed).
ALTER TABLE hotels
    ADD COLUMN destination_id BIGINT REFERENCES escape_points (seqp),
    ADD COLUMN address TEXT,
    ADD COLUMN contact_info TEXT,
    ADD COLUMN images TEXT[],
    ADD COLUMN amenities TEXT[],
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active',
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN updated_by BIGINT;

UPDATE hotels h
SET destination_id = sub.escape_point_id
FROM (
    SELECT hep.hotel_id, MIN(hep.escape_point_id) AS escape_point_id
    FROM hotel_escape_points hep
    GROUP BY hep.hotel_id
    HAVING COUNT(*) = 1
) sub
WHERE h.seqp = sub.hotel_id;
