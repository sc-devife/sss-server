-- ID-consistency fix: these tables had no external-facing `uid` at all, so
-- their internal auto-increment `seqp` was leaking directly through API
-- routes/DTOs/frontend URLs (e.g. /escapes/1) -- an IDOR/enumeration risk in
-- a multi-tenant SaaS. Add a proper uid on each, matching the pattern
-- already used by hotels/deals/etc (native uuid, gen_random_uuid() default).
-- Backend route/DTO changes to actually start using these lands separately.

ALTER TABLE escapes ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE escapes ADD CONSTRAINT uk_escapes_uid UNIQUE (uid);

ALTER TABLE leads ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE leads ADD CONSTRAINT uk_leads_uid UNIQUE (uid);

ALTER TABLE traveller ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE traveller ADD CONSTRAINT uk_traveller_uid UNIQUE (uid);

ALTER TABLE escape_sources ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE escape_sources ADD CONSTRAINT uk_escape_sources_uid UNIQUE (uid);

ALTER TABLE lead_import_attempts ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE lead_import_attempts ADD CONSTRAINT uk_lead_import_attempts_uid UNIQUE (uid);

ALTER TABLE organization_bank_accounts ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE organization_bank_accounts ADD CONSTRAINT uk_organization_bank_accounts_uid UNIQUE (uid);
