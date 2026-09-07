ALTER TABLE hotels ADD COLUMN priority_image VARCHAR(1000);

-- Backfill: hotels created before this column existed used the first-in-array
-- image as their de-facto cover — preserve that as the explicit priority so
-- existing data keeps showing the same cover image (same approach as V90's
-- escape_points.priority_image backfill).
UPDATE hotels
SET priority_image = images[1]
WHERE priority_image IS NULL AND images IS NOT NULL AND array_length(images, 1) > 0;
