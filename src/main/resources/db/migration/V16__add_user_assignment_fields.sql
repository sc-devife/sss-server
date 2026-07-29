-- Section 15 User dictionary fields needed by the Lead Assignment Engine
-- (Section 5): specialist match, capacity cap, and priority-lead eligibility.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_specialist BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS specialist_destinations BIGINT[],
    ADD COLUMN IF NOT EXISTS max_concurrent_assignments INTEGER,
    ADD COLUMN IF NOT EXISTS eligible_for_priority_leads BOOLEAN NOT NULL DEFAULT false;
