-- Day-by-day trip planner upgrade: itinerary_items gains a free-text title
-- (paired with a now-optional reference_id, same "ad-hoc allowed" pattern
-- itinerary_content_items already uses) and an optional time-of-day.
-- item_type expands from 3 values to 8 in application code only --
-- item_type is already VARCHAR(20), which fits the longest new value
-- ("pickup_drop" = 11 chars), so no column width change is needed.

ALTER TABLE itinerary_items ADD COLUMN start_time TIME NULL;
ALTER TABLE itinerary_items ADD COLUMN title VARCHAR(255) NULL;
ALTER TABLE itinerary_items ALTER COLUMN reference_id DROP NOT NULL;
