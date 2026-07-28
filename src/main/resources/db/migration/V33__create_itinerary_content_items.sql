-- Terms/Inclusions/Exclusions attached to a specific Itinerary. Library items
-- are COPIED in at attach time (name + content_html frozen into this row) so
-- later edits or deactivation of the library source never change a trip's
-- already-built itinerary — same "snapshot, don't reference" pattern as Quote
-- pricing. source_item_id is kept purely for traceability. Ad-hoc, trip-only
-- additions (no library backing) simply leave source_item_id null.
CREATE TABLE itinerary_content_items (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    itinerary_id BIGINT NOT NULL REFERENCES itineraries (seqp),
    type VARCHAR(20) NOT NULL,
    source_item_id BIGINT REFERENCES inclusion_exclusion_items (seqp),
    name VARCHAR(255) NOT NULL,
    content_html TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_itinerary_content_items_itinerary_id ON itinerary_content_items (itinerary_id);
