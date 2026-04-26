# Implementation Plan: On-Call Hours Tracker

**Branch**: `001-oncall-hours-tracker` | **Date**: 2026-04-25 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/001-oncall-hours-tracker/spec.md`

## Summary

A personal on-call hours management tool for a single engineer. The backend is a Spring Boot 3.x REST API using Spring Data JDBC against a local SQLite database. The frontend is a Nuxt 3 SPA. On first launch a multi-step onboarding wizard collects the engineer's profile, display preferences, and Compensation Rate Table defaults. The system then calculates on-call day entries and overtime entries per the WCA policy rules and presents a reporting screen for manual HR system entry.

## Technical Context

**Language/Version**: Java 25 (backend), TypeScript / Node.js 24 LTS (frontend)  
**Primary Dependencies**:
- Backend: Spring Boot 4.x (latest), Spring Data JDBC, `org.postgresql:postgresql`, `org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql`
- Frontend: Nuxt 4 (latest, via `pnpm create nuxt@latest`), `@nuxt/ui` (v3 — bundles Tailwind CSS v4, `@nuxtjs/color-mode`, `@nuxt/icon`, `@nuxt/fonts`), `@pinia/nuxt`, `@vueuse/nuxt`, `dayjs-nuxt`

**Storage**: PostgreSQL 18 — runs as a Docker container; no local install required  
**Testing**: Backend — JUnit 5, AssertJ, Mockito, Spring Boot Test; Frontend — Vitest, Vue Test Utils  
**Target Platform**: Local web application served at `localhost`; mobile-first responsive design — fully functional on phone and tablet browsers, scales to desktop  
**Project Type**: Web application — Spring Boot REST API backend + Nuxt 4 SPA frontend, both dockerized  
**Performance Goals**: Sub-second response for all API calls; single-user load, no concurrency requirements  
**Constraints**: Local deployment via Docker Compose; no external network required at runtime; constructor injection only (`@Autowired` on fields is forbidden)  
**Scale/Scope**: 1 user, ~52 registration summaries per year, small data footprint

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Gate | Check | Status |
|------|-------|--------|
| **CA-01** | No domain/application class imports infrastructure or presentation types | ✅ Enforced by package structure — domain and application packages have no imports from `infrastructure` or `presentation` |
| **CA-02** | Every business operation is represented by a dedicated UseCase class | ✅ All 22 business operations mapped to dedicated UseCase classes (see data-model.md) |
| **CA-03** | Every UseCase has a corresponding Request record and RequestValidator | ✅ Every UseCase listed in data-model.md has a paired `Request` record and `RequestValidator` |
| **CC-01** | No business logic exists in controllers or gateway implementations | ✅ Controllers delegate entirely to UseCases; JDBC repositories contain only I/O translation |
| **CC-02** | No field injection (`@Autowired` on fields) anywhere in the codebase | ✅ Constructor injection exclusively; enforced via ArchUnit test (see S-01) |
| **T-01** | Every UseCase, Validator, and Controller has a corresponding test class | ✅ Test class required for every UseCase, Validator, and Controller before merging |
| **S-01** | Every new dependency or abstraction layer is documented in Complexity Tracking | ✅ All four non-standard dependencies documented below |

## Project Structure

### Documentation (this feature)

```text
specs/001-oncall-hours-tracker/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── api.md           ← Phase 1 output
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/dutytracker/
│   │   │   ├── domain/
│   │   │   │   ├── model/          # Entities and value objects (Java records)
│   │   │   │   ├── exception/      # Domain exceptions
│   │   │   │   └── gateway/        # Gateway interfaces (ports)
│   │   │   ├── application/
│   │   │   │   └── usecase/        # UseCase classes, Request records, Validators
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/    # Spring Data JDBC repositories, row mappers
│   │   │   │   └── holiday/        # Jollyday-backed PublicHolidayGateway implementation
│   │   │   └── presentation/
│   │   │       └── api/            # REST controllers, request/response DTOs
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/      # Flyway versioned migrations (V1__, V2__, ...)
│   └── test/
│       └── java/com/dutytracker/
│           ├── application/        # UseCase unit tests (mocked gateways)
│           ├── infrastructure/     # JDBC integration tests
│           └── presentation/       # MockMvc controller tests
├── Dockerfile
└── pom.xml

frontend/
├── pages/
│   ├── onboarding/
│   │   └── index.vue              # Multi-step wizard
│   ├── index.vue                  # Dashboard / home
│   ├── oncall/
│   │   ├── index.vue              # On-call periods list
│   │   └── [id].vue               # On-call period detail + incident entry
│   ├── report/
│   │   ├── index.vue              # Registration summaries list
│   │   └── [id].vue               # Full reporting screen
│   └── settings/
│       └── index.vue              # Profile, preferences, Compensation Rate Table
├── components/
│   ├── onboarding/                # Wizard step components
│   ├── oncall/
│   ├── report/
│   └── shared/                    # Reusable UI components
├── composables/
│   ├── useOnCallCalculation.ts
│   └── useOvertimeCalculation.ts
├── stores/
│   ├── profile.ts
│   ├── preferences.ts
│   ├── oncall.ts
│   └── report.ts
├── Dockerfile
├── nuxt.config.ts
├── tailwind.config.ts
└── package.json

docker-compose.yml                 # Spins up postgres + backend + frontend
```

**Structure Decision**: Option 2 (Web application) — `backend/` for the Spring Boot REST API, `frontend/` for the Nuxt 4 SPA. They communicate exclusively over REST at `http://localhost:8080/api/v1`. PostgreSQL runs as a Docker container alongside both services.

## Complexity Tracking

| Dependency / Deviation | Why Needed | Simpler Alternative Rejected Because |
|------------------------|------------|--------------------------------------|
| `org.postgresql:postgresql` | PostgreSQL JDBC driver; required for Spring Data JDBC → PostgreSQL connection | H2/SQLite: lose production-parity; PostgreSQL is the target database |
| `org.flywaydb:flyway-core` + `flyway-database-postgresql` | Database schema versioning; schema changes tracked as versioned migration scripts | `spring.sql.init`: no versioning, no rollback tracking, not suitable beyond initial setup |
| `de.focus-shift:jollyday-core` | Accurate Dutch public holiday calendar for on-call rate calculation; holidays change yearly | Hardcoded list: brittle, requires annual code update; external API: requires internet at runtime |
| `@nuxt/ui` v3 | 54 core components (Table, Modal, Form, Button, Badge, etc.), Tailwind CSS v4, `@nuxtjs/color-mode`, `@nuxt/icon`, `@nuxt/fonts` — all bundled; dark/light/auto is a specified FR | Manual Tailwind + separate component libs: significant boilerplate, less cohesive design system |
| `@vueuse/nuxt` | Composable utilities auto-imported (`useDateFormat`, `useLocalStorage`, `useConfirmDialog`, etc.) — avoids reinventing common patterns | Manual implementation: time-consuming, error-prone |
| `dayjs-nuxt` | Dutch locale date formatting (`nl`) across all date display in the UI | `Intl.DateTimeFormat` directly: more verbose, no auto-import |
| Docker + Docker Compose | Reproducible local environment; PostgreSQL 18 runs as a container — no host install required | Host-installed PostgreSQL: environment-dependent, harder to reset and share |
