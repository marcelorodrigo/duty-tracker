# AGENTS.md

## Repo layout

Monorepo with two independent sub-projects and no shared tooling:

- `backend/` — Spring Boot 4.1.2 / Java 25 / Maven. **The only place `./mvnw` exists** (there is no root POM).
- `frontend/` — Nuxt 4.6 SPA (`ssr: false`) / Vue 3 / pnpm 11.6.0 / Node 26 (root `.nvmrc`).

Always `cd` into the sub-project first. Nothing builds from the repo root.

---

## Backend

Run from `backend/`.

```bash
./mvnw clean package              # compile + tests + Spotless check (fails on unformatted code)
./mvnw package -DskipTests        # skip tests
./mvnw test -Dtest=SomeUseCaseTest
./mvnw test -Dtest=SomeUseCaseTest#methodName
./mvnw spotless:apply             # fix formatting (Palantir Java Format 2.90.0)
./mvnw generate-sources           # regenerate OpenAPI interfaces only
```

### Package root and wiring

Root package is **`com.github.marcelorodrigo.dutytracker`** (not `com.dutytracker`, despite the Maven `groupId`). Layout:

```
configuration/    BusinessClock — business timezone Europe/Amsterdam + Clock bean
                  (all LocalDate/LocalTime/LocalDateTime values are implicitly Amsterdam time)
domain/           records + domain exceptions, no Spring, no I/O
usecase/          one UseCase<Request, Response> class per operation, @Service + @Transactional
  request/        hand-written request records
  response/       hand-written response records (returned straight by controllers)
  validator/      one validator per use case; called as the first line of execute()
gateway/          controllers/, postgres/ (JPA adapters), holiday/, calendarfeed/
infrastructure/   Spring config, converters, CORS, app properties
```

The recurring pattern: a use case invokes its validator, loads/creates domain records through a gateway interface, and returns a response record. Mappers are MapStruct, boilerplate is Lombok (`@RequiredArgsConstructor`) — never hand-write what they generate.

`ArchitectureTest` (ArchUnit) enforces three rules that are easy to break:

- `domain` and `usecase` must not depend on `infrastructure`, `gateway.postgres`, or `gateway.controllers`.
- No field `@Autowired` injection anywhere (constructor injection only).
- `ical4j` is only allowed inside `gateway.calendarfeed`.

### Contract-first API

`src/main/resources/openapi/openapi.yaml` is the single source of truth. Controllers implement interfaces generated from it (`*Api`, e.g. `OnCallPeriodController implements OnCallPeriodsApi`), while request/response types are **hand-written use case records** wired in through the plugin's `importMappings`. Schema names in the YAML must match those records.

When changing the API: edit the YAML → add/rename the `importMapping` if needed → `./mvnw generate-sources` → update the controller. Swagger UI serves this static file at `/swagger-ui.html`.

Flyway migrations live in `src/main/resources/db/migration/`. Never edit an existing `V*` file; append a new one. `ddl-auto` is `validate`, so entity changes without a migration fail at startup.

### Testing

- Unit tests: Mockito, no Spring context, no DB (`usecase/`, gateway adapters).
- Controller tests: `@WebMvcTest` slices (requires `spring-boot-starter-webmvc-test`, already added).
- Testcontainers is used only by `gateway/postgres/integration/PaginationTest`. It is guarded by `DockerAvailableCondition` and **silently skips** when Docker is not running, so a green local build does not prove it passed.

---

## Frontend

Run from `frontend/`.

```bash
pnpm install         # postinstall runs `nuxt prepare`; required before typecheck on a fresh clone
pnpm dev             # http://localhost:3000
pnpm build           # static SPA output in .output/public/
pnpm lint
pnpm typecheck
pnpm test            # both vitest projects (watch mode; use `pnpm test --run` for one-shot)
pnpm test:unit       # test/unit/ only
pnpm test:nuxt       # test/nuxt/ only
pnpm test:coverage
```

### Structure and state

