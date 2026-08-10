-- Part 1: uid columns that exist but were never actually written -- no DB
-- default, no @PrePersist, nothing writing them anywhere -- so every row's
-- uid has always been NULL. Give them the same default-generation treatment
-- the rest of the codebase relies on.

ALTER TABLE user_credentials ALTER COLUMN uid SET DEFAULT gen_random_uuid();
UPDATE user_credentials SET uid = gen_random_uuid() WHERE uid IS NULL;

ALTER TABLE user_role_links ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE user_role_links SET uid = gen_random_uuid()::text WHERE uid IS NULL;

ALTER TABLE roles ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE roles SET uid = gen_random_uuid()::text WHERE uid IS NULL;

-- Part 2: uid columns that predate the V1 Flyway baseline (insertable=false,
-- relying purely on a DB default that no tracked migration ever set). One of
-- these (organizations) already turned out to be missing its default and was
-- fixed in V5 -- these four were patched by hand in post-bootstrap-patches.sql
-- during the last DB recovery, but that was never captured in Flyway history,
-- so a fresh environment (new dev, CI, another region move) would silently
-- hit the same bug. Idempotent -- safe to re-run even where the default
-- already secretly exists.

ALTER TABLE users ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE users SET uid = gen_random_uuid()::text WHERE uid IS NULL;

ALTER TABLE escape_points ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE escape_points SET uid = gen_random_uuid()::text WHERE uid IS NULL;

ALTER TABLE organization_address ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE organization_address SET uid = gen_random_uuid()::text WHERE uid IS NULL;

ALTER TABLE inclusion_exclusion_items ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
UPDATE inclusion_exclusion_items SET uid = gen_random_uuid()::text WHERE uid IS NULL;
