ALTER TABLE itinerary_item_hotel_details
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'Initialize',
    ADD COLUMN dropping_reason TEXT,
    ADD COLUMN cancellation_charge_inr NUMERIC(12,2);

ALTER TABLE quotes
    ADD COLUMN cancellation_charges_inr NUMERIC(14,2);
