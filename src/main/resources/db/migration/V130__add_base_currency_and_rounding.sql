-- Phase 1 of multi-currency: every vendor has a base currency (the currency
-- their books, margins and reports are kept in) and a rounding preference for
-- customer-facing amounts. Until now every amount in the system was INR, so
-- existing vendors are set to INR and nothing about their data changes.
UPDATE organization_settings SET default_currency_code = 'INR'
WHERE default_currency_code IS NULL OR default_currency_code = '';

-- 'decimals' = show minor units (e.g. 1,234.50); 'whole' = round to whole numbers.
ALTER TABLE organization_settings ADD COLUMN rounding_mode VARCHAR(16) NOT NULL DEFAULT 'decimals';

-- Number of minor-unit digits per currency (ISO 4217), used when formatting.
ALTER TABLE supported_currencies ADD COLUMN minor_units SMALLINT NOT NULL DEFAULT 2;
UPDATE supported_currencies SET minor_units = 0 WHERE code IN ('JPY', 'KRW', 'IDR', 'VND', 'CLP', 'ISK', 'UGX');
UPDATE supported_currencies SET minor_units = 3 WHERE code IN ('KWD', 'BHD', 'OMR', 'JOD', 'TND');
