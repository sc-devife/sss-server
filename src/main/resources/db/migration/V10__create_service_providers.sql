-- Section 15 data dictionary: Service Provider (Library). Built fresh — no
-- prior table existed for this concept.
CREATE TABLE service_providers (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    name VARCHAR(255) NOT NULL,
    type_code VARCHAR(20) NOT NULL,
    contact_info TEXT,
    country_code VARCHAR(4),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_service_providers_org_id ON service_providers (org_id);
