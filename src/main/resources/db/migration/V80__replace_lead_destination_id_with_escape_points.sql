-- Leads move from a single library-linked Escape Point (destination_id,
-- added in V14) to many, mirroring the Escape/Hotel many-to-many pattern
-- (escape_destination / hotel_escape_points) rather than introducing a
-- third naming convention.
CREATE TABLE lead_escape_points (
    lead_id BIGINT NOT NULL REFERENCES leads (seqp) ON DELETE CASCADE,
    escape_point_id BIGINT NOT NULL REFERENCES escape_points (seqp) ON DELETE CASCADE,
    PRIMARY KEY (lead_id, escape_point_id)
);

-- Carry forward any existing single selection so already-converted/qualified
-- leads don't silently lose their escape point.
INSERT INTO lead_escape_points (lead_id, escape_point_id)
SELECT seqp, destination_id FROM leads WHERE destination_id IS NOT NULL;

ALTER TABLE leads DROP COLUMN destination_id;
