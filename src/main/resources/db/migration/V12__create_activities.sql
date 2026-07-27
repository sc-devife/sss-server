-- Section 15 data dictionary: Activity (Library). Built fresh. destination_id
-- references escape_points, same single-FK pattern used for Hotel (V9).
CREATE TABLE activities (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    name VARCHAR(255) NOT NULL,
    destination_id BIGINT REFERENCES escape_points (seqp),
    category_code VARCHAR(20),
    duration_minutes INTEGER,
    description TEXT,
    images TEXT[],
    base_price NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_activities_org_id ON activities (org_id);
