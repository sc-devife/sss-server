-- Section 15/16: Itinerary -> many ItineraryItem (day-by-day line items).
-- reference_id is a polymorphic pointer at Hotel/Activity/Transport's uid
-- depending on item_type — same "no single FK possible across three
-- tables" pattern as AuditLog's entity_type+entity_id, resolved at the
-- application layer rather than a DB constraint.
CREATE TABLE itinerary_items (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    itinerary_id BIGINT NOT NULL REFERENCES itineraries (seqp) ON DELETE CASCADE,
    day_number INTEGER NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    reference_id UUID NOT NULL,
    notes TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_itinerary_items_itinerary_id ON itinerary_items (itinerary_id);
