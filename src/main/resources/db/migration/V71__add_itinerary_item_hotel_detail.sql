CREATE TABLE itinerary_item_hotel_details (
    itinerary_item_id         BIGINT PRIMARY KEY REFERENCES itinerary_items (seqp),
    meal_plan_id               BIGINT REFERENCES meal_plans (seqp),
    room_type_id                BIGINT REFERENCES room_types (seqp),
    pax_per_room                 INTEGER,
    room_count                    INTEGER,
    adults_with_extra_bed          INTEGER,
    children_with_extra_bed         INTEGER,
    children_no_bed                  INTEGER,
    complimentary_child_count         INTEGER,
    price                              NUMERIC(12, 2),
    total_price                         NUMERIC(12, 2)
);

CREATE TABLE itinerary_item_hotel_inclusions (
    seqp                BIGSERIAL PRIMARY KEY,
    itinerary_item_id   BIGINT NOT NULL REFERENCES itinerary_items (seqp),
    service              VARCHAR(255),
    night                VARCHAR(255),
    total_price          NUMERIC(12, 2),
    comments             TEXT
);

CREATE INDEX idx_itinerary_item_hotel_inclusions_item ON itinerary_item_hotel_inclusions (itinerary_item_id);
