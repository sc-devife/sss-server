-- Team (Section 5 follow-up): groups users so destination specialization and
-- assignment capacity can be set once per team instead of repeated on every
-- individual user, so the assignment engine can route by team first, then
-- pick the specific person within it. Multi-team membership per user
-- (user_team_links join table, not a single FK) — an agent can belong to
-- more than one team at once.
CREATE TABLE teams (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT NOT NULL REFERENCES organizations (seqp),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    specialized_escape_points BIGINT[],
    team_lead_user_id BIGINT REFERENCES users (seqp),
    max_concurrent_assignments INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_teams_org_id ON teams (org_id);

CREATE TABLE user_team_links (
    seqp BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users (seqp),
    team_id BIGINT NOT NULL REFERENCES teams (seqp),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, team_id)
);

CREATE INDEX idx_user_team_links_user_id ON user_team_links (user_id);
CREATE INDEX idx_user_team_links_team_id ON user_team_links (team_id);

-- User — finalized profile fields from the metadata-planning pass.
ALTER TABLE users ADD COLUMN designation VARCHAR(255);
ALTER TABLE users ADD COLUMN signature TEXT;
