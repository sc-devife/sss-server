-- escape_points is the canonical Destination. `destinations` was a thinner, newer
-- duplicate of the same concept and is retired here in favour of escape_points.
--
-- locations remains the precise physical placement of a library item (a Hotel's
-- actual city, not the destination it's grouped under for planning purposes — a
-- hotel can serve an escape_point without being physically inside it, e.g. a
-- stopover on the way there). It now also carries currency/timezone, since those
-- should reflect where the item actually is, not the destination's representative
-- default.

ALTER TABLE locations
    ADD COLUMN currency_code VARCHAR(255),
    ADD COLUMN time_zone VARCHAR(255);

CREATE TABLE hotel_escape_points (
    hotel_id        BIGINT NOT NULL REFERENCES hotels (seqp),
    escape_point_id BIGINT NOT NULL REFERENCES escape_points (seqp),
    PRIMARY KEY (hotel_id, escape_point_id)
);

-- No production data exists in these yet (fresh instance) — a straight drop is
-- safe. If real rows ever land here before this ships, back them up first.
DROP TABLE IF EXISTS hotel_destinations;
DROP TABLE IF EXISTS destinations;
