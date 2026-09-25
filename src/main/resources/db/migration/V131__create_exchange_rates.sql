-- Phase 2 of multi-currency: exchange rates.
--
-- market_exchange_rates: shared by every vendor, refreshed daily from a free
-- provider. Rates are stored against USD (units of the currency per 1 USD);
-- any pair is derived through that pivot, so a single fetch covers all pairs.
CREATE TABLE market_exchange_rates (
    seqp          BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(10)    NOT NULL,
    rate_per_usd  NUMERIC(24, 10) NOT NULL,
    as_of_date    DATE           NOT NULL,
    source        VARCHAR(50)    NOT NULL,
    fetched_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_market_rate_currency_date UNIQUE (currency_code, as_of_date)
);
CREATE INDEX idx_market_rates_currency ON market_exchange_rates (currency_code, as_of_date DESC);

-- org_exchange_rates: a vendor's own rate for a pair, "1 <from> = rate <to>"
-- (vendor currency first). While `is_manual` is true it overrides the market
-- rate and the daily refresh never touches it; turning it off (or deleting
-- the row) falls back to the market rate.
CREATE TABLE org_exchange_rates (
    seqp          BIGSERIAL PRIMARY KEY,
    org_id        BIGINT         NOT NULL,
    from_currency VARCHAR(10)    NOT NULL,
    to_currency   VARCHAR(10)    NOT NULL,
    rate          NUMERIC(24, 10) NOT NULL,
    is_manual     BOOLEAN        NOT NULL DEFAULT TRUE,
    updated_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_by    BIGINT,
    CONSTRAINT uq_org_rate_pair UNIQUE (org_id, from_currency, to_currency)
);
