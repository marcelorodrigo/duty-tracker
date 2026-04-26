# Tasks: On-Call Hours Tracker

**Input**: Design documents from `specs/001-oncall-hours-tracker/`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/api.md ✅ quickstart.md ✅

**Tests**: Included — required by Constitution gate T-01 (every UseCase, Validator, and Controller must have a test class before merging).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing. US5 (Onboarding, P1) must be completed before US1 since profile setup gates all registrations.

---

## Phase 1: Project Setup (Shared Infrastructure)

**Purpose**: Scaffolding for all services (postgres, backend, frontend) so any story can be developed and run immediately.

- [ ] T001 Create `backend/pom.xml` — Spring Boot 4.x parent, Java 25, dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jdbc`, `postgresql`, `flyway-core`, `flyway-database-postgresql`, `jollyday-core 0.26.0`, `spring-boot-starter-validation`, `spring-boot-starter-test`, `archunit-junit5`
- [ ] T002 [P] Create `backend/src/main/java/com/dutytracker/DutyTrackerApplication.java` — `@SpringBootApplication` main class
- [ ] T003 Bootstrap Nuxt 4 frontend: `pnpm create nuxt@latest frontend`, then `pnpm add @nuxt/ui @pinia/nuxt @vueuse/nuxt dayjs-nuxt`; create `frontend/package.json` with all installed dependencies recorded
- [ ] T004 [P] Create `frontend/nuxt.config.ts` — modules: `['@nuxt/ui', '@pinia/nuxt', '@vueuse/nuxt', 'dayjs-nuxt']`; colorMode `{ preference: 'system', fallback: 'light' }`; dayjs `{ locales: ['nl'], defaultLocale: 'nl' }`; runtimeConfig `{ public: { apiBase: 'http://localhost:8080/api/v1' } }`
- [ ] T005 [P] Create `frontend/assets/css/main.css` — `@import "tailwindcss";` and `@import "@nuxt/ui";` (Tailwind v4 CSS-first)
- [ ] T006 [P] Create `.npmrc` at repo root — `shamefully-hoist=true` and `strict-peer-dependencies=false`
- [ ] T007 [P] Create `backend/Dockerfile` — multi-stage: stage 1 `eclipse-temurin:25-jdk` + Maven build (`mvn package -DskipTests`), stage 2 `eclipse-temurin:25-jre-alpine` runtime; `EXPOSE 8080`; `ENTRYPOINT ["java", "-jar", "app.jar"]`
- [ ] T008 [P] Create `frontend/Dockerfile` — multi-stage: stage 1 `node:24-alpine` + pnpm install + `pnpm build`, stage 2 `nginx:alpine` serving `.output/public`; include `frontend/nginx.conf` with SPA fallback (`try_files $uri /index.html`); `EXPOSE 80`
- [ ] T009 Create `docker-compose.yml` at repo root — three services: `postgres` (image `postgres:18-alpine`, env `POSTGRES_DB/USER/PASSWORD: dutytracker`, named volume `postgres_data`, healthcheck `pg_isready -U dutytracker`), `backend` (build `./backend`, env DB_HOST/PORT/NAME/USER/PASSWORD, `depends_on postgres: condition: service_healthy`), `frontend` (build `./frontend`, env `NUXT_PUBLIC_API_BASE=http://localhost:8080/api/v1`, depends on backend); expose 8080 and 3000

