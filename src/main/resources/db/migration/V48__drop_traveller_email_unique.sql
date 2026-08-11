-- Traveller.email no longer needs to be unique org-wide: a duplicate-email
-- check made it impossible to re-collect a traveller's details on a second
-- escape (or re-add one after being detached from an escape) whenever that
-- email was already used by any other traveller record. Drops whatever the
-- unique constraint was actually named (originates from the entity's old
-- @Column(unique = true), auto-named by the Hibernate bootstrap) rather than
-- assuming a specific name.
DO $$
DECLARE
    uq_name text;
BEGIN
    SELECT con.conname INTO uq_name
    FROM pg_constraint con
    JOIN pg_class rel ON rel.oid = con.conrelid
    JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey)
    WHERE rel.relname = 'traveller' AND att.attname = 'email' AND con.contype = 'u';

    IF uq_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE traveller DROP CONSTRAINT %I', uq_name);
    END IF;
END $$;
