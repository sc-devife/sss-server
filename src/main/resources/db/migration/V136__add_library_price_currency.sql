-- Library prices in a supplier's own currency. A hotel/activity/transport can
-- price everything it lists (room prices, services, base price) in a currency
-- other than the vendor's base; NULL = the vendor's base currency. Prices are
-- converted to base when they are priced into an itinerary or a quote.
ALTER TABLE hotels ADD COLUMN price_currency VARCHAR(10);
ALTER TABLE activities ADD COLUMN price_currency VARCHAR(10);
ALTER TABLE transports ADD COLUMN price_currency VARCHAR(10);
