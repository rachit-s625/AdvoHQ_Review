# AdvoHQ Backend

REST API for the AdvoHQ case & brief management app — **Java 17 + Spring Boot 3**, with
**PostgreSQL** for working data and **AWS S3** for client-provided documents.

It backs every feature of the front-end prototype (which currently uses `localStorage`):
users/auth, cases, important dates, the hearing calendar, document storage, profile,
settings toggles, and custom case stages.

---

## Stack

| Concern            | Choice                                             |
|--------------------|----------------------------------------------------|
| Language / runtime | Java 17                                            |
| Framework          | Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| Database (SQL)     | PostgreSQL, schema managed by **Flyway**           |
| Document storage   | **AWS S3** (private objects + pre-signed URLs)     |
| Auth               | Stateless **JWT** (BCrypt-hashed passwords)        |
| Build              | Maven                                              |

---

## Prerequisites

- **JDK 17+** — `brew install openjdk@17` (this machine had no JDK; install one to build)
- **Maven 3.9+** — `brew install maven`
- **PostgreSQL 13+** running locally (or via Docker)
- An **AWS account + S3 bucket** (or LocalStack/MinIO for offline dev)

---

## 1. Database

```bash
# quickest: Docker
docker run --name advohq-pg -e POSTGRES_DB=advohq \
  -e POSTGRES_USER=advohq -e POSTGRES_PASSWORD=advohq \
  -p 5432:5432 -d postgres:16
```

Flyway creates all tables automatically on first start (`src/main/resources/db/migration/V1__init.sql`).

## 2. Configure

```bash
cp .env.example .env        # then edit values
# generate a strong JWT secret:
openssl rand -base64 32
```

Everything is environment-driven (see `application.yml`). Key vars:
`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `AWS_REGION`, `AWS_S3_BUCKET`,
`CORS_ALLOWED_ORIGINS`.

AWS credentials are resolved by the default SDK chain (env vars, `~/.aws/credentials`,
or an IAM role) — **never hard-coded**.

## 3. Run

```bash
set -a && source .env && set +a      # load env into the shell
mvn spring-boot:run                  # or: mvn clean package && java -jar target/advohq-backend-1.0.0.jar
```

API base: `http://localhost:8080` · health check: `GET /actuator/health`

> **Note:** the code was written but not compiled in this environment (no JDK/Maven was
> installed here). Run `mvn clean package` once to compile; if JPA's `ddl-auto: validate`
> ever complains about a column type on your Postgres version, switch it to `none` in
> `application.yml` — Flyway already owns the schema.

---

## API reference

All routes except `/api/auth/**` and `/actuator/health` require
`Authorization: Bearer <token>`.

### Auth
| Method | Path                 | Body                                            |
|--------|----------------------|-------------------------------------------------|
| POST   | `/api/auth/register` | `{username,password,fullName,phone?,email?}`    |
| POST   | `/api/auth/login`    | `{username,password}` → `{token,user,...}`      |

### Cases
| Method | Path              | Notes                          |
|--------|-------------------|--------------------------------|
| GET    | `/api/cases`      | list (newest first)            |
| POST   | `/api/cases`      | `{title,stage?,points?,importantDates?}` |
| GET    | `/api/cases/{id}` | one case + its important dates |
| PUT    | `/api/cases/{id}` | full update (dates re-synced)  |
| DELETE | `/api/cases/{id}` |                                |

### Schedule events
| Method | Path               | Notes                                          |
|--------|--------------------|------------------------------------------------|
| GET    | `/api/events`      | `?from=YYYY-MM-DD&to=YYYY-MM-DD` (optional)     |
| POST   | `/api/events`      | `{type,caseName,date,caseNo?,location?,hall?,notes?}` |
| PUT    | `/api/events/{id}` |                                                |
| DELETE | `/api/events/{id}` |                                                |

### Documents (AWS S3)
| Method | Path                              | Notes                                  |
|--------|-----------------------------------|----------------------------------------|
| GET    | `/api/documents`                  | `?caseId=` (optional)                  |
| POST   | `/api/documents`                  | multipart `file` (+ optional `caseId`) |
| GET    | `/api/documents/{id}/download-url`| short-lived pre-signed S3 URL          |
| DELETE | `/api/documents/{id}`             | removes from S3 **and** DB             |

### Profile / settings / stages
| Method | Path               | Notes                              |
|--------|--------------------|------------------------------------|
| GET/PUT| `/api/me`          | profile                            |
| GET/PUT| `/api/settings`    | key/value toggles (`{key,value}`)  |
| GET/POST/DELETE | `/api/stages` `/api/stages/{id}` | custom case stages |

---

## Front-end integration

A ready-made client lives at `../advohq-frontend/api.js`. Add it to any page:

```html
<script>window.ADVOHQ_API_BASE = 'http://localhost:8080';</script>
<script src="api.js"></script>
```

Then replace the `localStorage` calls with API calls, e.g.:

```js
// login.html
await AdvoAPI.auth.login(username, password);   // stores the JWT for you
location.href = 'advohq-home.html';

// dashboard — load the user's cases from SQL instead of localStorage
const cases = await AdvoAPI.cases.list();

// upload a client document to S3
await AdvoAPI.documents.upload(fileInput.files[0], caseId);
const { url } = await AdvoAPI.documents.downloadUrl(docId);
window.open(url);                                // opens the pre-signed S3 link
```

`AdvoAPI` mirrors the existing data shapes (`importantDates`, `caseName`, `dateISO`, …),
so wiring it in is mostly swapping each `localStorage.getItem/setItem` for the matching
`AdvoAPI.*` call.

---

## Security notes
- Passwords hashed with BCrypt; tokens are signed JWTs (HMAC-SHA256).
- Every query is scoped to the authenticated user — no cross-tenant access.
- S3 objects are **private**; the browser only ever receives time-limited pre-signed URLs.
- Set a real `JWT_SECRET` and restrict `CORS_ALLOWED_ORIGINS` in production.
