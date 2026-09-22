-- Quote tab redesign: a persisted, itinerary-item-shaped breakdown of each
-- quote (day-wise, one row per itinerary item) so each item can carry its
-- own discount, on top of (or instead of) the quote's existing single
-- overall discount. Rows are synced from the itinerary's current items
-- whenever the Quote tab is opened/computed (see QuoteComputationServiceImpl
-- .syncLineItems) — deleted itinerary items cascade-delete their line item.
CREATE TABLE quote_line_items (
    seqp                BIGSERIAL PRIMARY KEY,
    uid                 UUID NOT NULL UNIQUE,
    org_id              BIGINT,
    quote_id            BIGINT NOT NULL REFERENCES quotes(seqp) ON DELETE CASCADE,
    itinerary_item_id   BIGINT NOT NULL REFERENCES itinerary_items(seqp) ON DELETE CASCADE,
    day_number          INT NOT NULL,
    item_type           TEXT NOT NULL,
    label               TEXT NOT NULL,
    sort_order          INT NOT NULL DEFAULT 0,
    -- True for a Dropped hotel/activity/transport's cancellation-charge
    -- contribution (see QuoteComputationServiceImpl.resolvePrice) — kept so
    -- the breakdown-by-category totals can bucket it separately from an
    -- active booking's cost, same distinction PricingBreakdownDTO already draws.
    is_cancellation     BOOLEAN NOT NULL DEFAULT false,
    -- Snapshot of what QuoteComputationServiceImpl.resolvePrice() currently
    -- resolves for this item — the itinerary-planning "estimate" price the
    -- Quote tab lists it at before any quote-side discount.
    base_amount_inr     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    -- none / percent / flat — same vocabulary as quotes.discount_type.
    discount_type       TEXT NOT NULL DEFAULT 'none',
    discount_value      NUMERIC(12, 2),
    final_amount_inr    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,
    UNIQUE (quote_id, itinerary_item_id)
);

CREATE INDEX idx_quote_line_items_quote_id ON quote_line_items(quote_id);

-- Lets a quote's tax % be entered directly (a one-off rate, or a tweak to
-- the selected Tax Profile's own stored rate) instead of only ever using
-- whatever percent the profile has saved. When set, it wins over
-- tax_profile_id's own rate_percent at compute time; tax_profile_id is still
-- recorded (which tax TYPE — GST/VAT/etc — this override belongs to).
ALTER TABLE quotes ADD COLUMN tax_rate_percent_override NUMERIC(6, 3);
