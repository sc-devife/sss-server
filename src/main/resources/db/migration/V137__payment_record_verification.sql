-- Verification is tracked per payment, not just per milestone: a milestone's
-- "verify" step now stamps every payment on it that was still awaiting it.
ALTER TABLE payment_records ADD COLUMN verified_at TIMESTAMP;
ALTER TABLE payment_records ADD COLUMN verified_by BIGINT;

-- Payments already on verified milestones count as verified.
UPDATE payment_records r
SET verified_at = COALESCE(m.marked_paid_at, r.recorded_at), verified_by = m.marked_paid_by
FROM payment_milestones m
WHERE m.seqp = r.milestone_id AND m.status IN ('paid', 'partially_paid');
