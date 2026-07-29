-- Reuses the pre-existing (but never-seeded, never-migrated) Currency
-- entity/supported_currencies table — matches the user's Excel "Currencies
-- > System Supported Currencies" module exactly. GET /system/currencies
-- previously 404'd since the table was always empty.
-- Defensive: ensure uid has a generation default, same as every other
-- pre-Flyway-baseline table's uid column, in case this one was missed.
ALTER TABLE supported_currencies ALTER COLUMN uid SET DEFAULT gen_random_uuid();

-- WHERE NOT EXISTS rather than ON CONFLICT: the legacy table's `code` column
-- has no confirmed unique/exclusion constraint to target, so ON CONFLICT
-- could itself error. This is constraint-agnostic and safe to re-run.
INSERT INTO supported_currencies (code, name, symbol, is_active)
SELECT v.code, v.name, v.symbol, v.is_active FROM (VALUES
    ('USD', 'US Dollar', '$', true),
    ('EUR', 'Euro', '€', true),
    ('GBP', 'British Pound', '£', true),
    ('INR', 'Indian Rupee', '₹', true),
    ('AED', 'UAE Dirham', 'د.إ', true),
    ('SGD', 'Singapore Dollar', 'S$', true),
    ('THB', 'Thai Baht', '฿', true),
    ('IDR', 'Indonesian Rupiah', 'Rp', true),
    ('MYR', 'Malaysian Ringgit', 'RM', true),
    ('AUD', 'Australian Dollar', 'A$', true),
    ('NZD', 'New Zealand Dollar', 'NZ$', true),
    ('CAD', 'Canadian Dollar', 'C$', true),
    ('CHF', 'Swiss Franc', 'CHF', true),
    ('JPY', 'Japanese Yen', '¥', true),
    ('CNY', 'Chinese Yuan', '¥', true),
    ('HKD', 'Hong Kong Dollar', 'HK$', true),
    ('KRW', 'South Korean Won', '₩', true),
    ('VND', 'Vietnamese Dong', '₫', true),
    ('PHP', 'Philippine Peso', '₱', true),
    ('LKR', 'Sri Lankan Rupee', 'Rs', true),
    ('NPR', 'Nepalese Rupee', 'Rs', true),
    ('MVR', 'Maldivian Rufiyaa', 'Rf', true),
    ('SAR', 'Saudi Riyal', '﷼', true),
    ('QAR', 'Qatari Riyal', 'ر.ق', true),
    ('TRY', 'Turkish Lira', '₺', true),
    ('EGP', 'Egyptian Pound', '£', true),
    ('ZAR', 'South African Rand', 'R', true),
    ('MUR', 'Mauritian Rupee', '₨', true),
    ('SCR', 'Seychellois Rupee', '₨', true),
    ('FJD', 'Fijian Dollar', 'FJ$', true),
    ('MXN', 'Mexican Peso', '$', true),
    ('BRL', 'Brazilian Real', 'R$', true)
) AS v(code, name, symbol, is_active)
WHERE NOT EXISTS (SELECT 1 FROM supported_currencies s WHERE s.code = v.code);