**Checkpoint**: `docker compose up --build` starts all three services without application errors.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model, persistence layer, and cross-cutting infrastructure that MUST exist before any user story work begins.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T010 Create `backend/src/main/resources/db/migration/V1__create_schema.sql` — PostgreSQL DDL for all 9 tables from data-model.md: `engineer_profile`, `user_preferences`, `compensation_rate`, `on_call_period`, `holiday_override`, `on_call_day_entry`, `incident`, `overtime_entry`, `registration_summary` with all constraints, FK references with CASCADE/SET NULL, and CHECK constraints for enum columns
- [ ] T011 Create `backend/src/main/resources/db/migration/V2__seed_compensation_rates.sql` — placeholder seed rows (`0.0000` percentage) for all base rate categories for both INTERNAL and EXTERNAL employee types; add a structured file header block (`-- WCA PLACEHOLDER: percentages below must be updated from the WCA PDF (Jumbo Logistics WCA, version P7-2025) before production use`) — do not use `TODO` keyword (Constitution III.Clean Code)
- [ ] T012 [P] Create domain enums in `backend/src/main/java/com/dutytracker/domain/model/`: `EmployeeType.java` (INTERNAL, EXTERNAL), `ColorScheme.java` (DARK, LIGHT, AUTO), `StandbyRateType.java` (WEEKDAY_SATURDAY, SUNDAY_HOLIDAY), `RateCategory.java` (ONCALL_WEEKDAY_SATURDAY, ONCALL_SUNDAY_HOLIDAY, OVERTIME_BASE, OVERTIME_ALLOWANCE), `OnboardingStep.java` (PROFILE, PREFERENCES, COMPENSATION_RATES, COMPLETE)
- [ ] T013 [P] Create all 8 domain exception classes in `backend/src/main/java/com/dutytracker/domain/exception/`: `ProfileAlreadyExistsException`, `ProfileLockedException`, `OnboardingNotCompletedException`, `InvalidOnCallPeriodException`, `InvalidIncidentException`, `HolidayAlreadyRegisteredException`, `IncidentDuringWorkingHoursException`, `OvertimeDayOffException` — extend `RuntimeException`, include message constructor
- [ ] T014 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `EngineerProfile.java` (id, employeeType, workingDays Set<DayOfWeek>, workStartTime LocalTime, workEndTime LocalTime, createdAt Instant), `UserPreferences.java` (id, colorScheme, onboardingStep), `CompensationRate.java` (id, employeeType, rateCategory, label, timeFrom, timeTo, percentage BigDecimal)
- [ ] T015 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `OnCallPeriod.java` (id, startDateTime LocalDateTime, endDateTime LocalDateTime, createdAt Instant), `HolidayOverride.java` (id, onCallPeriodId Long, date LocalDate), `OnCallDayEntry.java` (id, onCallPeriodId, date, hours BigDecimal, rateType StandbyRateType, capped boolean, timeForTimeFlag boolean, manualOverride boolean)
- [ ] T016 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `Incident.java` (id, onCallPeriodId Long nullable, date LocalDate, startTime LocalTime, endTime LocalTime, createdAt Instant), `OvertimeEntry.java` (id, incidentId Long, overtimeHours BigDecimal, allowanceHours BigDecimal nullable, allowancePercentage BigDecimal nullable, timeFrom LocalTime nullable, timeTo LocalTime nullable, isAllowanceEntry boolean, manualOverride boolean), `RegistrationSummary.java` (id, label String, periodStart LocalDate, periodEnd LocalDate, createdAt Instant, updatedAt Instant)
- [ ] T017 [P] Create all 10 domain gateway interfaces in `backend/src/main/java/com/dutytracker/domain/gateway/`: `EngineerProfileGateway` (save, find), `UserPreferencesGateway` (save, find), `CompensationRateGateway` (saveAll, findAll, findByEmployeeType, update, deleteById), `OnCallPeriodGateway` (save, findById, findAll, deleteById), `HolidayOverrideGateway` (save, findByOnCallPeriodId, deleteById), `OnCallDayEntryGateway` (save, saveAll, findByOnCallPeriodId, findById, deleteById), `IncidentGateway` (save, findById, findByOnCallPeriodId, findAll, deleteById), `OvertimeEntryGateway` (save, saveAll, findByIncidentId, findById, deleteById), `RegistrationSummaryGateway` (save, findById, findAll, deleteById, existsAny), `PublicHolidayGateway` (isHoliday(LocalDate), getHolidays(int year))
- [ ] T018 [P] Create `UseCase<Req, Res>` functional interface and `RequestValidator<Req>` interface in `backend/src/main/java/com/dutytracker/application/usecase/`; create `UseCase.java` with single method `Res execute(Req request)` and `RequestValidator.java` with single method `void validate(Req request)` (throws domain exceptions on failure)
- [ ] T019 Create `WorkingDaysConverter.java` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/converter/` — two static inner classes: `Write` (`@WritingConverter`, `Set<DayOfWeek>` → `String` comma-separated) and `Read` (`@ReadingConverter`, `String` → `Set<DayOfWeek>`); register via `JdbcConfiguration` bean
- [ ] T020 Create all Spring Data JDBC repository implementations in `backend/src/main/java/com/dutytracker/infrastructure/persistence/` — one `JdbcXxxGateway` class per gateway interface (10 classes total); use `NamedParameterJdbcTemplate` for all queries; implement all methods declared by the gateway interface; constructor injection only
- [ ] T021 [P] Create `backend/src/main/resources/application.yml` — datasource (`jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}`), username/password with env-var defaults, Flyway config (`enabled: true`, `locations: classpath:db/migration`), server port 8080
- [ ] T022 [P] Create `CorsConfiguration.java` in `backend/src/main/java/com/dutytracker/infrastructure/config/` — `@Configuration` bean implementing `WebMvcConfigurer`; allow `http://localhost:3000` for all `/api/**` paths; allow all standard HTTP methods and headers
- [ ] T023 [P] Create `GlobalExceptionHandler.java` in `backend/src/main/java/com/dutytracker/presentation/api/` — `@RestControllerAdvice`; one `@ExceptionHandler` per domain exception; map each to RFC 7807 `ProblemDetail` with correct HTTP status (409 for conflicts/locks, 400 for validation, 404 for not found) and typed `type` URI (e.g., `https://dutytracker/errors/profile-locked`)
- [ ] T024 [P] Create `ArchitectureTest.java` in `backend/src/test/java/com/dutytracker/` — ArchUnit tests: (1) no imports from `infrastructure` or `presentation` in `domain` or `application`; (2) no `@Autowired` annotation on fields anywhere; (3) all Spring beans use constructor injection
- [ ] T025 [P] Create `frontend/composables/useApi.ts` — typed `$fetch` wrapper using `useRuntimeConfig().public.apiBase`; parse RFC 7807 `ProblemDetail` error responses into typed errors; surface errors via `useToast()` (Nuxt UI); export `useApi()` composable returning `get`, `post`, `put`, `delete` typed methods
- [ ] T026 [P] Create `frontend/composables/useApiError.ts` — maps `ProblemDetail.type` URI to human-readable Dutch/English messages for all 8 domain exceptions; export `getErrorMessage(problemDetail)` helper used by all stores
- [ ] T027 [P] Create `frontend/stores/profile.ts` — Pinia store: `profile` state (EngineerProfile | null), `fetchProfile`, `createProfile`, `updateProfile` actions using `useApi()`; expose `isLocked` computed from profile response
- [ ] T028 [P] Create `frontend/stores/preferences.ts` — Pinia store: `preferences` state (UserPreferences | null), `fetchPreferences`, `updatePreferences` actions; call `useColorMode().preference` setter with mapped value on update
- [ ] T029 [P] Create `frontend/stores/oncall.ts` — Pinia store: `periods` (OnCallPeriod[]), `currentPeriod` (OnCallPeriod | null), `dayEntries` (OnCallDayEntry[]), `incidents` (Incident[]), `overtimeEntries` (OvertimeEntry[]) state; all CRUD actions plus `calculate` actions for both entries
- [ ] T030 [P] Create `frontend/stores/report.ts` — Pinia store: `summaries` (RegistrationSummary[]), `currentSummary` (full summary with entries | null) state; all CRUD and entry override/add/delete actions
- [ ] T031 Create `frontend/middleware/onboarding.global.ts` — global route middleware: call `GET /api/v1/onboarding/status`; if `completed === false` and current route is not `/onboarding`, redirect to `/onboarding`; skip redirect on `/onboarding` route
- [ ] T111 [P] Create Spring Data JDBC integration tests in `backend/src/test/java/com/dutytracker/infrastructure/persistence/` — one `@SpringBootTest` + `@Testcontainers` test class per JDBC gateway implementation (10 classes); use `@Container` PostgreSQL Testcontainers instance; verify CRUD operations and all non-trivial query methods (e.g., `findByOnCallPeriodId`, `existsAny`) against a real PostgreSQL 18 container; add `testcontainers-bom`, `testcontainers`, and `postgresql` Testcontainers dependencies to `pom.xml` under `<scope>test</scope>` (Constitution Principle IV — gateway integration tests are mandatory)

