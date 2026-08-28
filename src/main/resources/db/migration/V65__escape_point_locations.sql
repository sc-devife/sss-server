-- EscapePoint/Location metadata pass: a destination (EscapePoint) covers a
-- set of cities (Location) rather than a single free-text city/region/country
-- — same "list of cities" relation already agreed for Hotel selection. Drop
-- the old singular free-text/coded fields (city/province/country/region,
-- country_code/region_code/city_code) in favor of a many-to-many join with an
-- is_primary flag for the "headline" display city — same pattern as
-- address_constraints.is_primary.
CREATE TABLE escape_point_locations (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    escape_point_id BIGINT NOT NULL REFERENCES escape_points (seqp),
    location_id BIGINT NOT NULL REFERENCES locations (seqp),
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (escape_point_id, location_id)
);

CREATE INDEX idx_escape_point_locations_escape_point_id ON escape_point_locations (escape_point_id);
CREATE INDEX idx_escape_point_locations_location_id ON escape_point_locations (location_id);

ALTER TABLE escape_points DROP COLUMN city;
ALTER TABLE escape_points DROP COLUMN province;
ALTER TABLE escape_points DROP COLUMN country;
ALTER TABLE escape_points DROP COLUMN region;
ALTER TABLE escape_points DROP COLUMN country_code;
ALTER TABLE escape_points DROP COLUMN region_code;
ALTER TABLE escape_points DROP COLUMN city_code;
