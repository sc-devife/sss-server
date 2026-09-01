-- Defensive follow-up to V76: drops whatever the actual unique constraint
-- on services.name is named, in case it didn't match the guessed
-- "services_name_key" default-naming assumption. Idempotent either way — a
-- hotel-scoped service is only unique within the set it's visible in
-- (enforced in the application layer), never globally across every hotel.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
        WHERE tc.table_name = 'services'
          AND tc.constraint_type = 'UNIQUE'
          AND kcu.column_name = 'name'
    LOOP
        EXECUTE format('ALTER TABLE services DROP CONSTRAINT %I', r.constraint_name);
    END LOOP;
END $$;

ALTER TABLE services ADD COLUMN IF NOT EXISTS hotel_id BIGINT REFERENCES hotels (seqp);

CREATE INDEX IF NOT EXISTS idx_services_hotel_id ON services (hotel_id);
