-- Vendor/supplier email for the activity — mirrors hotels.email (see
-- V101__add_hotel_phone_email.sql) and is the recipient for the "Send
-- Booking Email" booking-request flow on the Activity Detail page.
ALTER TABLE activities
    ADD COLUMN email VARCHAR(255);
