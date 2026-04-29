# AGENTS.md

Agent instructions for the `duty-tracker` repository.

## Repository layout

```
duty-tracker/
├── backend/    ← Spring Boot 4 Maven project (Java 25)
├── frontend/   ← Nuxt 4 SPA (Vue 3, pnpm, Node 24)
└── docker-compose.yml
```

Two independent subprojects; CI pipelines are also separate. Each has its own commands, package manager, and test runner.

---

## Backend (`backend/`)

### Commands

| Task | Command |
|---|---|
| Build + test + format check | `./mvnw clean package` |
| Tests only | `./mvnw test` |
| Apply formatting | `./mvnw spotless:apply` |
| Check formatting only | `./mvnw spotless:check` |
| Build, skip tests | `./mvnw clean package -DskipTests` |
| Run locally | `./mvnw spring-boot:run` |

**Always run `./mvnw spotless:apply` before committing.** Spotless (Palantir Java Format) is bound to the `package` lifecycle — a build fails if formatting is off.

### Architecture rules (enforced by ArchUnit at test time)

- `domain/` and `usecase/` must stay pure — no Spring, JPA, or Hibernate imports.
- `gateway/` is the only layer that may depend on Spring/JPA/Hibernate.
- **No `@Autowired` field injection anywhere.** Constructor injection only. Violation = test failure.

Package mapping:
- `domain/` — enums, value objects, domain exceptions
- `usecase/` — use cases, port interfaces, request/response DTOs, validators
- `gateway/controllers/` — REST controllers (inbound)
- `gateway/postgres/` — JPA repositories + entity mappers (outbound)
- `infrastructure/` — Spring `@Configuration`, converters

### Annotation processor order

`pom.xml` wires `lombok` → `lombok-mapstruct-binding` → `mapstruct-processor` in that exact order. Do not change this ordering.

### Database

- Flyway manages schema (`src/main/resources/db/migration/`). Hibernate `ddl-auto=validate` — it validates but never modifies the schema.
- Three migrations: V1 creates all tables, V2/V3 seed compensation rates.
- Compensation rates ship as `0.00` — onboarding wizard expects the user to fill them in.

Environment variables with defaults:

| Var | Default |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `dutytracker` |
| `DB_USER` | `dutytracker` |
| `DB_PASSWORD` | `dutytracker` |

### Testing

- `usecase/**` — pure unit tests, Mockito-mocked ports, no Spring context, no DB.
- `gateway/controllers/**` — `@WebMvcTest` slice tests (requires `spring-boot-starter-webmvc-test`, separate from the regular test starter in Spring Boot 4).
- `gateway/postgres/**` — directories exist but are **empty**; Testcontainers is declared as a dependency and ready to use.
- `gateway/holiday/**` — Jollyday integration tests (offline, no DB).

---

## Frontend (`frontend/`)

### Commands

| Task | Command |
|---|---|
| Install deps | `pnpm install` |
| Dev server | `pnpm dev` |
| Run tests | `pnpm test` |
| Production build (Docker) | `pnpm generate` → `.output/public/` |
| Preview generated output | `pnpm preview` |

**Use `pnpm` only.** `packageManager` is pinned to `pnpm@10.33.2`. npm/yarn are unsupported.

There is no ESLint config and no `vue-tsc` typecheck script — Vitest is the only code quality gate.

### Key facts

- `ssr: false` — pure SPA. No server components, no server routes, no SSR.
- API calls go directly from the browser to the backend (`localhost:8080` by default).
- Override backend URL with `NUXT_PUBLIC_API_BASE`.
- Day.js locale is Dutch (`nl`) globally — all date formatting is Dutch by default.
- Tests use Vitest + `happy-dom`; no real browser or external service needed.
- Tests live in `__tests__/` and are currently minimal.

---

## Full stack

```bash
docker compose up --build   # start postgres + backend + frontend
```

The frontend Docker build uses `pnpm generate` (static export) and serves `.output/public` via Nginx with a catch-all SPA rule.

---

## CI

| Workflow | Trigger | Steps |
|---|---|---|
| `backend-ci.yml` | push/PR on `backend/**` | `./mvnw clean package -B -q -T 1C` (includes Spotless + tests) |
| `frontend-ci.yml` | push/PR on `frontend/**` | `pnpm install` → `pnpm build` → `pnpm test` |
