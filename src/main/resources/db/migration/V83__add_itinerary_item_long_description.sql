-- Long-form descriptive copy per itinerary item (e.g. a paragraph about a
-- named activity/temple/tour) — distinct from the existing short `notes`
-- field, and rendered as an expandable block in quotation PDFs.
ALTER TABLE itinerary_items
    ADD COLUMN long_description TEXT;