**Checkpoint**: Foundation complete — `mvn compile` passes, Flyway applies V1 + V2 cleanly, ArchUnit test compiles and passes (empty app), all domain types compile without errors.

---

## Phase 3: User Story 5 — First-Run Onboarding Setup (Priority: P1) 🎯 MVP Prerequisite

**Goal**: The engineer completes a 3-step wizard (Profile → Preferences → Compensation Rates) on first launch. All routes are blocked until onboarding is `COMPLETE`. Settings remain editable post-onboarding via `/settings`.

**Independent Test**: Launch with empty database — verify wizard appears, all other routes redirect to `/onboarding`, wizard progresses through all 3 steps in order, each step saves data, and the main app is accessible after the final step confirms `COMPLETE`.

### Backend — Onboarding, Profile & Compensation Rates

- [ ] T032 [P] [US5] `GetOnboardingStatusUseCase` + `GetOnboardingStatusRequest` (empty record) + `GetOnboardingStatusValidator` in `backend/src/main/java/com/dutytracker/application/usecase/onboarding/`; unit test: returns `PROFILE` step when no preferences row exists, returns saved step when preferences exist
- [ ] T033 [P] [US5] `AdvanceOnboardingStepUseCase` + `AdvanceOnboardingStepRequest` (currentStep OnboardingStep) + `AdvanceOnboardingStepValidator` (currentStep must match stored step); unit test: advances PROFILE→PREFERENCES→COMPENSATION_RATES→COMPLETE; error on mismatched step
- [ ] T034 [P] [US5] `CreateEngineerProfileUseCase` + `CreateEngineerProfileRequest` (employeeType, workingDays, workStartTime, workEndTime) + `CreateEngineerProfileValidator` (workingDays ≥ 1, workEndTime after workStartTime, no existing profile); unit test: creates profile successfully; throws `ProfileAlreadyExistsException` when profile exists
- [ ] T035 [P] [US5] `GetEngineerProfileUseCase` + `GetEngineerProfileRequest` (empty) + `GetEngineerProfileValidator`; unit test: returns profile with `locked=true` when `RegistrationSummaryGateway.existsAny()` is true, `locked=false` otherwise
- [ ] T036 [P] [US5] `UpdateEngineerProfileUseCase` + `UpdateEngineerProfileRequest` (employeeType, workingDays, workStartTime, workEndTime) + `UpdateEngineerProfileValidator`; unit test: updates profile when no registrations exist; throws `ProfileLockedException` when `RegistrationSummaryGateway.existsAny()` is true
- [ ] T037 [P] [US5] `UpdateUserPreferencesUseCase` + `UpdateUserPreferencesRequest` (colorScheme ColorScheme) + `UpdateUserPreferencesValidator`; unit test: creates preferences row if absent, updates if present
- [ ] T038 [P] [US5] `GetUserPreferencesUseCase` + `GetUserPreferencesRequest` (empty) + `GetUserPreferencesValidator`; unit test: returns defaults (`AUTO`, `PROFILE`) when no row exists
- [ ] T039 [P] [US5] `GetCompensationRateTableUseCase` + `GetCompensationRateTableRequest` (employeeType optional) + `GetCompensationRateTableValidator`; unit test: returns all rates; filters by employeeType when provided
- [ ] T040 [P] [US5] `UpdateCompensationRateUseCase` + `UpdateCompensationRateRequest` (rateId, percentage BigDecimal, label String) + `UpdateCompensationRateValidator` (percentage ≥ 0, rateId must exist); unit test
- [ ] T041 [P] [US5] `CreateCompensationRateUseCase` (OVERTIME_ALLOWANCE rows only) + `CreateCompensationRateRequest` (employeeType, label, timeFrom, timeTo, percentage) + `CreateCompensationRateValidator` (rateCategory must be OVERTIME_ALLOWANCE, timeFrom + timeTo required, no duplicate key); unit test
- [ ] T042 [P] [US5] `DeleteCompensationRateUseCase` + `DeleteCompensationRateRequest` (rateId) + `DeleteCompensationRateValidator` (only OVERTIME_ALLOWANCE rows may be deleted — base rows throw 409); unit test
- [ ] T043 [US5] `OnboardingController` in `backend/src/main/java/com/dutytracker/presentation/api/` — `GET /api/v1/onboarding/status` → `GetOnboardingStatusUseCase`, `POST /api/v1/onboarding/advance` → `AdvanceOnboardingStepUseCase`; constructor-inject both use cases; `OnboardingControllerTest` with MockMvc
- [ ] T044 [P] [US5] `ProfileController` — `POST /api/v1/profile` (201), `GET /api/v1/profile` (200), `PUT /api/v1/profile` (200, 409 if locked); `ProfileControllerTest` with MockMvc
- [ ] T045 [P] [US5] `PreferencesController` — `GET /api/v1/preferences` (200), `PUT /api/v1/preferences` (200); `PreferencesControllerTest` with MockMvc
- [ ] T046 [P] [US5] `CompensationRateController` — `GET /api/v1/compensation-rates` (optional `?employeeType`), `POST /api/v1/compensation-rates` (201), `PUT /api/v1/compensation-rates/{id}` (200), `DELETE /api/v1/compensation-rates/{id}` (204); `CompensationRateControllerTest` with MockMvc

### Frontend — Onboarding Wizard & Settings

