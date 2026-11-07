# Internship & Placement Opportunity Tracker

**Course:** 21CSE354T – Full Stack Web Development · **Task 4** (Project #11)
**Stack:** React 18 (Vite) · Spring Boot 3.2 (Java 17) · Spring Data JPA · H2 / MySQL · JDBC · separate Eligibility microservice

A campus placement cell currently tracks internships/placements in spreadsheets and messages. This application digitises the workflow:
the placement officer publishes opportunities with eligibility criteria, students apply (only if eligible, before the deadline, once),
and the officer moves applications through *Applied → Shortlisted → Interview → Offered / Rejected*, with a live dashboard.

## Features
| Area | What it does |
|---|---|
| Roles | **Placement Officer (admin)** and **Student** (email login, no passwords – demo auth) |
| Opportunities | CRUD, internship/placement types, CGPA/backlog/department criteria, openings, deadline, open/close |
| Search & filter | Text search, type, company, "open only" (opportunities); text/department (students, companies); status/text (applications) |
| Applications | Eligibility check → apply with cover note → withdraw; officer updates status with remarks |
| Business rules | Deadline, closed role, duplicate application, **eligibility (microservice)**, **one placement-offer policy**, valid status transitions only, **openings cannot be exceeded** |
| Dashboard | Totals, placement rate, status chart, applications per company (join), closing-soon list, **department report (plain JDBC)** |
| Validation | Client-side (forms) + server-side (Jakarta Validation) with field-level error display, toasts for success/error |
| Data | Companies, Students CRUD with delete protection when records are referenced |

## Architecture
```
 React SPA (5173/3000) ──/api──▶ Spring Boot main app (8080) ──REST──▶ Eligibility microservice (8081)
                                   │  controller → service → repository          (stateless rules)
                                   └── JPA + JDBC ──▶ H2 file DB  (or MySQL)
```
See `docs/ARCHITECTURE.md`, `docs/ER_DIAGRAM.md`, `docs/API.md`, `docs/TEST_PLAN.md`, `docs/WIREFRAMES.md`, `docs/CO_MAPPING.md`.

## Quick start (local, no database install needed)
**Prerequisites:** JDK 17+, Maven 3.9+, Node 18+.

```bash
# Terminal 1 – eligibility microservice (port 8081)
cd eligibility-service && mvn spring-boot:run

# Terminal 2 – main backend (port 8080); creates ./data/placementdb (H2) and seeds synthetic demo data
cd backend && mvn spring-boot:run

# Terminal 3 – frontend (port 5173)
cd frontend && npm install && npm run dev
```
Or run everything with `./run-local.sh`. Open **http://localhost:5173**.

**Demo logins (no password):** admin `tpo@campus.demo`; students e.g. `aarav.kumar@campus.demo`, `diya.sharma@campus.demo`, `meera.nair@campus.demo`
(the login page shows clickable demo accounts). Try: log in as Meera (CGPA 6.8, 1 backlog) and open a strict role to see the eligibility rejection.

> If the eligibility service is not running, the backend falls back to an identical local rule set (`ELIGIBILITY_FALLBACK=true`, default) and
> the UI shows `Checked by: local-fallback`. Set it to `false` to make the service mandatory.

## Run tests
```bash
cd eligibility-service && mvn test      # 10 tests: rules + REST contract + validation
cd backend && mvn test                  # unit tests (mocked repos) + integration test (MockMvc + in-memory H2)
```

## Deployment

### Option A – Docker (MySQL + all services)
```bash
export DB_ROOT_PASSWORD='choose-a-local-password'   # never commit real passwords
docker compose up --build
# open http://localhost:3000
```

### Option B – MySQL without Docker
1. Create DB: `mysql -u root -p < db/schema.sql` (optional; tables are auto-created otherwise).
2. Set environment variables, then run the backend:
```bash
export DB_URL='jdbc:mysql://localhost:3306/placement_db?createDatabaseIfNotExist=true'
export DB_USERNAME=root DB_PASSWORD='your-password'
export ELIGIBILITY_URL=http://localhost:8081
cd backend && mvn spring-boot:run
```

### Environment variables
| Variable | Default | Used by | Purpose |
|---|---|---|---|
| `DB_URL` | `jdbc:h2:file:./data/placementdb;AUTO_SERVER=TRUE` | backend | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `sa` / *(empty)* | backend | DB credentials |
| `ELIGIBILITY_URL` | `http://localhost:8081` | backend | Microservice base URL |
| `ELIGIBILITY_FALLBACK` | `true` | backend | Use local rules if the service is down |
| `ADMIN_EMAIL` | `tpo@campus.demo` | backend | Placement officer login |
| `APP_SEED` | `true` | backend | Insert synthetic demo data when DB is empty |
| `DEMO_MODE` | `true` | backend | Show demo accounts on login page (**set false for real use**) |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` | backend | Allowed browser origins |
| `PORT` | `8081` | eligibility | Service port |
| `VITE_API_URL` | `/api` | frontend (build) | API base URL if not using the proxy |

### Option C – cloud (live URL)
Backend and eligibility service run as plain Spring Boot jars (`mvn package`, `java -jar target/*.jar`) on e.g. Render/Railway with a managed MySQL;
build the frontend with `VITE_API_URL=https://<your-backend>/api npm run build` and host `frontend/dist` on Netlify/Vercel (add an SPA fallback to `index.html`),
and set `CORS_ORIGINS` to the frontend URL.

## Project layout
```
backend/               Spring Boot main app  (domain · repository · service · web · config · dto · exception)
eligibility-service/   Spring Boot microservice (POST /api/eligibility/check)
frontend/              React + Vite SPA (pages, components, hooks, routing)
db/                    schema.sql, queries.sql (reference SQL)
docs/                  architecture, ER diagram, API, test plan, wireframes, CO mapping, report template
```

## Security & data notes
* No passwords or API keys are stored in the repo. Authentication is a **demo** (email header); for real use add Spring Security + OAuth2/JWT.
* All seed data is synthetic (`*.example.com`, `@campus.demo`). Do not load real student data in public demos.
* Roles are enforced server-side (`AuthInterceptor` + controller checks), not only in the UI.

## Credits
Built with Spring Boot, Spring Data JPA, H2, MySQL Connector/J, React, React Router, Vite. Add your team's references/tutorials here.
