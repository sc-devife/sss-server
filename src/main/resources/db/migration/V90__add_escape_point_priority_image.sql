ALTER TABLE escape_points ADD COLUMN priority_image VARCHAR(1000);

-- Backfill: escape points created before this column existed used the
-- first-in-array image as their de-facto cover — preserve that as the
-- explicit priority so existing data keeps showing the same cover image.
UPDATE escape_points
SET priority_image = images[1]
WHERE priority_image IS NULL AND images IS NOT NULL AND array_length(images, 1) > 0;
