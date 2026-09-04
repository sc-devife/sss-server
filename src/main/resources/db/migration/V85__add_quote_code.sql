-- Human-readable quote code for download filenames (e.g. "QUOTE-00001"),
-- computed from the existing identity seqp — mirrors Escape.trip_code (see
-- V82). No new sequence/counter: reuses the row's own existing seqp so it
-- can never drift out of sync or collide.
ALTER TABLE quotes
    ADD COLUMN quote_code TEXT GENERATED ALWAYS AS ('QUOTE-' || lpad(seqp::text, 5, '0')) STORED;