- [ ] T047 [P] [US5] `frontend/stores/compensation.ts` — Pinia store: `rates` (CompensationRate[]) state; `fetchRates(employeeType?)`, `createRate`, `updateRate`, `deleteRate` actions using `useApi()`
- [ ] T048 [P] [US5] `frontend/components/onboarding/ProfileStep.vue` — `UForm` + `UFormField`: `USelect` for employee type (INTERNAL / EXTERNAL), `UCheckboxGroup` for working days (Mon–Fri checked by default), `UInput type="time"` for start/end time; form validation with Zod or Valibot; emits `saved` event on successful API call
- [ ] T049 [P] [US5] `frontend/components/onboarding/PreferencesStep.vue` — `UColorModeSwitch` (Nuxt UI built-in) for dark/light/auto toggle with immediate live preview; emits `saved` on `PUT /preferences`
- [ ] T050 [P] [US5] `frontend/components/onboarding/CompensationRatesStep.vue` — `UTable` displaying rates grouped by employeeType; `UInput` for percentage per row with inline save `UButton`; `UButton` to add new OVERTIME_ALLOWANCE row (`UModal` form); delete `UButton` for OVERTIME_ALLOWANCE rows only; `UAlert` warning about WCA placeholder values; emits `saved`
- [ ] T051 [US5] `frontend/pages/onboarding/index.vue` — wizard shell: `UStepper` or `UProgress` indicator (3 steps); on `onMounted` call `GET /api/v1/onboarding/status` and navigate directly to the step matching the returned `step` value (not always step 1 — satisfies FR-022 resume requirement); conditionally renders `ProfileStep`, `PreferencesStep`, `CompensationRatesStep`; calls `POST /onboarding/advance` after each `saved` event; on `COMPLETE` redirects to `/`
- [ ] T052 [P] [US5] `frontend/pages/settings/index.vue` — `UTabs` with three panels: Profile (reuses `ProfileStep` + locked `UBadge` when `isLocked`), Preferences (reuses `PreferencesStep`), Compensation Rates (reuses `CompensationRatesStep`); accessible post-onboarding
- [ ] T053 [P] [US5] `frontend/pages/index.vue` — home dashboard: `UCard` widgets linking to on-call periods (`/oncall`), registration summaries (`/report`), and settings (`/settings`); shows latest on-call period status if any; `UButton` "New On-Call Period" shortcut
- [ ] T112 [P] [US5] Implement profile-type UI adaptation (FR-020) — in `frontend/stores/profile.ts` expose `isExternal` computed (`employeeType === 'EXTERNAL'`); in `frontend/components/oncall/DayEntryTable.vue` and `frontend/components/report/SummaryEntryTable.vue` conditionally show/hide columns irrelevant to the engineer's type; in `frontend/components/onboarding/CompensationRatesStep.vue` and `frontend/stores/compensation.ts` filter the displayed and fetched rates by `profile.employeeType` so the engineer only sees rates relevant to their type

**Checkpoint**: Full onboarding flow works end-to-end — wizard appears on first launch, all other routes block, settings are editable at `/settings` after completion.

---

## Phase 4: User Story 1 — Register On-Call Hours for a Week (Priority: P1) 🎯 MVP

**Goal**: The engineer enters an on-call period, optionally marks holiday overrides, triggers calculation, and receives one on-call day entry per calendar day with correct hours, rate type, and cap applied.

**Independent Test**: POST an on-call period Mon 14:00 → following Mon 14:00 with no holidays; call `POST /oncall-periods/{id}/calculate`; verify 7 day entries: Mon–Sat get `WEEKDAY_SATURDAY`, Sunday gets `SUNDAY_HOLIDAY`, working days capped at 15h, non-working days allow up to 24h.

### Backend — On-Call Periods & Calculation

- [ ] T054 [US1] `JollydayPublicHolidayGateway.java` in `backend/src/main/java/com/dutytracker/infrastructure/holiday/` — implements `PublicHolidayGateway`; wraps `de.focus-shift:jollyday-core` with `HolidayCalendar.NETHERLANDS`; `isHoliday(LocalDate)` returns true for Dutch public holidays; `getHolidays(int year)` returns set of holiday dates for the year; integration test: verify Koningsdag (Apr 27), Eerste Kerstdag (Dec 25), Easter Monday for a known year
- [ ] T055 [P] [US1] `CreateOnCallPeriodUseCase` + `CreateOnCallPeriodRequest` (startDateTime LocalDateTime, endDateTime LocalDateTime) + `CreateOnCallPeriodValidator` (endDateTime after startDateTime, duration ≥ 1 hour, onboarding must be COMPLETE); unit test
- [ ] T056 [P] [US1] `GetOnCallPeriodUseCase` + `GetOnCallPeriodRequest` (periodId Long) + `GetOnCallPeriodValidator`; unit test: returns period with holidayOverrides list
- [ ] T057 [P] [US1] `ListOnCallPeriodsUseCase` + `ListOnCallPeriodsRequest` (empty) + `ListOnCallPeriodsValidator`; unit test
- [ ] T058 [P] [US1] `UpdateOnCallPeriodUseCase` + `UpdateOnCallPeriodRequest` (periodId, startDateTime, endDateTime) + `UpdateOnCallPeriodValidator` (same as create); unit test
- [ ] T059 [P] [US1] `DeleteOnCallPeriodUseCase` + `DeleteOnCallPeriodRequest` (periodId) + `DeleteOnCallPeriodValidator`; unit test
- [ ] T060 [P] [US1] `AddHolidayOverrideUseCase` + `AddHolidayOverrideRequest` (periodId, date LocalDate) + `AddHolidayOverrideValidator` (date must fall within period, no duplicate); unit test: success; throws `HolidayAlreadyRegisteredException` on duplicate
- [ ] T061 [P] [US1] `RemoveHolidayOverrideUseCase` + `RemoveHolidayOverrideRequest` (periodId, date LocalDate) + `RemoveHolidayOverrideValidator`; unit test
- [ ] T062 [US1] `CalculateOnCallDayEntriesUseCase` in `backend/src/main/java/com/dutytracker/application/usecase/oncall/` — implements the full per-day algorithm from data-model.md: (1) compute on-call hours per day from period start/end times respecting midnight boundary; (2) determine rateType (SUNDAY_HOLIDAY on Sundays and holidays, WEEKDAY_SATURDAY otherwise); (3) apply 15h cap on working days, 24h max on non-working days; (4) set `timeForTimeFlag` when working day + engineer-flagged day-off; persist results via `OnCallDayEntryGateway.saveAll()`; unit tests: full Mon–Mon week no holidays, Sunday rate, holiday rate (override), partial start day, partial end day, 15h cap on working day, no cap on non-working day, multi-holiday week, split-rate Monday (last day and first day of adjacent periods)
- [ ] T063 [US1] `OnCallPeriodController` in `backend/src/main/java/com/dutytracker/presentation/api/` — all endpoints from `contracts/api.md`: `POST /api/v1/oncall-periods` (201), `GET /api/v1/oncall-periods` (200), `GET /api/v1/oncall-periods/{id}` (200/404), `PUT /api/v1/oncall-periods/{id}` (200), `DELETE /api/v1/oncall-periods/{id}` (204), `POST /api/v1/oncall-periods/{id}/holidays` (200/409), `DELETE /api/v1/oncall-periods/{id}/holidays/{date}` (204/404), `POST /api/v1/oncall-periods/{id}/calculate` (200); `OnCallPeriodControllerTest` with MockMvc

