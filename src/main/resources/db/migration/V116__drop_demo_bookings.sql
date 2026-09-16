-- The public "Book a Demo" marketing form (and its backend) has moved out
-- of this application entirely — this CRM (app.demo.com) only handles the
-- product itself now; a separate marketing site (demo.com) owns that flow.
-- V115 is kept as history rather than deleted (the database already has it
-- recorded), this migration just undoes what it created.
DROP TABLE IF EXISTS demo_bookings;
