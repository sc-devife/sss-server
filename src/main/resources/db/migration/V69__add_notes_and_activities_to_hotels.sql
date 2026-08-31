ALTER TABLE hotels ADD COLUMN notes TEXT;

CREATE TABLE hotel_activities (
    hotel_id    BIGINT NOT NULL REFERENCES hotels (seqp),
    activity_id BIGINT NOT NULL REFERENCES activities (seqp),
    PRIMARY KEY (hotel_id, activity_id)
);