### Frontend — On-Call Periods

- [ ] T064 [P] [US1] `frontend/composables/useOnCallCalculation.ts` — wraps `POST /oncall-periods/{id}/calculate`; stores returned entries in the `oncall` store; surfaces typed API errors via `useApi()` and `useToast()`
- [ ] T065 [P] [US1] `frontend/pages/oncall/index.vue` — `UTable` listing on-call periods (start, end, created); `UButton` "New Period" opens `UModal` with `PeriodForm`; per-row delete with `ConfirmDeleteModal`; each row links to `/oncall/{id}`
- [ ] T066 [US1] `frontend/pages/oncall/[id].vue` — period detail page: (1) period header with dates; (2) `HolidayOverrideList` section; (3) "Calculate On-Call Hours" `UButton` calling `useOnCallCalculation()`; (4) `DayEntryTable` showing computed entries; loading state with `USkeleton`
- [ ] T067 [P] [US1] `frontend/components/oncall/PeriodForm.vue` — `UForm` + `UFormField`: datetime `UInput` for start/end; Zod validation (end after start); emits `submit` with form data; `UAlert` for API validation errors
- [ ] T068 [P] [US1] `frontend/components/oncall/HolidayOverrideList.vue` — list of holiday dates as `UBadge` chips with remove `UButton`; `UPopover` with `UInput type="date"` to add a new holiday override; calls `POST /oncall-periods/{id}/holidays` and `DELETE /oncall-periods/{id}/holidays/{date}`
- [ ] T069 [P] [US1] `frontend/components/oncall/DayEntryTable.vue` — `UTable`: columns date, hours, rateType `UBadge` (color-coded), capped `UIcon`, timeForTimeFlag `UBadge` "Time-for-time" (read-only indicator); no interactive toggle in this phase — the "Mark as day off" `UToggle` is added in Phase 6 alongside the backend endpoint (T083–T084)
- [ ] T070 [P] [US1] `frontend/components/shared/ConfirmDeleteModal.vue` — reusable `UModal` with confirm/cancel buttons; emits `confirmed`; used across all list views for delete actions

**Checkpoint**: After T070, full on-call registration flow works end-to-end — create period, mark holidays, calculate, review day entries.

---

## Phase 5: User Story 2 — Record Overtime Hours for Incident Work (Priority: P2)

**Goal**: The engineer logs an incident with start/end time, triggers overtime calculation, and receives correctly rounded entries split by allowance time zone.

**Independent Test**: POST an incident on a known date outside working hours (e.g., 02:00–03:45); call `POST /incidents/{id}/calculate`; verify: 2 overtime hours (ceil), one base entry, one allowance entry with correct percentage and zone; also verify `IncidentDuringWorkingHoursException` when incident is entirely within working hours.

### Backend — Incidents & Overtime Calculation

- [ ] T071 [P] [US2] `LogIncidentUseCase` + `LogIncidentRequest` (onCallPeriodId Long nullable, date LocalDate, startTime LocalTime, endTime LocalTime) + `LogIncidentValidator` (date not in future; if onCallPeriodId present, date must fall within period; onboarding must be COMPLETE); unit test
- [ ] T072 [P] [US2] `UpdateIncidentUseCase` + `UpdateIncidentRequest` (incidentId, date, startTime, endTime) + `UpdateIncidentValidator`; unit test
- [ ] T073 [P] [US2] `DeleteIncidentUseCase` + `DeleteIncidentRequest` (incidentId) + `DeleteIncidentValidator`; unit test
- [ ] T074 [P] [US2] `ListIncidentsUseCase` + `ListIncidentsRequest` (onCallPeriodId Long optional filter) + `ListIncidentsValidator`; unit test
- [ ] T075 [US2] `CalculateOvertimeEntriesUseCase` in `backend/src/main/java/com/dutytracker/application/usecase/incident/` — implements full algorithm from data-model.md: (1) if `incident.onCallPeriodId` is non-null, look up `OnCallDayEntry` by `(onCallPeriodId, incident.date)` and throw `OvertimeDayOffException` if `timeForTimeFlag = true`; if `onCallPeriodId` is null skip this check (non-oncall incident — day-off enforcement not applicable); (2) exclude normal working hours from incident time range (unless holiday); (3) throw `IncidentDuringWorkingHoursException` if all hours within working hours; (4) split remaining segments by `OVERTIME_ALLOWANCE` zone boundaries from CompensationRate table; (5) for each sub-segment: `ceil(duration)` hours minimum 1h, create base entry (isAllowanceEntry=false), create allowance entry (isAllowanceEntry=true) with matching percentage; persist via `OvertimeEntryGateway.saveAll()`; unit tests: normal-hours exclusion, rounding up (03:45→2h), multi-zone split (21:00–23:00 Sat), holiday rate, overnight incident, `IncidentDuringWorkingHoursException`, `OvertimeDayOffException`, non-oncall incident skips day-off check
- [ ] T076 [US2] `IncidentController` in `backend/src/main/java/com/dutytracker/presentation/api/` — all endpoints: `POST /api/v1/incidents` (201), `GET /api/v1/incidents` (optional `?onCallPeriodId`), `GET /api/v1/incidents/{id}` (200/404), `PUT /api/v1/incidents/{id}` (200), `DELETE /api/v1/incidents/{id}` (204), `POST /api/v1/incidents/{id}/calculate` (200, 409 on working-hours/day-off conflicts); `IncidentControllerTest` with MockMvc

