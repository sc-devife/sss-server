-- Notifications are always scoped to "your own" — never a privileged view —
-- so every role gets both permissions, unlike most other resource.action pairs.
INSERT INTO permissions (id, key, resource, action, description)
VALUES
    (gen_random_uuid(), 'notifications.read', 'notifications', 'read', 'View your own notifications'),
    (gen_random_uuid(), 'notifications.write', 'notifications', 'write', 'Mark your own notifications as read');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.seqp, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ADMIN', 'LEAD_ASSIGNER', 'REGULAR_USER') AND p.key IN ('notifications.read', 'notifications.write');
