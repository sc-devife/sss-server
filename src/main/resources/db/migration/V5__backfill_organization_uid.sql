-- organizations.uid was never given a DB default (unlike its sibling tables),
-- so every row — including the pre-existing org — has uid = null. That silently
-- breaks any uid-based lookup (GET /organizations/{uid}). Give it the same
-- gen_random_uuid() default the other tables already use, and backfill existing rows.

ALTER TABLE organizations ALTER COLUMN uid SET DEFAULT gen_random_uuid();
UPDATE organizations SET uid = gen_random_uuid() WHERE uid IS NULL;