### Frontend — Incidents & Overtime

- [ ] T077 [P] [US2] `frontend/composables/useOvertimeCalculation.ts` — wraps `POST /incidents/{id}/calculate`; stores returned entries in oncall store; maps `incident-during-working-hours` and `overtime-day-off` ProblemDetail errors to `useToast()` warnings
- [ ] T078 [P] [US2] Extend `frontend/stores/oncall.ts` with incident CRUD actions: `logIncident`, `updateIncident`, `deleteIncident`, `listIncidents`; add `overtimeEntries` state per incident
- [ ] T079 [US2] Extend `frontend/pages/oncall/[id].vue` — add "Incidents" section below day entries: `UTable` listing incidents, "Add Incident" `UButton` opening `IncidentForm` modal, per-incident "Calculate Overtime" `UButton`, collapsible `OvertimeEntryTable` sub-row per incident
- [ ] T080 [P] [US2] `frontend/components/oncall/IncidentForm.vue` — `UForm` + `UFormField`: date `UInput`, start/end time `UInput`, optional on-call period select `USelect`; Zod validation; emits `submit`; `UAlert` for `IncidentDuringWorkingHoursException` and `OvertimeDayOffException` errors
- [ ] T081 [P] [US2] `frontend/components/oncall/OvertimeEntryTable.vue` — `UTable`: timeFrom–timeTo, isAllowanceEntry `UBadge`, overtimeHours, allowanceHours, allowancePercentage, manualOverride `UBadge`

**Checkpoint**: After T081, incident logging and overtime calculation work end-to-end independently.

---

## Phase 6: User Story 3 — Identify Time-for-Time Scenarios (Priority: P3)

**Goal**: The system proactively identifies days flagged as time-for-time, blocks overtime for those days, and guides the engineer with a clear message.

**Independent Test**: Flag a day entry as day-off via the toggle in `DayEntryTable`; attempt to calculate overtime for an incident on that day; verify `OvertimeDayOffException` (409) is returned and the frontend shows a blocking `UAlert` with time-for-time guidance instead of an overtime table.

**Note**: The core backend logic (`OvertimeDayOffException` in `CalculateOvertimeEntriesUseCase`) was implemented in T075. This phase adds the explicit day-off toggle endpoint, `OverrideOnCallDayEntryUseCase` (needed to persist the flag), and the frontend UX.

- [ ] T082 [P] [US3] `OverrideOnCallDayEntryUseCase` + `OverrideOnCallDayEntryRequest` (entryId, hours BigDecimal optional, rateType StandbyRateType optional, timeForTimeFlag boolean optional) + `OverrideOnCallDayEntryValidator` (entryId must exist; sets `manualOverride = true`); unit test: flag-only override, full field override
- [ ] T083 [US3] Extend `OnCallPeriodController` with `PUT /api/v1/oncall-periods/{periodId}/day-entries/{entryId}` — routes to `OverrideOnCallDayEntryUseCase`; used both for field overrides and for toggling `timeForTimeFlag`; add to `OnCallPeriodControllerTest`
- [ ] T084 [P] [US3] Extend `frontend/components/oncall/DayEntryTable.vue` — "Mark as day off" `UToggle` per row calls `PUT /oncall-periods/{id}/day-entries/{entryId}` with `timeForTimeFlag: true/false`; optimistic update in store
- [ ] T085 [P] [US3] Extend `frontend/pages/oncall/[id].vue` — when a day entry has `timeForTimeFlag: true`, disable the corresponding incident's "Calculate Overtime" `UButton` and show `UAlert` info: "Time-for-time applies for this day — discuss with your manager"
- [ ] T086 [P] [US3] Extend `frontend/components/oncall/IncidentForm.vue` — if selected incident date matches a flagged day entry, show `UAlert` warning before submitting: "This day is flagged as a day off. Overtime pay does not apply — time-for-time applies instead."
- [ ] T087 [P] [US3] `frontend/components/shared/TimeForTimeGuidance.vue` — reusable `UAlert` component (variant `info`) explaining time-for-time: show when `timeForTimeFlag` is set and `onCallPeriodId` is null (engineer not on the rotation); used in incident form and day entry row

**Checkpoint**: After T087, all time-for-time scenarios are correctly flagged, overtime is blocked for those days, and the engineer receives actionable guidance.

---

## Phase 7: User Story 4 — Registration Summary & Reporting (Priority: P4)

**Goal**: The engineer views a structured reporting screen with all on-call and overtime entries for a period, can quick-edit or delete individual entries inline, and can persist the summary for future reference.

**Independent Test**: Create a registration summary for an existing on-call period with incidents; open `GET /summaries/{id}`; verify all on-call day entries and overtime entries are listed; update one entry via `PUT /summaries/{id}/oncall-entries/{entryId}`; verify `manualOverride: true` and updated value in subsequent `GET`.

### Backend — Registration Summaries

