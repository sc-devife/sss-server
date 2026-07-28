-- Section 8/15/16: Itinerary -> many Quotes (budget/standard/premium, or
-- revisions of the same itinerary), versioned not overwritten. Deliberately
-- structural only this phase — subtotal/tax/total/discount/fx fields are
-- plain nullable columns set directly by the caller for now; the actual
-- tax-profile lookup + FX conversion + rate-snapshot computation is
-- Phase 5's Quotation Engine, not built here. template_id is a placeholder
-- FK-less UUID (no quote_templates table exists yet), same pattern already
-- used for organizations.quote_template_id from Phase 1.
CREATE TABLE quotes (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT REFERENCES organizations (seqp),
    itinerary_id BIGINT NOT NULL REFERENCES itineraries (seqp),
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    currency_code VARCHAR(4),
    fx_rate_snapshot NUMERIC(18, 6),
    subtotal_usd NUMERIC(14, 2),
    tax_profile_id UUID,
    tax_amount_usd NUMERIC(14, 2),
    total_usd NUMERIC(14, 2),
    discount_type VARCHAR(20) NOT NULL DEFAULT 'none',
    discount_value NUMERIC(14, 2),
    template_id UUID,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_quotes_itinerary_id ON quotes (itinerary_id);
CREATE INDEX idx_quotes_org_id ON quotes (org_id);
