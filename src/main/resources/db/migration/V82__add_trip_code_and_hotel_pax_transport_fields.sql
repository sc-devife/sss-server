-- Human-readable trip code for quotation headers/watermarks (e.g. "TRP-000123"),
-- computed from the existing identity seqp so no backfill/generation logic is
-- needed in the application layer — every existing and future row gets one
-- automatically and it can never drift out of sync with seqp.
ALTER TABLE escapes
    ADD COLUMN trip_code TEXT GENERATED ALWAYS AS ('TRP-' || lpad(seqp::text, 6, '0')) STORED;