- [ ] T088 [P] [US4] `CreateRegistrationSummaryUseCase` + `CreateRegistrationSummaryRequest` (periodId, label String optional) + `CreateRegistrationSummaryValidator` (periodId must exist; auto-generate label from period dates if omitted); unit test
- [ ] T089 [P] [US4] `GetRegistrationSummaryUseCase` + `GetRegistrationSummaryRequest` (summaryId) + `GetRegistrationSummaryValidator`; unit test: returns summary with full `onCallEntries` and `overtimeEntries` lists
- [ ] T090 [P] [US4] `ListRegistrationSummariesUseCase` + `ListRegistrationSummariesRequest` (empty) + `ListRegistrationSummariesValidator`; unit test
- [ ] T091 [P] [US4] `DeleteRegistrationSummaryUseCase` + `DeleteRegistrationSummaryRequest` (summaryId) + `DeleteRegistrationSummaryValidator`; unit test
- [ ] T092 [P] [US4] `DeleteOnCallDayEntryUseCase` + `DeleteOnCallDayEntryRequest` (entryId) + `DeleteOnCallDayEntryValidator`; unit test
- [ ] T093 [P] [US4] `OverrideOvertimeEntryUseCase` + `OverrideOvertimeEntryRequest` (entryId, overtimeHours, allowanceHours, allowancePercentage) + `OverrideOvertimeEntryValidator` (sets `manualOverride = true`); unit test
- [ ] T094 [P] [US4] `DeleteOvertimeEntryUseCase` + `DeleteOvertimeEntryRequest` (entryId) + `DeleteOvertimeEntryValidator`; unit test
- [ ] T095 [P] [US4] `AddOnCallDayEntryUseCase` + `AddOnCallDayEntryRequest` (date, hours, rateType) + `AddOnCallDayEntryValidator` (creates entry with `manualOverride = true`); unit test
- [ ] T096 [P] [US4] `AddOvertimeEntryUseCase` + `AddOvertimeEntryRequest` (incidentId, overtimeHours, allowanceHours, allowancePercentage, timeFrom, timeTo, isAllowanceEntry) + `AddOvertimeEntryValidator` (incidentId must exist; sets `manualOverride = true`); unit test
- [ ] T097 [US4] `RegistrationSummaryController` in `backend/src/main/java/com/dutytracker/presentation/api/` — all endpoints from `contracts/api.md`: `GET /api/v1/summaries` (200), `POST /api/v1/summaries` (201), `GET /api/v1/summaries/{id}` (200/404), `DELETE /api/v1/summaries/{id}` (204), `POST /api/v1/summaries/{id}/oncall-entries` (201), `PUT /api/v1/summaries/{id}/oncall-entries/{entryId}` (200), `DELETE /api/v1/summaries/{id}/oncall-entries/{entryId}` (204), `POST /api/v1/summaries/{id}/overtime-entries` (201), `PUT /api/v1/summaries/{id}/overtime-entries/{entryId}` (200), `DELETE /api/v1/summaries/{id}/overtime-entries/{entryId}` (204); `RegistrationSummaryControllerTest` with MockMvc

### Frontend — Reporting Screen

- [ ] T098 [P] [US4] `frontend/stores/report.ts` — Pinia store: `summaries` (RegistrationSummary[]), `currentSummary` (full summary with entries | null); `fetchSummaries`, `createSummary`, `deleteSummary`, `fetchSummary`, `overrideOnCallEntry`, `deleteOnCallEntry`, `addOnCallEntry`, `overrideOvertimeEntry`, `deleteOvertimeEntry`, `addOvertimeEntry` actions
- [ ] T099 [P] [US4] `frontend/pages/report/index.vue` — `UTable` of summaries (label, period, createdAt); `UButton` "New Summary" opens `UModal` with period select and optional label input; per-row delete with `ConfirmDeleteModal`; each row links to `/report/{id}`
- [ ] T100 [US4] `frontend/pages/report/[id].vue` — reporting screen: (1) summary header (label, period); (2) On-Call Day Entries `UCard` with `SummaryEntryTable`, "Add Entry" `UButton`, per-row quick-edit `UButton` and delete `UButton`; (3) Overtime Entries `UCard` same structure; (4) `UModal` with `QuickEditPopup` for inline editing; all changes update the store and re-render immediately
- [ ] T101 [P] [US4] `frontend/components/report/SummaryEntryTable.vue` — `UTable` reusable for both on-call and overtime entries; manualOverride `UBadge` on overridden rows; columns adapt based on entry type prop; emits `edit` and `delete` events per row
- [ ] T102 [P] [US4] `frontend/components/report/QuickEditPopup.vue` — `UModal` for editing a single entry; for on-call entries: hours `UInput`, rateType `USelect`; for overtime entries: overtimeHours, allowanceHours, allowancePercentage `UInput`; saves via store action on confirm; emits `saved`
- [ ] T103 [P] [US4] `frontend/components/report/AddEntryModal.vue` — `UModal` for manually adding an on-call or overtime entry; entry type selector `USelect`; conditional fields based on selected type; calls `addOnCallEntry` or `addOvertimeEntry` store action; emits `added`
- [ ] T113 [P] [US4] Add browser print export to `frontend/pages/report/[id].vue` — "Print / Save as PDF" `UButton` calling `window.print()`; create `frontend/assets/css/print.css` with `@media print` styles that hide navigation, action buttons, and sidebars while formatting the summary as a clean printable table; import `print.css` in the report page; satisfies US4 AC#2 (output that can be retained as a personal record)

**Checkpoint**: After T103, the full registration summary and reporting workflow works end-to-end.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Completeness, mobile-first UX quality, and full deployment verification.

