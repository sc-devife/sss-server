-- Money columns were named for INR (subtotal_inr, amount_inr, ...) back when
-- every vendor was Indian. They hold the vendor's base-currency amount, so
-- they are renamed to say so. quote_line_items' "base" (pre-discount) and
-- "final" (post-discount) become gross / net to avoid "base_amount_base".
ALTER TABLE quotes RENAME COLUMN subtotal_inr TO subtotal_base;
ALTER TABLE quotes RENAME COLUMN tax_amount_inr TO tax_amount_base;
ALTER TABLE quotes RENAME COLUMN tcs_amount_inr TO tcs_amount_base;
ALTER TABLE quotes RENAME COLUMN total_inr TO total_base;
ALTER TABLE quotes RENAME COLUMN cancellation_charges_inr TO cancellation_charges_base;

ALTER TABLE quote_line_items RENAME COLUMN base_amount_inr TO gross_amount_base;
ALTER TABLE quote_line_items RENAME COLUMN final_amount_inr TO net_amount_base;

ALTER TABLE payment_milestones RENAME COLUMN amount_inr TO amount_base;
ALTER TABLE payment_milestones RENAME COLUMN amount_paid_inr TO amount_paid_base;

ALTER TABLE itinerary_items RENAME COLUMN cancellation_charge_inr TO cancellation_charge_base;
ALTER TABLE itinerary_item_hotel_details RENAME COLUMN cancellation_charge_inr TO cancellation_charge_base;

-- Currency-aware precision: currencies have 0 (JPY), 2 (INR, AED, USD) or 3
-- (KWD, BHD, OMR) minor-unit digits, and amounts must be stored without loss
-- for any of them. Every 2-decimal NUMERIC column in the schema (money and the
-- percentage columns alike - harmless) is widened to 4 decimals; values already
-- stored are unchanged. Application code rounds each amount to its currency's
-- own minor units (see MoneyScale).
DO $$
DECLARE
    col RECORD;
BEGIN
    FOR col IN
        SELECT table_name, column_name, numeric_precision
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND data_type = 'numeric'
          AND numeric_scale = 2
    LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN %I TYPE NUMERIC(%s, 4)',
                       col.table_name, col.column_name, col.numeric_precision + 2);
    END LOOP;
END $$;
