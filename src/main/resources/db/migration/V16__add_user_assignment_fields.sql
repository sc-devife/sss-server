-- Section 15 User dictionary fields needed by the Lead Assignment Engine
-- (Section 5): specialist match, capacity cap, and priority-lead eligibility.
ALTER TABLE users
    ADD COLUMN is_specialist BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN specialist_destinations BIGINT[],
    ADD COLUMN max_concurrent_assignments INTEGER,
    ADD COLUMN eligible_for_priority_leads BOOLEAN NOT NULL DEFAULT false;
