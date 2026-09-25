-- Payments recorded before V133 only exist as a running total on their milestone.
-- Give each such milestone one payment record for that total, in the vendor's base
-- currency (that is all it could have been), so every milestone lists its payments
-- and the ledger/report queries have a single source.
INSERT INTO payment_records (org_id, milestone_id, received_amount, received_currency, fx_rate,
                             applied_amount_base, base_value_received, fx_difference_base,
                             payment_method, payment_reference, recorded_by, recorded_at)
SELECT m.org_id, m.seqp, m.amount_paid_inr,
       COALESCE(NULLIF(s.default_currency_code, ''), 'INR'), 1,
       m.amount_paid_inr, m.amount_paid_inr, 0,
       m.payment_method, m.payment_reference, m.marked_paid_by, COALESCE(m.marked_paid_at, NOW())
FROM payment_milestones m
LEFT JOIN organization_settings s ON s.org_id = m.org_id
WHERE m.amount_paid_inr > 0
  AND NOT EXISTS (SELECT 1 FROM payment_records r WHERE r.milestone_id = m.seqp);
