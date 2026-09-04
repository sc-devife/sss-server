-- A hotel booking covers multiple consecutive nights, not a single
-- itinerary day — this is the number of nights the stay spans, starting
-- from the itinerary item's own day_number (its check-in day). Check-out
-- day/date is always derived (day_number + nights), never stored
-- separately, so it can never drift out of sync.
ALTER TABLE itinerary_item_hotel_details
    ADD COLUMN nights INTEGER NOT NULL DEFAULT 1;
