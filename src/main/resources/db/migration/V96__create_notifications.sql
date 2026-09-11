CREATE TABLE notifications (
    seqp BIGSERIAL PRIMARY KEY,
    uid UUID NOT NULL UNIQUE,
    org_id BIGINT NOT NULL,
    recipient_user_id BIGINT NOT NULL REFERENCES users(seqp),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    related_entity_type VARCHAR(30),
    related_entity_uid UUID,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE INDEX idx_notifications_recipient_org ON notifications(recipient_user_id, org_id);
CREATE INDEX idx_notifications_recipient_unread ON notifications(recipient_user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

ALTER TABLE users ADD COLUMN notification_sound_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE escapes ADD COLUMN travel_date_reminder_sent_at TIMESTAMP;
