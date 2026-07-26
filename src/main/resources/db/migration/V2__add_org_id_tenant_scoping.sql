-- Tenant isolation foundation: every org-scoped table gets a real org_id FK
-- (indexed, per the multi-tenancy convention), replacing the old ad hoc
-- company_id columns (which were never wired to a real FK and mostly left
-- null) and the tables that had no tenant column at all.
--
-- org_id is left NULLABLE here deliberately: nothing yet resolves "the
-- current user's organization" from a request (that lands with the auth
-- re-enablement work), so making it NOT NULL now would break every existing
-- create flow. A follow-up migration tightens these to NOT NULL once the
-- application actually populates them.
--
-- terms_condition_items is intentionally skipped — it has no corresponding
-- entity in the current codebase (created by a stale branch), so it isn't
-- touched until someone confirms what owns it.

ALTER TABLE users ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_users_org_id ON users (org_id);
ALTER TABLE users DROP COLUMN company_id;

-- Nullable by definition: null org_id = platform-level role, visible across all orgs.
ALTER TABLE roles ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_roles_org_id ON roles (org_id);

ALTER TABLE leads ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_leads_org_id ON leads (org_id);

ALTER TABLE escapes ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_escapes_org_id ON escapes (org_id);

ALTER TABLE escape_points ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_escape_points_org_id ON escape_points (org_id);
ALTER TABLE escape_points DROP COLUMN company_id;

ALTER TABLE hotels ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_hotels_org_id ON hotels (org_id);

ALTER TABLE locations ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_locations_org_id ON locations (org_id);

ALTER TABLE traveller ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_traveller_org_id ON traveller (org_id);

ALTER TABLE escape_sources ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_escape_sources_org_id ON escape_sources (org_id);

ALTER TABLE user_invitations ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_user_invitations_org_id ON user_invitations (org_id);

ALTER TABLE inclusion_exclusion_items ADD COLUMN org_id BIGINT REFERENCES organizations (seqp);
CREATE INDEX idx_inclusion_exclusion_items_org_id ON inclusion_exclusion_items (org_id);
ALTER TABLE inclusion_exclusion_items DROP COLUMN company_id;
