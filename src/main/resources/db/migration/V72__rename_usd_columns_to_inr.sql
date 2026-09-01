-- Pure metadata rename — Postgres preserves all existing data untouched.
-- No values change; only the base currency's name in the schema does
-- (USD -> INR).

ALTER TABLE quotes RENAME COLUMN subtotal_usd TO subtotal_inr;
ALTER TABLE quotes RENAME COLUMN tax_amount_usd TO tax_amount_inr;
ALTER TABLE quotes RENAME COLUMN total_usd TO total_inr;

ALTER TABLE payment_milestones RENAME COLUMN amount_usd TO amount_inr;
ALTER TABLE payment_milestones RENAME COLUMN amount_paid_usd TO amount_paid_inr;
