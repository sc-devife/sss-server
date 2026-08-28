-- Dummy/test data for local development. NOT a Flyway migration — run this
-- manually, once, after post-bootstrap-patches.sql. Safe to re-run: guarded
-- by a check for the admin user_id at the top: if 'admin' already exists,
-- the whole block is skipped rather than creating duplicates.
--
-- Login after this runs: userId "admin", password "Admin@1234"
-- (bcrypt hash below was generated locally with the app's own
-- BCryptPasswordEncoder and verified to round-trip correctly).

DO $$
DECLARE
    v_org_id        BIGINT;
    v_admin_id      BIGINT;
    v_super_admin_role_id BIGINT;
    v_loc_madikeri  BIGINT;
    v_loc_chikka    BIGINT;
    v_ep_crrog      BIGINT;
    v_ep_netravati  BIGINT;
    v_lead_sanat    BIGINT;
    v_lead_sangmesh BIGINT;
    v_trav_sanat    BIGINT;
    v_trav_sangmesh BIGINT;
    v_escape_1      BIGINT;
    v_escape_2      BIGINT;
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE user_id = 'admin') THEN
        RAISE NOTICE 'Seed data already present (admin user exists) — skipping.';
        RETURN;
    END IF;

    -- Organization. Unlike every other entity here (GenerationType.IDENTITY,
    -- auto-populated on insert), Organizations uses GenerationType.SEQUENCE
    -- against an explicitly-named sequence — Hibernate's own INSERTs call
    -- nextval() themselves rather than relying on a column DEFAULT, so a raw
    -- SQL insert has to do the same explicitly or seqp would try to insert NULL.
    INSERT INTO organizations (seqp, registered_name, display_name, org_code, support_ph_num, country_code, default_currency_code, status, auto_assign_enabled, created_at, updated_at)
    VALUES (nextval('organizations_seqp_seq'), 'Demo Travel Co', 'Demo Travel Co', 'DEMO', '+911234567890', 'IN', 'INR', 'ACTIVE', true, now(), now())
    RETURNING seqp INTO v_org_id;

    -- Admin user + credentials + role
    INSERT INTO users (user_id, name, email, first_name, last_name, contact_number, org_id, created_at, accepting_leads, blocked)
    VALUES ('admin', 'Admin User', 'admin@demo.travel', 'Admin', 'User', '+911234567890', v_org_id, now(), true, false)
    RETURNING seqp INTO v_admin_id;

    INSERT INTO user_credentials (user_id, uid, password_hash, failed_login_attempts)
    VALUES (v_admin_id, gen_random_uuid(), '$2a$10$99sVliLlwUjmTNDjwdbvm.BdvQAmOvUUQPlEPAY7/O12LOpQWpxey', 0);

    SELECT seqp INTO v_super_admin_role_id FROM roles WHERE name = 'SUPER_ADMIN';
    INSERT INTO user_role_links (seqa, seqa_type, seqb, seqb_type, uid, is_active, is_archived)
    VALUES (v_admin_id, 'users', v_super_admin_role_id, 'roles', gen_random_uuid()::text, true, false);

    -- Locations (required by hotels)
    INSERT INTO locations (uid, org_id, city, state, country, display_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_org_id, 'Madikeri', 'Karnataka', 'India', 'Madikeri, Karnataka, India', true, now(), now())
    RETURNING seqp INTO v_loc_madikeri;

    INSERT INTO locations (uid, org_id, city, state, country, display_name, is_active, created_at, updated_at)
    VALUES (gen_random_uuid(), v_org_id, 'Chikkamagaluru', 'Karnataka', 'India', 'Chikkamagaluru, Karnataka, India', true, now(), now())
    RETURNING seqp INTO v_loc_chikka;

    -- Escape points (destinations)
    INSERT INTO escape_points (org_id, id, name, city, province, country, description, status, created_at, updated_at)
    VALUES (v_org_id, 'crg', 'Crrog', 'Madikeri', 'Karnataka', 'India', 'Best visiting place in Karnataka', 'active', now(), now())
    RETURNING seqp INTO v_ep_crrog;

    INSERT INTO escape_points (org_id, id, name, city, province, country, status, created_at, updated_at)
    VALUES (v_org_id, 'ntr', 'Netravati', 'Chikkamagaluru', 'Karnataka', 'India', 'active', now(), now())
    RETURNING seqp INTO v_ep_netravati;

    -- Hotels (one per escape point, for library completeness)
    INSERT INTO hotels (uid, org_id, name, stars, location_id, destination_id, is_active, status, created_at, updated_at)
    VALUES (gen_random_uuid(), v_org_id, 'Crrog Homestay', 3, v_loc_madikeri, v_ep_crrog, true, 'active', now(), now());

    INSERT INTO hotels (uid, org_id, name, stars, location_id, destination_id, is_active, status, created_at, updated_at)
    VALUES (gen_random_uuid(), v_org_id, 'Netravati Resort', 4, v_loc_chikka, v_ep_netravati, true, 'active', now(), now());

    -- Leads (never individually assigned — see leads schema note in V62)
    INSERT INTO leads (org_id, name, email, phone, destination, number_of_people, travel_date, duration_days, budget, status, destination_id, source_type, source_channel, origin_city, travel_type, is_priority, created_at, updated_at)
    VALUES (v_org_id, 'Sanat', 'sanat@gmail.com', '+916363356214', 'Crrog', 2, DATE '2026-08-20', 2, 4499, 'Converted', v_ep_crrog, 'DIRECT', 'manual', 'Bengaluru', 'friends', false, now(), now())
    RETURNING seqp INTO v_lead_sanat;

    INSERT INTO leads (org_id, name, email, phone, destination, number_of_people, travel_date, duration_days, budget, status, destination_id, source_type, source_channel, origin_city, travel_type, is_priority, created_at, updated_at)
    VALUES (v_org_id, 'Sangmesh', 'sangmesh@gmail.com', '+916363356215', 'Netravati', 2, DATE '2026-09-05', 2, 15000, 'Converted', v_ep_netravati, 'DIRECT', 'manual', 'Bengaluru', 'friends', false, now(), now())
    RETURNING seqp INTO v_lead_sangmesh;

    -- Travellers
    INSERT INTO traveller (org_id, first_name, last_name, email, phone)
    VALUES (v_org_id, 'Sanat', 'Buyya', 'sanat.buyya@example.com', '+916363356214')
    RETURNING seqp INTO v_trav_sanat;

    INSERT INTO traveller (org_id, first_name, last_name, email, phone)
    VALUES (v_org_id, 'Sangmesh', 'Rao', 'sangmesh.rao@example.com', '+916363356215')
    RETURNING seqp INTO v_trav_sangmesh;

    -- Escapes (converted from the leads above) — assignment lives here now,
    -- not on the lead (see escapes schema note in V63).
    INSERT INTO escapes (org_id, lead_id, status, start_date, number_of_days, end_date, assigned_to_user_id)
    VALUES (v_org_id, v_lead_sanat, 'Quotation Sent', DATE '2026-08-20', 2, DATE '2026-08-22', v_admin_id)
    RETURNING seqp INTO v_escape_1;

    INSERT INTO escapes (org_id, lead_id, status, start_date, number_of_days, end_date, assigned_to_user_id)
    VALUES (v_org_id, v_lead_sangmesh, 'Planning', DATE '2026-09-05', 2, DATE '2026-09-07', v_admin_id)
    RETURNING seqp INTO v_escape_2;

    INSERT INTO escape_traveller (escape_id, traveller_id) VALUES (v_escape_1, v_trav_sanat);
    INSERT INTO escape_traveller (escape_id, traveller_id) VALUES (v_escape_2, v_trav_sangmesh);

    INSERT INTO escape_destination (escape_id, escape_point_id) VALUES (v_escape_1, v_ep_crrog);
    INSERT INTO escape_destination (escape_id, escape_point_id) VALUES (v_escape_2, v_ep_netravati);

    RAISE NOTICE 'Seed complete. Org seqp=%, admin seqp=%, escapes: %, %', v_org_id, v_admin_id, v_escape_1, v_escape_2;
END $$;
