-- Phase 3 of multi-currency: a quote can be issued in a currency other than
-- the vendor's base. Amounts stay stored in the base currency; the quote's
-- currency_code + fx_rate_snapshot ("1 base = rate <currency>") say how it is
-- shown to the traveller.
--   fx_rate_source: 'market' (daily market rate), 'vendor' (the vendor's manual
--                   rate from Organization settings) or 'custom' (typed on this quote)
--   fx_rate_custom: true = pinned on this quote, never auto-refreshed
--   fx_rate_as_of : date of the market rate the snapshot came from
-- While a quote is a draft and not custom, the rate follows the daily rate;
-- it freezes for good once the quote leaves draft (marked sent / accepted).
ALTER TABLE quotes ADD COLUMN fx_rate_custom BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE quotes ADD COLUMN fx_rate_source VARCHAR(20);
ALTER TABLE quotes ADD COLUMN fx_rate_as_of DATE;
