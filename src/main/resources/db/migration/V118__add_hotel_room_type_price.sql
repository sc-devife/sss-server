-- hotel_room_types was a plain (hotel_id, room_type_id) composite-PK join
-- table. Each pairing now needs its own price/night, so it becomes a real
-- entity table: add a surrogate seqp PK + price, keep the old pair unique.
DO $$
DECLARE
    pk_name text;
BEGIN
    SELECT tc.constraint_name INTO pk_name
    FROM information_schema.table_constraints tc
    WHERE tc.table_name = 'hotel_room_types' AND tc.constraint_type = 'PRIMARY KEY';
    IF pk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE hotel_room_types DROP CONSTRAINT %I', pk_name);
    END IF;
END $$;

ALTER TABLE hotel_room_types ADD COLUMN seqp BIGSERIAL;
ALTER TABLE hotel_room_types ADD PRIMARY KEY (seqp);
ALTER TABLE hotel_room_types ADD CONSTRAINT uq_hotel_room_types_pair UNIQUE (hotel_id, room_type_id);
ALTER TABLE hotel_room_types ADD COLUMN price NUMERIC(12, 2);
