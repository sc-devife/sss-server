-- When a quote's PDF was last generated, and a fingerprint of everything that
-- goes into it at that moment. Comparing the stored fingerprint to a fresh one
-- tells whether the quote changed after it was generated (so "send" can warn
-- that the generated copy is out of date).
ALTER TABLE quotes ADD COLUMN generated_at TIMESTAMP;
ALTER TABLE quotes ADD COLUMN generated_fingerprint VARCHAR(64);
