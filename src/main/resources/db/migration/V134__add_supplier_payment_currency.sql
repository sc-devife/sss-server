-- Phase 5 of multi-currency: the agency can pay a hotel/activity supplier in a
-- currency other than its base. `amount` stays the base-currency value (what
-- every total already sums); when the payment was made in another currency the
-- original amount, its currency and the rate ("1 base = fx_rate <paid_currency>")
-- are kept alongside. NULL paid_* = paid in the base currency.
ALTER TABLE hotel_payments ADD COLUMN paid_amount NUMERIC(14, 2);
ALTER TABLE hotel_payments ADD COLUMN paid_currency VARCHAR(10);
ALTER TABLE hotel_payments ADD COLUMN fx_rate NUMERIC(24, 10);

ALTER TABLE activity_payments ADD COLUMN paid_amount NUMERIC(14, 2);
ALTER TABLE activity_payments ADD COLUMN paid_currency VARCHAR(10);
ALTER TABLE activity_payments ADD COLUMN fx_rate NUMERIC(24, 10);
