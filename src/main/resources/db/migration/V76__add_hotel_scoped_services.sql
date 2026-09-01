ALTER TABLE services DROP CONSTRAINT IF EXISTS services_name_key;

ALTER TABLE services ADD COLUMN hotel_id BIGINT REFERENCES hotels (seqp);

CREATE INDEX idx_services_hotel_id ON services (hotel_id);
