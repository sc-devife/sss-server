-- Custom (hotel-only) meal plans: hotel_id set = created for that one hotel and never
-- listed in the library; NULL = global library meal plan. Mirrors services.hotel_id (V76).
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
        WHERE tc.table_name = 'meal_plans'
          AND tc.constraint_type = 'UNIQUE'
          AND kcu.column_name = 'code'
    LOOP
        EXECUTE format('ALTER TABLE meal_plans DROP CONSTRAINT %I', r.constraint_name);
    END LOOP;
END $$;

ALTER TABLE meal_plans ADD COLUMN IF NOT EXISTS hotel_id BIGINT REFERENCES hotels (seqp);
CREATE INDEX IF NOT EXISTS idx_meal_plans_hotel_id ON meal_plans (hotel_id);
-- code stays unique among library plans, and unique within one hotel's own plans.
CREATE UNIQUE INDEX IF NOT EXISTS uq_meal_plans_code_global ON meal_plans (LOWER(code)) WHERE hotel_id IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_meal_plans_code_hotel ON meal_plans (hotel_id, LOWER(code)) WHERE hotel_id IS NOT NULL;
