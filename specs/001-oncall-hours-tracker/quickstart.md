# Quickstart: On-Call Hours Tracker

**Date**: 2026-04-25 | **Plan**: [plan.md](plan.md)

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Docker Desktop | 4.x+ | `docker --version` |
| Docker Compose | 2.x+ | Bundled with Docker Desktop |
| Java JDK | 25+ | Only needed for local backend dev |
| Maven | 3.9+ | Only needed for local backend dev |
| Node.js | 24 LTS | Only needed for local frontend dev |
| pnpm | 9+ | `pnpm -v`; install via `npm i -g pnpm` if missing |

---

## Repository Structure

```
duty-tracker/
├── backend/          ← Spring Boot 4 REST API (Java 25, PostgreSQL)
│   ├── Dockerfile
│   └── src/main/resources/db/migration/   ← Flyway SQL files
├── frontend/         ← Nuxt 4 SPA (Vue 3, Pinia, Tailwind)
│   └── Dockerfile
├── docker-compose.yml
└── specs/            ← Specifications and plans
```

---

## Recommended: Docker Compose (all services)

Starts PostgreSQL 18, the Spring Boot backend and the Nuxt frontend together.

```bash
# From the repository root:
docker compose up --build
```

| Service | URL |
|---------|-----|
| Frontend | `http://localhost:3000` |
| Backend API | `http://localhost:8080/api/v1` |
| PostgreSQL | `localhost:5432` (internal only) |

Stop all services:
```bash
docker compose down
```

### Environment variables (`.env` in repo root)

```dotenv
POSTGRES_DB=dutytracker
POSTGRES_USER=dutytracker
POSTGRES_PASSWORD=dutytracker
```

Override the backend port if 8080 is taken:
```dotenv
BACKEND_PORT=9090
```

---

## Local Development (without Docker)

### 1. Start PostgreSQL

You still need a running PostgreSQL instance. The easiest way is to start only the database container:

```bash
docker compose up -d postgres
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Backend starts on **`http://localhost:8080`**.

Flyway runs automatically on startup and applies all migrations from `src/main/resources/db/migration/`.

#### Configuration

`backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:localhost}:5432/${POSTGRES_DB:dutytracker}
    username: ${POSTGRES_USER:dutytracker}
    password: ${POSTGRES_PASSWORD:dutytracker}
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
```

Override for a different database:
```bash
POSTGRES_HOST=my-server POSTGRES_DB=mydb mvn spring-boot:run
```

#### Backend Tests

```bash
cd backend
mvn test
```

Tests use an embedded H2 database in PostgreSQL compatibility mode — no external database needed for tests.

---

### 3. Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend starts on **`http://localhost:3000`**.

#### Configuration

`frontend/nuxt.config.ts` exposes the API base URL via `runtimeConfig`:

```ts
runtimeConfig: {
  public: {
    apiBase: process.env.NUXT_PUBLIC_API_BASE ?? 'http://localhost:8080/api/v1',
  },
}
```

Override for a different backend port:
```bash
NUXT_PUBLIC_API_BASE=http://localhost:9090/api/v1 pnpm dev
```

#### Frontend Tests

```bash
cd frontend
pnpm test
```

---

## First Launch

1. Open `http://localhost:3000` in your browser.
2. The onboarding wizard starts automatically:
   - **Step 1 — Profile**: Select employee type (Internal / External), set working days and hours.
   - **Step 2 — Preferences**: Choose color scheme (Dark / Light / Auto).
   - **Step 3 — Compensation Rates**: Review pre-loaded WCA placeholder rates. **⚠️ Update percentages from the WCA PDF (Jumbo Logistics Works Council Agreement, version P7-2025) before recording any registrations.**
3. After completing the wizard the main application unlocks.

---

## Typical Workflow

```
1. Go to On-Call → New Period
   Enter start and end date/time of your on-call shift.
   Mark any public holidays in the period.
   Click Calculate → review computed day entries.

2. Log incidents (if any)
   For each incident during the period:
   Go to On-Call → [period] → Add Incident
   Enter date, start time, end time.
   Click Calculate → review computed overtime entries.

3. View Reporting Screen
   Go to Report → [period]
   Review all on-call and overtime line items.
   Use quick-edit popups to correct any entries.

4. Submit to HR
   Use the reporting screen as your reference when entering
   data in MyHR (Manage Personal Contributions).
```

---

## Production Build

```bash
# Backend — produces a fat JAR
cd backend && mvn package -DskipTests
java -jar target/duty-tracker-backend-*.jar

# Frontend — produces a static build
cd frontend && pnpm build && pnpm preview
```

Or build and run everything with Docker Compose (recommended):

```bash
docker compose -f docker-compose.yml up --build
```

---

## Resetting the Application

To wipe all data and start fresh (including engineer profile and all registrations):

```bash
# Stop services, remove the postgres volume, restart
docker compose down -v
docker compose up --build
```

> **Warning**: This permanently deletes all registration summaries and the engineer profile.

For local development (without Docker volumes):

```bash
# Drop and recreate the database
psql -U dutytracker -c "DROP DATABASE dutytracker;"
psql -U dutytracker -c "CREATE DATABASE dutytracker;"
# Flyway will re-apply all migrations on next backend start
```
