-- Ties a Service Provider to a destination, same "destination_id -> escape_points(seqp)"
-- FK already used by hotels/transports/activities/inclusion_exclusion_items.
-- Nullable: existing providers aren't scoped to any destination yet.
ALTER TABLE service_providers
    ADD COLUMN destination_id BIGINT REFERENCES escape_points (seqp);
