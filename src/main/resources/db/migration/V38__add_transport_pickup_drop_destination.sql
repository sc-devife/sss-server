ALTER TABLE transports
    ADD COLUMN IF NOT EXISTS pickup_location VARCHAR(255),
    ADD COLUMN IF NOT EXISTS drop_location VARCHAR(255),
    ADD COLUMN IF NOT EXISTS destination_id BIGINT REFERENCES escape_points (seqp);
