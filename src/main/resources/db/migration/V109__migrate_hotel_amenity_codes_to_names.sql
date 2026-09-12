-- hotels.amenities (text[]) used to store the old hardcoded option *values*
-- (e.g. "wifi") rather than display names. Now that Amenities are dynamic
-- master data keyed by name (see V108), rewrite existing hotel rows to the
-- matching display name so they still match an amenities.name entry and
-- keep showing as selected/checked in the Add/Edit Hotel form and hotel
-- detail page.

UPDATE hotels SET amenities = array_replace(amenities, 'wifi', 'Wi-Fi');
UPDATE hotels SET amenities = array_replace(amenities, 'pool', 'Pool');
UPDATE hotels SET amenities = array_replace(amenities, 'parking', 'Parking');
UPDATE hotels SET amenities = array_replace(amenities, 'gym', 'Gym');
UPDATE hotels SET amenities = array_replace(amenities, 'spa', 'Spa');
UPDATE hotels SET amenities = array_replace(amenities, 'restaurant', 'Restaurant');
UPDATE hotels SET amenities = array_replace(amenities, 'ac', 'Air Conditioning');
UPDATE hotels SET amenities = array_replace(amenities, 'breakfast', 'Breakfast Included');
