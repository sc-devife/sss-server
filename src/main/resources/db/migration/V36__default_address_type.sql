-- The live organization_address table has an address_type NOT NULL column
-- that isn't mapped on the Address entity at all (that concept lives on the
-- separate address_constraints join table in the current code) — Hibernate's
-- generated INSERT never references it, so the NOT NULL constraint always
-- fails. Giving it a default lets Postgres fill it in for any INSERT that
-- omits the column entirely, which is exactly what Hibernate does here.
ALTER TABLE organization_address
    ALTER COLUMN address_type SET DEFAULT 'CONTACT';
