# Database recovery — fresh RDS instance

Your database was recreated empty (`ap-south-1`) and Flyway's `V1` migration
failed because it (and several migrations after it) assume tables that were
never captured in a migration script — they originally existed via
Hibernate's `ddl-auto` from before this project adopted Flyway. Full
diagnosis is in `application.properties`'s comments and the git history of
this file. Short version: rather than hand-reconstruct ~20 tables' historical
DDL (risky, unverifiable without a live DB connection on my end), we let
Hibernate build today's correct schema directly from the `@Entity` classes,
then tell Flyway to treat that as already fully migrated.

**Do these steps in order. Steps marked (you) need your own terminal/IDE
since I don't have `DB_USERNAME`/`DB_PASSWORD` in my shell.**

## 1. (you) One-time schema bootstrap

`application.properties` is currently set to `spring.flyway.enabled=false` and
`spring.jpa.hibernate.ddl-auto=update` — both marked `TEMPORARY` in comments
right above them.

Run the backend once, normally (however you usually start it — IDE run
config or `mvnw.cmd spring-boot:run` with your env vars set). Watch the logs:
you should see Hibernate's `SchemaManagementTool` create a bunch of tables
and Tomcat start cleanly, with **no Flyway lines at all** (it's disabled).

**Stop the app as soon as it's up.** Do not run it a second time in this
state — `ddl-auto=update` is safe to re-run (it only adds what's missing, never
drops), but there's no reason to and I'd rather you move straight to step 2.

## 2. (me) Revert the properties + tell Flyway it's already at V42

Message me once step 1's clean startup is confirmed and I'll flip
`application.properties` back (`flyway.enabled=true`, `ddl-auto=validate`,
`baseline-version` bumped from `0` to `42` — matching the highest existing
migration file). This is needed so Flyway's already-on
`baseline-on-migrate=true` auto-baselines the now-populated-but-unversioned
schema at V42 next time, instead of trying to replay `V1`..`V42` against
tables that already exist.

## 3. (you) Run the two SQL scripts

Two new files sit next to this one in `src/main/resources/`:

- **`post-bootstrap-patches.sql`** — run this first. It (a) sets a handful of
  DB-level `uid` column defaults that several entities need but no migration
  ever added (a pre-existing gap, not something the recreation caused —
  worth knowing about even outside this recovery), and (b) replays the pure
  seed-data `INSERT`s from `V6`, `V13`, `V21` (RBAC roles/permissions,
  currencies) — since Flyway baselining in step 2 marks those migrations
  "done" without ever executing their SQL bodies.
- **`dummy-data-seed.sql`** — run this second (depends on the roles seeded
  above). Creates one organization ("Demo Travel Co"), one admin user, two
  escape points (Crrog/Netravati), two hotels, two leads converted to two
  escapes — enough to exercise the app end to end. Guarded against
  re-running twice (checks for the `admin` user first).

Run both via `psql`, pgAdmin, DBeaver, whatever you have connected to the new
RDS instance. Both are idempotent-ish (see their own header comments) but
there's no need to run them more than once.

**Login after this:** userId `admin`, password `Admin@1234`.

## 4. (you) Start the backend normally

Same as always. Flyway should auto-baseline at V42 silently, Hibernate
validates cleanly (it's the same schema it just built), app comes up. Ping me
once it's running and I'll take over to verify the frontend against the
seeded data.

## Afterward (optional, not urgent)

Once you're confirmed working, running `pg_dump --schema-only` against this
now-correct database and checking that in as a real `V0__baseline_schema.sql`
would mean the *next* fresh database (new dev, CI, another region move)
doesn't need this manual dance again — a normal `mvnw spring-boot:run` would
just work. Happy to help with that whenever it's convenient; not needed for
today.

## Delete this file

Once everything's confirmed working, this file and the two SQL scripts have
served their purpose — feel free to delete them (or keep them around as a
reference for next time).