Nuxt 4 uses `app/` as the source dir (`app/pages`, `app/components`, `app/composables`, `app/queries`, `app/utils`, `app/types`).

State lives in two layers — follow this instead of calling Colada directly:

- `app/queries/*.ts` — declarative `defineQueryOptions` / mutation definitions with keys from `app/queries/keys.ts` (`QUERY_KEYS`, tree-shaped for invalidation).
- `app/composables/*.ts` — thin wrappers exposing `data` / `pending` / `error` / `refresh` to components.

Components must not call `useQuery` / `useMutation` directly. Full conventions: `frontend/docs/state-management.md`.

### Tests

| Project | Location | Environment |
|---|---|---|
| `unit` | `test/unit/*.{test,spec}.ts` | node |
| `nuxt` | `test/nuxt/*.{test,spec}.ts` | nuxt + happy-dom |

Wrong folder = not picked up by the intended project. Shared helpers are in `test/utils/`: `setupFetchMock` (mocks ofetch's `$fetch`), `withComposable` (mounts a composable with Pinia + Pinia Colada so `useQuery` works outside a component), and `factories.ts`.

### Quirks

- Fully static SPA: `ssr: false`, `prerender` only for `/`, served by nginx with an `index.html` fallback.
- `NUXT_PUBLIC_API_BASE` defaults to `http://localhost:8080` and is read **at build time**. Do not append `/api/v1` — request paths already include it. There is no dev proxy; backend CORS allows `http://localhost:3000`.
- `frontend/tsconfig.json` just delegates to the generated `.nuxt/tsconfig.json`; run `pnpm install` (or `nuxt prepare`) before `pnpm typecheck`.
- Use the pnpm version from `package.json` (`11.6.0`), not npm/yarn. `frontend/Dockerfile` still pins `pnpm@11.1.3` via corepack — update both together.
- Dates/times: use Nuxt UI's `UInputDate` with `@internationalized/date`. `app/app.vue` wraps everything in `UApp :locale="en_gb"`, so inputs render `dd/mm/yyyy` and date+time in a single field with `granularity="minute"`. Do not use `<input type="datetime-local">`.

---

## Domain, specs, and OpenSpec

- `doc/` holds the compensation rules (standby hours, overtime, allowance percentages, edge cases). Change calculation behavior only against those docs.
- `openspec/specs/` holds capability specs; `openspec/changes/` holds in-flight proposals (`proposal.md` / `design.md` / `tasks.md` + delta specs). Check for an active change (and its delta specs) before implementing a feature; `tasks.md` checkboxes are the progress log.
- OpenSpec slash commands live in `.opencode/commands/` (`/opsx-propose`, `/opsx-apply`, `/opsx-archive`, `/opsx-sync`, `/opsx-explore`) and expect the `openspec` CLI, which is not installed here. Read/write the Markdown artifacts directly instead.

---

## Full-stack local dev (repo root)

```bash
./run.sh                # docker compose down + build + up, foreground
docker compose up --build
./feed.sh               # seed periods/incidents via the API (backend must be up)
```

Backend `:8080`, frontend `:3000` (nginx), Swagger UI `:8080/swagger-ui.html`, PostgreSQL `:5432` (`dutytracker`/`dutytracker`). `run.sh` uses `docker compose down` (keeps the named volume), so seeded data survives unless you pass `-v`.

---

## CI and commits

- `frontend-ci.yml`: `pnpm install` → `pnpm lint`, plus a Sonar job running `pnpm build` → `pnpm test:coverage`. It does **not** run `typecheck`, so run it locally.
- `backend-ci.yml`: `./mvnw -ntp -T1C verify … sonar` from `backend/` (Temurin 25) — Spotless, ArchUnit, and all tests run there.
- Workflows are path-filtered (`frontend/**`, `backend/**`) and pinned to `ubuntu-26.04`; both Sonar jobs skip Dependabot PRs.
- Commit messages follow Conventional Commits (`feat(backend):`, `fix(frontend):`, `chore(deps):`). No pre-commit hooks.
