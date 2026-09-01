-- Hotels don't need a link to trip Activities (trekking/scuba/etc) — that
-- was replaced with a "Services" concept instead (Candle Light Dinner, Room
-- Decoration, Honeymoon Setup, Birthday Decoration): hotel-level add-ons,
-- not trip experiences. Drop the now-unwanted join table and add the new
-- master-data table + join table, mirroring room_types/hotel_room_types.

DROP TABLE hotel_activities;

CREATE TABLE services (
    seqp        BIGSERIAL PRIMARY KEY,
    uid         UUID NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP
);

CREATE TABLE hotel_services (
    hotel_id   BIGINT NOT NULL REFERENCES hotels (seqp),
    service_id BIGINT NOT NULL REFERENCES services (seqp),
    PRIMARY KEY (hotel_id, service_id)
);
