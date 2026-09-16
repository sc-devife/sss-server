-- Public "Book a Demo" submissions from the marketing site — deliberately
-- has NO org_id: these are prospects who don't belong to any tenant
-- organization yet, unlike every other table in this schema.
CREATE TABLE demo_bookings (
    seqp                  BIGSERIAL PRIMARY KEY,
    uid                   UUID NOT NULL UNIQUE,

    demo_date             DATE NOT NULL,
    demo_time             TIME NOT NULL,
    languages             TEXT[] NOT NULL,

    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    contact_number        VARCHAR(32) NOT NULL,
    guest_emails          TEXT[],

    company_name          VARCHAR(255) NOT NULL,
    employee_count        VARCHAR(32) NOT NULL,
    agency_type           VARCHAR(64) NOT NULL,
    destinations          TEXT NOT NULL,
    automation_processes  TEXT[] NOT NULL,
    office_location       VARCHAR(255) NOT NULL,
    referral_source       VARCHAR(64) NOT NULL,

    created_at            TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_demo_bookings_demo_date ON demo_bookings (demo_date);
