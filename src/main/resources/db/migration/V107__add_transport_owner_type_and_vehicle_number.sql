ALTER TABLE transports
    ADD COLUMN owner_type VARCHAR(20) DEFAULT 'multi',
    ADD COLUMN vehicle_number VARCHAR(50);
