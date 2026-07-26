-- Policy-based RBAC foundation: permissions are granted to roles via
-- role_permissions, rather than checking resource/action directly in
-- business logic, so new roles/permissions can be added via data, not code.
-- Enforcement (actually checking these at request time) lands with the
-- auth re-enablement work, once a real authenticated principal exists.

CREATE TABLE permissions (
    id          UUID PRIMARY KEY,
    key         VARCHAR(255) NOT NULL UNIQUE,
    resource    VARCHAR(255) NOT NULL,
    action      VARCHAR(255) NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles (seqp),
    permission_id UUID   NOT NULL REFERENCES permissions (id),
    PRIMARY KEY (role_id, permission_id)
);
