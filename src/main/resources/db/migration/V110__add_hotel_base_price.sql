-- Hotel was the one library entity with no price of its own (Transport and
-- Activity already have base_price) — needed so the itinerary's hotel
-- suggestion dropdown can show a real per-hotel starting price instead of
-- nothing.
ALTER TABLE hotels ADD COLUMN base_price NUMERIC(12, 2);
