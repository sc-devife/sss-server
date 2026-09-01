ALTER TABLE itinerary_item_hotel_inclusions
    DROP COLUMN night,
    ADD COLUMN start_time TIME,
    ADD COLUMN duration_minutes INTEGER;
