-- V6 seeded leads.read (view) and leads.assign (Lead Assigner's job) but
-- nothing covers creating a lead or running its lifecycle actions
-- (contact/qualify/disqualify/convert) — distinct from assignment per
-- Section 3: Lead Assigner's scope is narrowly assignment, while Regular
-- Users "act on" their leads via these actions without hand-editing fields.
INSERT INTO permissions (id, key, resource, action, description)
VALUES (gen_random_uuid(), 'leads.write', 'leads', 'write', 'Create leads and run lead lifecycle actions (contact/qualify/disqualify/convert)');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN', 'REGULAR_USER') AND p.key = 'leads.write';
