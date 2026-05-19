# AGENTS.md

## Repo structure

Monorepo with two independent sub-projects:

- `backend/` — Spring Boot 4 / Java 25 / Maven
- `frontend/` — Nuxt 4 SPA / Vue 3 / pnpm 11.1.3

All frontend commands must be run from `frontend/`. All backend commands from `backend/` (or repo root using the Maven wrapper).

---

## Backend

### Commands

```bash
# Build + test (runs Spotless format check too)
./mvnw clean package

# Skip tests
./mvnw package -DskipTests

# Single test class
./mvnw test -Dtest=SomeUseCaseTest
```

### Spotless (code formatter)

Uses **Palantir Java Format 2.90.0**. Runs automatically as part of `package`/`verify`. If formatting is wrong, the build fails. Fix with:

```bash
./mvnw spotless:apply
```

### Testing

- Unit tests (`usecase/`) — no Spring context, no DB
- Controller tests (`gateway/controllers/`) — `@WebMvcTest` slices
- Integration tests — **Testcontainers** spins up a real PostgreSQL container; Docker must be running locally
- Architecture tests (`ArchitectureTest.java`) — ArchUnit enforces package boundaries; do not violate `domain → usecase → gateway` dependency direction

### Architecture (Clean Architecture)

```
domain/          ← entities, exceptions (no external deps)
usecase/         ← business logic, request/response/validator subpackages
gateway/         ← controllers, postgres adapters, holiday, mappers
infrastructure/  ← Spring config, converters
```

MapStruct mappers and Lombok are compile-time generated — do not hand-write what they produce.

Flyway migrations live in `src/main/resources/db/migration/`. Never modify existing `V*` scripts; add a new one.

---

## Frontend

### Commands (run from `frontend/`)

```bash
pnpm install         # also runs `nuxt prepare` via postinstall
pnpm dev             # dev server with /api proxy → localhost:8080
pnpm build           # static SPA output to .output/public/
pnpm lint            # eslint
pnpm typecheck       # nuxt typecheck
pnpm test            # all vitest projects
pnpm test:unit       # unit project only (test/unit/)
pnpm test:nuxt       # nuxt project only (test/nuxt/)
```

### Test projects (vitest)

| Project | Pattern | Environment |
|---------|---------|-------------|
| `unit` | `test/unit/*.{test,spec}.ts` | node |
| `nuxt` | `test/nuxt/*.{test,spec}.ts` | nuxt + happy-dom |

Place tests in the right folder or they won't be picked up by the correct environment.

### Key quirks

- `ssr: false` — this is a fully static SPA. No server-side rendering.
- Dev proxy: `/api/*` → `http://localhost:8080/api` (configured in `nuxt.config.ts`). No CORS changes needed locally.
- `frontend/tsconfig.json` delegates entirely to `.nuxt/tsconfig.json` (generated). Run `pnpm install` or `nuxt prepare` before typechecking if `.nuxt/` is missing.
- Package manager is pinned: use **pnpm**, not npm/yarn.
- **Date/Time inputs**: Uses Nuxt UI v4 `UInputDate` component backed by `@internationalized/date`. The `UApp` component in `app.vue` is configured with `locale="enGB"` which enforces `dd/mm/yyyy` format for all date inputs and renders combined date+time fields in one input field with `granularity="minute"`. Do not use native `<input type="datetime-local">` — always use `UInputDate`.

---

## Full stack local dev

```bash
# Start everything (tears down volumes first)
./run.sh

# Or manually
docker compose up --build
```

Backend: `http://localhost:8080`  
Frontend: `http://localhost:3000`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## CI

- `frontend-ci.yml`: `pnpm install` → `pnpm build` → `pnpm test` (Node 26)
- `backend-ci.yml`: `./mvnw clean package -B -q -T 1C` (Java 25, Temurin)

CI does not run `lint` or `typecheck` for frontend — do these locally before pushing.

No pre-commit hooks. No Makefile.
