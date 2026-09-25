-- The Currency entity maps minor_units to an Integer (INTEGER); V130 created it as SMALLINT,
-- which Hibernate's schema validation rejects. V130 has already run, so fix it forward.
ALTER TABLE supported_currencies ALTER COLUMN minor_units TYPE INTEGER;
