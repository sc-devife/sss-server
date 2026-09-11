-- Follow-up / Task Management (Section: Follow-ups) — same read/write split
-- and role grants as leads/trips (V13__add_leads_write_permission.sql).
INSERT INTO permissions (id, key, resource, action, description)
VALUES
    (gen_random_uuid(), 'followups.read', 'followups', 'read', 'View follow-ups/tasks'),
    (gen_random_uuid(), 'followups.write', 'followups', 'write', 'Create/edit/assign follow-ups and update their status');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN', 'REGULAR_USER') AND p.key IN ('followups.read', 'followups.write');
