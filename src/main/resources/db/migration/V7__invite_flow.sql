-- Real invite-token flow needs: a token that's actually generated (uid had
-- the same missing-default bug organizations.uid had), who sent the invite,
-- and who a user was invited by (per the User data dictionary).

ALTER TABLE user_invitations ALTER COLUMN uid SET DEFAULT gen_random_uuid();
UPDATE user_invitations SET uid = gen_random_uuid() WHERE uid IS NULL;

ALTER TABLE user_invitations ADD COLUMN invited_by BIGINT REFERENCES users (seqp);
ALTER TABLE users ADD COLUMN invited_by BIGINT REFERENCES users (seqp);
