CREATE TABLE hotel_payments (
    seqp             BIGSERIAL PRIMARY KEY,
    uid              UUID NOT NULL UNIQUE,
    org_id           BIGINT,
    hotel_id         BIGINT NOT NULL REFERENCES hotels (seqp),
    escape_id        BIGINT NOT NULL REFERENCES escapes (seqp),
    transaction_id   VARCHAR(255),
    payment_method   VARCHAR(50) NOT NULL,
    amount           NUMERIC(14, 2) NOT NULL,
    paid_by          VARCHAR(255),
    payment_date     DATE NOT NULL,
    notes            TEXT,
    status           VARCHAR(20) NOT NULL DEFAULT 'Paid',
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    created_by       BIGINT,
    updated_by       BIGINT
);

CREATE INDEX idx_hotel_payments_hotel_id ON hotel_payments (hotel_id);
