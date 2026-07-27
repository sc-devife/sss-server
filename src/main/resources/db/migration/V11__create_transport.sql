-- Section 15 data dictionary: Transport (Library). Built fresh. provider_id
-- references the service_providers table created in V10.
CREATE TABLE transports (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    mode_code VARCHAR(20) NOT NULL,
    vehicle_type_code VARCHAR(20),
    capacity INTEGER,
    provider_id BIGINT REFERENCES service_providers (seqp),
    base_price NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_transports_org_id ON transports (org_id);