- [ ] T104 [P] Backend — run `mvn test`; fix all failing unit tests and MockMvc tests; confirm ArchUnit test passes across all packages; ensure all 22 use cases and all controllers have test classes (Constitution gate T-01)
- [ ] T105 [P] Frontend — add `frontend/plugins/error-handler.ts` global error handler: catch unhandled Nuxt errors, surface as `useToast()` notifications for all domain exception types; ensure no silent failures on API calls
- [ ] T106 [P] Frontend — audit all pages for loading state coverage: add `USkeleton` or `UProgress` while API calls are in-flight; add `UAlert` variant `error` for persistent failures; ensure all form submissions disable their submit `UButton` while pending
- [ ] T107 [P] Backend — review all controllers against `contracts/api.md`; verify HTTP status codes (201 for creation, 204 for deletion, 409 for domain conflicts), response body shapes, and `Content-Type: application/json` headers; add missing edge-case MockMvc tests for 404 and 409 paths
- [ ] T108 [P] Validate mobile-first layout at 375 px width (iPhone SE baseline) across `pages/onboarding/index.vue`, `pages/oncall/[id].vue`, and `pages/report/[id].vue` — confirm `UTable` columns collapse or scroll horizontally on small screens; confirm `UModal` and `UPopover` are touch-friendly; apply Tailwind breakpoint prefixes (`sm:`, `md:`, `lg:`) and Nuxt UI responsive props throughout
- [ ] T109 Run full quickstart validation per `quickstart.md` — `docker compose up --build`; verify: postgres starts and healthcheck passes, Flyway applies V1 + V2 cleanly, backend starts on port 8080, frontend loads at `http://localhost:3000`, onboarding wizard appears on first visit, all 3 wizard steps complete successfully
- [ ] T110 [P] Add `UAlert` warning on `CompensationRatesStep.vue` and `frontend/pages/settings/index.vue` compensation tab — prominently note that WCA percentage values are placeholders (`0.0000`) and must be updated from the WCA PDF (Jumbo Logistics Works Council Agreement, version P7-2025) before recording any registrations

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **US5 (Phase 3)** and **US1 (Phase 4)**: Both depend on Foundational — can proceed in parallel (entirely different files)
- **US2 (Phase 5)**: Depends on Foundational + US1 (uses OnCallPeriod entity and oncall store patterns)
- **US3 (Phase 6)**: Depends on US1 backend (extends `OnCallPeriodController`) + US2 completion (`CalculateOvertimeEntriesUseCase` must exist for day-off check)
- **US4 (Phase 7)**: Depends on US1 + US2 completion (summaries aggregate both on-call day entries and overtime entries)
- **Polish (Phase 8)**: Depends on all prior phases

### Key Intra-Phase Dependencies

| Task | Depends On |
|------|-----------|
| T020 (JDBC repositories) | T012–T017 (domain types + gateway interfaces) |
| T022 (CORS config) | T002 (main class exists) |
| T043–T046 (controllers) | T032–T042 (use cases for that controller) |
| T054 (JollydayGateway) | T017 (PublicHolidayGateway interface) |
| T062 (CalculateOnCallDayEntries) | T054 (Jollyday), T055–T061 (period use cases) |
| T075 (CalculateOvertimeEntries) | T071–T074 (incident use cases) |
| T083 (extend OnCallPeriodController) | T082 (OverrideOnCallDayEntryUseCase) |
| T097 (SummaryController) | T088–T096 (summary use cases) |
| T100 (reporting screen) | T098 (report store), T099 (summaries list) |

### Within Each Phase

- All tasks marked `[P]` can run in parallel (different files, no intra-task dependencies)
- Unmarked tasks within a phase have intra-phase dependencies — implement in listed order

---

## Parallel Execution Examples

### Phase 3 (US5) — Backend Use Cases (all parallelizable)

```
Launch simultaneously:
T032 GetOnboardingStatusUseCase
T033 AdvanceOnboardingStepUseCase
T034 CreateEngineerProfileUseCase
T035 GetEngineerProfileUseCase
T036 UpdateEngineerProfileUseCase
T037 UpdateUserPreferencesUseCase
T038 GetUserPreferencesUseCase
T039 GetCompensationRateTableUseCase
T040 UpdateCompensationRateUseCase
T041 CreateCompensationRateUseCase
T042 DeleteCompensationRateUseCase
```

### Phase 4 (US1) — On-Call Period Use Cases (all parallelizable)

```
Launch simultaneously:
T055 CreateOnCallPeriodUseCase
T056 GetOnCallPeriodUseCase
T057 ListOnCallPeriodsUseCase
T058 UpdateOnCallPeriodUseCase
T059 DeleteOnCallPeriodUseCase
T060 AddHolidayOverrideUseCase
T061 RemoveHolidayOverrideUseCase
→ Then T062 CalculateOnCallDayEntriesUseCase (depends on above + T054)
```

---

## Implementation Strategy

### MVP (P1 Stories Only)

1. Phase 1: Setup
2. Phase 2: Foundational (CRITICAL — blocks everything)
3. Phase 3: US5 — Onboarding & Settings
4. Phase 4: US1 — On-Call Hours Registration
5. **STOP and VALIDATE**: engineer can complete full on-call week registration end-to-end

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. Phase 3 + 4 → On-call registration MVP (P1 complete, engineer can be compensated correctly)
3. Phase 5 → Overtime calculation (P2 complete)
4. Phase 6 → Time-for-time guidance (P3 complete)
5. Phase 7 → Reporting screen (P4 complete — full product)
6. Phase 8 → Polish and production validation

---

## Notes

- `[P]` = parallel — different files, no intra-task dependency; launch simultaneously for speed
- Each UseCase task includes the Request record, RequestValidator, and unit test class (3 files)
- Each Controller task includes the MockMvc test class
- Commit after each phase checkpoint at minimum
- Stop at each `**Checkpoint**` to validate the story end-to-end before advancing
- WCA compensation rate percentages (`0.0000` placeholders in V2 migration) must be updated manually from the WCA PDF before first real use — the engineer is warned during onboarding Step 3 and in Settings
- `StandbyRateType` is the correct domain enum name for on-call rate types (WEEKDAY_SATURDAY, SUNDAY_HOLIDAY)
- All Spring beans use constructor injection exclusively — `@Autowired` on fields is forbidden (ArchUnit enforces this)
