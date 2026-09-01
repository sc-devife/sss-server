CREATE TABLE itinerary_item_transport_details (
    itinerary_item_id      BIGINT PRIMARY KEY REFERENCES itinerary_items (seqp),
    mode_code              VARCHAR(255),
    vehicle_type_code      VARCHAR(255),
    price                  NUMERIC(12, 2),
    trip_type              VARCHAR(255),
    cost_price              NUMERIC(12, 2),
    cost_price_per_person   BOOLEAN,
    selling_price           NUMERIC(12, 2),
    selling_price_per_person BOOLEAN,
    adults_count            INTEGER,
    children_count          INTEGER,
    infants_count            INTEGER,
    additional_options       TEXT
);

CREATE TABLE itinerary_item_transport_legs (
    seqp                BIGSERIAL PRIMARY KEY,
    itinerary_item_id   BIGINT NOT NULL REFERENCES itinerary_items (seqp),
    leg_order           INTEGER NOT NULL,
    direction           VARCHAR(255),
    departure_airport   VARCHAR(255),
    departure_terminal  VARCHAR(255),
    departure_time      TIMESTAMP,
    arrival_airport     VARCHAR(255),
    arrival_terminal    VARCHAR(255),
    arrival_time        TIMESTAMP,
    flight_number       VARCHAR(255)
);

CREATE INDEX idx_itinerary_item_transport_legs_item ON itinerary_item_transport_legs (itinerary_item_id);
