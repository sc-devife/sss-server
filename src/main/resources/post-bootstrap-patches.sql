-- Run this ONCE, after the one-time Hibernate schema bootstrap (ddl-auto=update)
-- and BEFORE dummy-data-seed.sql. Not a Flyway migration — Flyway is baselined
-- past all 42 existing migrations (see README-recovery.md), which means their
-- SQL bodies never actually execute against this schema. This file replays
-- just the two kinds of thing that matters going forward: (1) DB-level column
-- defaults that several @Entity classes rely on because their uid field is
-- marked insertable=false (Hibernate never writes it, only a DB default can),
-- and (2) the pure reference-data INSERTs those same 42 migrations contained
-- (V6/V13 RBAC seed, V21 currencies) — verbatim from those files.
-- Safe to re-run: every ALTER is idempotent, every INSERT is guarded.

-- ── Column defaults Hibernate's ddl-auto never sets (from V5/V7/V21) ──
ALTER TABLE organizations ALTER COLUMN uid SET DEFAULT gen_random_uuid();
ALTER TABLE user_invitations ALTER COLUMN uid SET DEFAULT gen_random_uuid();
ALTER TABLE supported_currencies ALTER COLUMN uid SET DEFAULT gen_random_uuid();
-- These four are also insertable=false on their entities but no migration
-- ever added a default for them either (pre-existing gaps, not something the
-- schema recreation introduced) — needed for the app's own create flows
-- (escape points, users/invites-adjacent, organization addresses, library
-- inclusion/exclusion items) to work; all four are plain varchar columns
-- (String-typed uid fields), not uuid columns, hence the ::text cast
-- (matches what Postgres would assignment-cast implicitly anyway, kept
-- explicit for clarity).
ALTER TABLE escape_points ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
ALTER TABLE users ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
ALTER TABLE organization_address ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;
ALTER TABLE inclusion_exclusion_items ALTER COLUMN uid SET DEFAULT gen_random_uuid()::text;

-- ── V32's defaults (inclusion_exclusion_items) ──
ALTER TABLE inclusion_exclusion_items
    ALTER COLUMN created_at SET DEFAULT now(),
    ALTER COLUMN updated_at SET DEFAULT now(),
    ALTER COLUMN is_active SET DEFAULT true;

-- ── V36 (organization_address.address_type) ──
-- Not just a missing default: this column isn't mapped on the Address
-- entity at all (confirmed by V36's own migration comment), so Hibernate's
-- schema generation never created it either — has to be added outright,
-- not just defaulted.
ALTER TABLE organization_address ADD COLUMN IF NOT EXISTS address_type VARCHAR(255) NOT NULL DEFAULT 'CONTACT';

-- ── V6: RBAC roles + baseline permission set + grants ──
INSERT INTO roles (name, label, is_active, is_archived, org_id)
VALUES
    ('SUPER_ADMIN', 'Super Admin', true, false, NULL),
    ('ADMIN', 'Admin', true, false, NULL),
    ('LEAD_ASSIGNER', 'Lead Assigner', true, false, NULL),
    ('REGULAR_USER', 'Regular User', true, false, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO permissions (id, key, resource, action, description) VALUES
    (gen_random_uuid(), 'organizations.read', 'organizations', 'read', 'View organization profile'),
    (gen_random_uuid(), 'organizations.write', 'organizations', 'write', 'Edit organization profile'),
    (gen_random_uuid(), 'users.read', 'users', 'read', 'View users in the organization'),
    (gen_random_uuid(), 'users.write', 'users', 'write', 'Invite, edit, deactivate users'),
    (gen_random_uuid(), 'roles.manage', 'roles', 'manage', 'Manage roles and permission grants'),
    (gen_random_uuid(), 'bank_accounts.read', 'bank_accounts', 'read', 'View organization bank accounts'),
    (gen_random_uuid(), 'bank_accounts.write', 'bank_accounts', 'write', 'Add/remove organization bank accounts'),
    (gen_random_uuid(), 'library.read', 'library', 'read', 'View library items (hotels, destinations, etc.)'),
    (gen_random_uuid(), 'library.write', 'library', 'write', 'Create/edit library items'),
    (gen_random_uuid(), 'leads.read', 'leads', 'read', 'View leads'),
    (gen_random_uuid(), 'leads.assign', 'leads', 'assign', 'Assign/reassign leads to any user'),
    (gen_random_uuid(), 'trips.read', 'trips', 'read', 'View trips'),
    (gen_random_uuid(), 'trips.write', 'trips', 'write', 'Create/edit trips, itineraries, and quotes')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'LEAD_ASSIGNER' AND p.key IN ('leads.read', 'leads.assign')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'REGULAR_USER' AND p.key IN ('library.read', 'leads.read', 'trips.read', 'trips.write')
ON CONFLICT DO NOTHING;

-- ── V13: leads.write permission ──
INSERT INTO permissions (id, key, resource, action, description)
VALUES (gen_random_uuid(), 'leads.write', 'leads', 'write', 'Create leads and run lead lifecycle actions (contact/qualify/disqualify/convert)')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN', 'REGULAR_USER') AND p.key = 'leads.write'
ON CONFLICT DO NOTHING;

-- ── V21: supported currencies ──
INSERT INTO supported_currencies (code, name, symbol, is_active)
SELECT v.code, v.name, v.symbol, v.is_active FROM (VALUES
    ('USD', 'US Dollar', '$', true),
    ('EUR', 'Euro', '€', true),
    ('GBP', 'British Pound', '£', true),
    ('INR', 'Indian Rupee', '₹', true),
    ('AED', 'UAE Dirham', 'د.إ', true),
    ('SGD', 'Singapore Dollar', 'S$', true),
    ('THB', 'Thai Baht', '฿', true),
    ('IDR', 'Indonesian Rupiah', 'Rp', true),
    ('MYR', 'Malaysian Ringgit', 'RM', true),
    ('AUD', 'Australian Dollar', 'A$', true),
    ('NZD', 'New Zealand Dollar', 'NZ$', true),
    ('CAD', 'Canadian Dollar', 'C$', true),
    ('CHF', 'Swiss Franc', 'CHF', true),
    ('JPY', 'Japanese Yen', '¥', true),
    ('CNY', 'Chinese Yuan', '¥', true),
    ('HKD', 'Hong Kong Dollar', 'HK$', true),
    ('KRW', 'South Korean Won', '₩', true),
    ('VND', 'Vietnamese Dong', '₫', true),
    ('PHP', 'Philippine Peso', '₱', true),
    ('LKR', 'Sri Lankan Rupee', 'Rs', true),
    ('NPR', 'Nepalese Rupee', 'Rs', true),
    ('MVR', 'Maldivian Rufiyaa', 'Rf', true),
    ('SAR', 'Saudi Riyal', '﷼', true),
    ('QAR', 'Qatari Riyal', 'ر.ق', true),
    ('TRY', 'Turkish Lira', '₺', true),
    ('EGP', 'Egyptian Pound', '£', true),
    ('ZAR', 'South African Rand', 'R', true),
    ('MUR', 'Mauritian Rupee', '₨', true),
    ('SCR', 'Seychellois Rupee', '₨', true),
    ('FJD', 'Fijian Dollar', 'FJ$', true),
    ('MXN', 'Mexican Peso', '$', true),
    ('BRL', 'Brazilian Real', 'R$', true)
) AS v(code, name, symbol, is_active)
WHERE NOT EXISTS (SELECT 1 FROM supported_currencies s WHERE s.code = v.code);
