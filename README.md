# AdvoHQ

Case & brief management for advocates — static front-end + a Java/Spring Boot API,
deployed as two separate Render web services from this one repo.

```
advohq-frontend/   Static site (HTML/CSS/JS) + a small Express server
                    that also serves /api/chat, /api/send-otp, /api/verify-otp
advohq-backend/    Java 17 + Spring Boot 3 REST API (Postgres + S3 + JWT)
render.yaml        Render Blueprint — deploys both services in one go
```

> Note: the front-end currently persists case data to `localStorage` and is not
> yet wired up to `advohq-backend`'s REST API. Both services can be deployed
> independently; integrating them (swapping `localStorage` calls for API calls,
> per `advohq-backend/README.md`) is separate follow-up work.

## Deploying to Render

### Option A — Blueprint (one click)

In the Render dashboard: **New → Blueprint**, point it at this repo. Render reads
`render.yaml` and creates both services. You'll be prompted for the env vars
marked `sync: false` below.

### Option B — Manual (two services)

**`advohq-backend`** (Web Service, Docker)
- Root Directory: `advohq-backend`
- Environment: Docker (uses the included `Dockerfile`)
- Health check path: `/actuator/health`
- Env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_SCHEMA`, `JWT_SECRET`,
  `CORS_ALLOWED_ORIGINS`, `AWS_REGION`, `AWS_S3_BUCKET`, `AWS_ACCESS_KEY_ID`,
  `AWS_SECRET_ACCESS_KEY` (see `advohq-backend/.env.example`)
- Storage: **AWS S3** for documents (already the default in this backend — just set
  the `AWS_*` vars above to your bucket/credentials).
- Database: **Supabase Postgres**, via the **Session Pooler** (Render's network is
  IPv4-only; Supabase's direct connection host is IPv6-only and will fail with
  `Network is unreachable`). In Supabase: Connect → Session pooler tab, then set:
  - `DB_URL=jdbc:postgresql://<pooler-host-shown>:5432/postgres?sslmode=require`
  - `DB_USERNAME=postgres.<project-ref>` (qualified form — required for the pooler)
  - `DB_PASSWORD=<your Supabase database password>`

  If your Supabase project already has other tables (e.g. from a previous backend
  attempt sharing the same database), set `DB_SCHEMA=advohq` — the app runs entirely
  inside that dedicated schema instead of `public`, so Flyway/Hibernate never see or
  collide with unrelated tables. Flyway creates the schema and its tables on first boot
  (`ddl-auto: validate` — Flyway owns the schema, Hibernate only validates against it).

**`advohq-frontend`** (Web Service, Node)
- Root Directory: `advohq-frontend`
- Build Command: `npm install`
- Start Command: `npm start`
- Env vars: `ANTHROPIC_API_KEY`, `OTP_SECRET`, `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`,
  `BREVO_SENDER_NAME` (see `advohq-frontend/.env.example`)
- Email: signup OTP codes are sent via **Brevo**. `BREVO_SENDER_EMAIL` must be a
  verified sender (or domain) in your Brevo account, or sends will fail.

### After both are live

- Set `CORS_ALLOWED_ORIGINS` on `advohq-backend` to the `advohq-frontend` URL Render gives you.
- If/when the front-end is wired to call the API, set its base URL to the `advohq-backend` URL.
