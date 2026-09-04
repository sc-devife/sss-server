-- TCS (Tax Collected at Source) — a second, statutory tax stacked alongside
-- GST on Indian outbound travel packages, shown as a separate line/rate from
-- the existing TaxProfile-driven GST. Kept as flat fields on Quote (mirroring
-- tax_amount_inr) rather than a generalized multi-tax model, since exactly
-- two taxes (GST + TCS) is the real-world requirement, not an open-ended list.
ALTER TABLE quotes
    ADD COLUMN tcs_rate_percent NUMERIC(5, 2),
    ADD COLUMN tcs_amount_inr NUMERIC(14, 2);
