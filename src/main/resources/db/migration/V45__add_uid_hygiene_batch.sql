-- Lower-priority tier of the ID-consistency fix: these tables don't leak a
-- seqp anywhere today (nothing routes on them externally), so this is pure
-- future-proofing rather than a bug fix -- every table ends up with the
-- standard seqp+uid shape.

ALTER TABLE audit_logs ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE audit_logs ADD CONSTRAINT uk_audit_logs_uid UNIQUE (uid);

ALTER TABLE user_password_resets ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE user_password_resets ADD CONSTRAINT uk_user_password_resets_uid UNIQUE (uid);

ALTER TABLE payment_reminder_log ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE payment_reminder_log ADD CONSTRAINT uk_payment_reminder_log_uid UNIQUE (uid);

ALTER TABLE lead_source_metadata ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE lead_source_metadata ADD CONSTRAINT uk_lead_source_metadata_uid UNIQUE (uid);

ALTER TABLE meta_webhook_events ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE meta_webhook_events ADD CONSTRAINT uk_meta_webhook_events_uid UNIQUE (uid);

ALTER TABLE meta_field_mappings ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE meta_field_mappings ADD CONSTRAINT uk_meta_field_mappings_uid UNIQUE (uid);

ALTER TABLE meta_channel_configs ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE meta_channel_configs ADD CONSTRAINT uk_meta_channel_configs_uid UNIQUE (uid);

ALTER TABLE integration_connections ADD COLUMN uid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE integration_connections ADD CONSTRAINT uk_integration_connections_uid UNIQUE (uid);
