-- Seeds the four roles from Section 3 of the spec as platform-level roles
-- (org_id null = usable by any organization), a baseline permission set for
-- the resources that currently have real endpoints, and grants matching
-- Section 3's description: Admin mirrors Super Admin at the org level for
-- now, Lead Assigner is scoped to lead management, Regular User is
-- read-only on the library with full control on their own trips/leads.

INSERT INTO roles (name, label, is_active, is_archived, org_id)
VALUES
    ('SUPER_ADMIN', 'Super Admin', true, false, NULL),
    ('ADMIN', 'Admin', true, false, NULL),
    ('LEAD_ASSIGNER', 'Lead Assigner', true, false, NULL),
    ('REGULAR_USER', 'Regular User', true, false, NULL);

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
    (gen_random_uuid(), 'trips.write', 'trips', 'write', 'Create/edit trips, itineraries, and quotes');

-- Super Admin and Admin: every permission (Admin mirrors Super Admin at the
-- org level today, per spec — narrow later without re-architecting).
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN');

-- Lead Assigner: lead management only, no broad org/user/library access.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'LEAD_ASSIGNER' AND p.key IN ('leads.read', 'leads.assign');

-- Regular User: read-only on library, full control on their own leads/trips.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'REGULAR_USER' AND p.key IN ('library.read', 'leads.read', 'trips.read', 'trips.write');
