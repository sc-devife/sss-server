-- Renames leads.duration_days to duration_nights to match the corrected
-- business meaning: this field has always been entered/used as a night
-- count (see LeadsPanel's "No. of Night" field, and the priority-detection
-- rule's own Javadoc, which already said "nights"), even though the column
-- and code were named "days". No numeric values are changed here —
-- existing rows are preserved exactly as stored (see the impact analysis:
-- historical data is a mix of entry paths and cannot be reliably
-- reinterpreted after the fact, so this migration deliberately does NOT
-- attempt any backfill/conversion of existing values).
ALTER TABLE leads RENAME COLUMN duration_days TO duration_nights;
