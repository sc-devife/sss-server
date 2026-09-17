-- Records the link between a Dropped hotel booking and the replacement
-- hotel item created for it via the Change/Replace Hotel flow. Nullable and
-- self-referential — most items never replace anything.
ALTER TABLE itinerary_items
    ADD COLUMN replaces_item_id BIGINT REFERENCES itinerary_items(seqp);
