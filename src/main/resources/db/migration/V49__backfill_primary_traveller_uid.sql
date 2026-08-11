-- Best-effort backfill for escapes created before Escape.primary_traveller_uid
-- existed. There is no real "primary" signal for these rows (no timestamp on
-- traveller, no ordering on escape_traveller) — this picks the
-- lowest-seqp (earliest-inserted) traveller linked to each escape as a
-- reasonable guess, explicitly accepted as a guess rather than a fact.
-- Only touches escapes that don't already have a primary_traveller_uid, so
-- it's safe to re-run and won't disturb escapes created after V47.
UPDATE escapes e
SET primary_traveller_uid = t.uid
FROM (
    SELECT et.escape_id, MIN(et.traveller_id) AS min_traveller_seqp
    FROM escape_traveller et
    GROUP BY et.escape_id
) sub
JOIN traveller t ON t.seqp = sub.min_traveller_seqp
WHERE e.seqp = sub.escape_id
  AND e.primary_traveller_uid IS NULL;
