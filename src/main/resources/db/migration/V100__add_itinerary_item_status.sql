-- Generalizes the Initialize/Booked/Drop lifecycle (already on
-- itinerary_item_hotel_details) to the item level, for item types that
-- don't have their own dedicated detail table — currently Activity.
ALTER TABLE itinerary_items
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'Initialize',
    ADD COLUMN dropping_reason TEXT,
    ADD COLUMN cancellation_charge_inr NUMERIC(12,2);
