-- Amenities become dynamic master data instead of a hardcoded frontend
-- list, mirroring services/room_types/meal_plans — but always global (no
-- hotel_id scope column) since a newly-added amenity must be immediately
-- reusable by every hotel, not just the one it was added from.

CREATE TABLE amenities (
    seqp       BIGSERIAL PRIMARY KEY,
    uid        UUID NOT NULL UNIQUE,
    name       VARCHAR(255) NOT NULL UNIQUE,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

INSERT INTO amenities (uid, name) VALUES
    (gen_random_uuid(), 'Wi-Fi'),
    (gen_random_uuid(), 'Pool'),
    (gen_random_uuid(), 'Parking'),
    (gen_random_uuid(), 'Gym'),
    (gen_random_uuid(), 'Spa'),
    (gen_random_uuid(), 'Restaurant'),
    (gen_random_uuid(), 'Air Conditioning'),
    (gen_random_uuid(), 'Breakfast Included');
