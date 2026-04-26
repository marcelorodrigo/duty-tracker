# Tasks: On-Call Hours Tracker

**Input**: Design documents from `specs/001-oncall-hours-tracker/`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/api.md ✅

**Tests**: Included — required by Constitution gate T-01 (every UseCase, Validator, and Controller must have a test class).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story. P1 stories (US5 and US1) must be completed before P2+.

---

## Phase 1: Project Setup (Shared Infrastructure)

**Purpose**: Scaffolding for all three services (postgres, backend, frontend) so any story can be developed and run immediately.

- [ ] T001 Create `backend/pom.xml` — Spring Boot 4.x parent, Java 25, dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jdbc`, `postgresql`, `flyway-core`, `flyway-database-postgresql`, `jollyday-core`, `spring-boot-starter-test`, `archunit-junit5`
- [ ] T002 Create Nuxt 4 frontend via `pnpm create nuxt@latest frontend`; install modules: `pnpm add @nuxt/ui @pinia/nuxt @vueuse/nuxt dayjs-nuxt`; configure `nuxt.config.ts` (modules, colorMode, dayjs `nl` locale, `runtimeConfig.public.apiBase`); add `assets/css/main.css` with `@import "tailwindcss"; @import "@nuxt/ui";`; set `shamefully-hoist=true` in `.npmrc`
- [ ] T003 [P] Create `docker-compose.yml` (repo root) — three services: `postgres` (image `postgres:18-alpine`, named volume `pgdata`), `backend` (build `./backend`, depends on postgres), `frontend` (build `./frontend`, depends on backend); expose ports 5432 (internal only), 8080, 3000
- [ ] T004 [P] Create `backend/Dockerfile` — multi-stage: stage 1 `eclipse-temurin:25-jdk` Maven build (`mvn package -DskipTests`), stage 2 `eclipse-temurin:25-jre` runtime; `ENTRYPOINT ["java", "-jar", "app.jar"]`
- [ ] T005 [P] Create `frontend/Dockerfile` — multi-stage: stage 1 `node:24-alpine` pnpm install + `pnpm build`, stage 2 `nginx:alpine` serving `.output/public`; expose port 80

**Checkpoint**: `docker compose up --build` should start all services without application errors.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model, persistence layer, and cross-cutting infrastructure. No user story work can begin until this phase is complete.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T006 Create Flyway migration `backend/src/main/resources/db/migration/V1__create_schema.sql` — all 8 tables from data-model.md (PostgreSQL DDL)
- [ ] T007 Create Flyway migration `backend/src/main/resources/db/migration/V2__seed_compensation_rates.sql` — placeholder seed rows (0.0000) for all base rate categories
- [ ] T008 [P] Create domain enums in `backend/src/main/java/com/dutytracker/domain/model/`: `EmployeeType`, `RateCategory`, `RateType`, `ColorScheme`, `OnboardingStep`
- [ ] T009 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `EngineerProfile`, `UserPreferences`, `CompensationRate`
- [ ] T010 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `OnCallPeriod`, `HolidayOverride`, `OnCallDayEntry`
- [ ] T011 [P] Create domain entity records in `backend/src/main/java/com/dutytracker/domain/model/`: `Incident`, `OvertimeEntry`, `RegistrationSummary`
- [ ] T012 [P] Create all 8 domain exceptions in `backend/src/main/java/com/dutytracker/domain/exception/`: `ProfileAlreadyExistsException`, `ProfileLockedException`, `OnboardingNotCompletedException`, `InvalidOnCallPeriodException`, `InvalidIncidentException`, `HolidayAlreadyRegisteredException`, `IncidentDuringWorkingHoursException`, `OvertimeDayOffException`
- [ ] T013 [P] Create all 10 gateway interfaces in `backend/src/main/java/com/dutytracker/domain/gateway/`: `EngineerProfileGateway`, `UserPreferencesGateway`, `CompensationRateGateway`, `OnCallPeriodGateway`, `HolidayOverrideGateway`, `OnCallDayEntryGateway`, `IncidentGateway`, `OvertimeEntryGateway`, `RegistrationSummaryGateway`, `PublicHolidayGateway`
- [ ] T014 Create Spring Data JDBC repository implementations for all gateways in `backend/src/main/java/com/dutytracker/infrastructure/persistence/` — one `JdbcXxxGateway` class per gateway interface; include `WorkingDaysConverter` (comma-separated `Set<DayOfWeek>`)
- [ ] T015 [P] Create `backend/src/main/resources/application.yml` — datasource (PostgreSQL), Flyway config, server port, logging levels
- [ ] T016 [P] Create `GlobalExceptionHandler` in `backend/src/main/java/com/dutytracker/presentation/api/` — maps all domain exceptions to RFC 7807 `ProblemDetail` responses with appropriate HTTP status codes and `type` URIs
- [ ] T017 [P] Create `UseCase<Req, Res>` interface in `backend/src/main/java/com/dutytracker/application/usecase/`; create `RequestValidator<Req>` interface
- [ ] T018 [P] Update `specs/001-oncall-hours-tracker/data-model.md` — add 4 use cases missing after API contract update: `CreateCompensationRateUseCase`, `DeleteCompensationRateUseCase`, `AddOnCallDayEntryUseCase`, `AddOvertimeEntryUseCase`
- [ ] T019 [P] Create ArchUnit test `backend/src/test/java/com/dutytracker/ArchitectureTest.java` — enforce: no imports from `infrastructure`/`presentation` in `domain`/`application`; no `@Autowired` field injection anywhere; constructor injection only

**Checkpoint**: Foundation complete — Flyway migrations apply cleanly, all domain types compile, ArchUnit test passes (empty project).

---

## Phase 3: User Story 5 — First-Run Onboarding Setup (Priority: P1)

**Goal**: The engineer can complete the 3-step onboarding wizard (Profile → Preferences → Compensation Rates) before accessing the main application. Settings are editable post-onboarding.

**Independent Test**: Launch the app with empty database. Verify wizard is shown, all other routes redirect to `/onboarding`, wizard progresses through all 3 steps in order, stores data, and unlocks the main application after completion.

### Backend — Onboarding & Profile

- [ ] T020 [P] `GetOnboardingStatusUseCase` + `GetOnboardingStatusRequest` + `GetOnboardingStatusValidator` in `backend/src/main/java/com/dutytracker/application/usecase/onboarding/`; unit test in `backend/src/test/java/com/dutytracker/application/`
- [ ] T021 [P] `AdvanceOnboardingStepUseCase` + `AdvanceOnboardingStepRequest` + validator + unit test
- [ ] T022 [P] `CreateEngineerProfileUseCase` + `CreateEngineerProfileRequest` + validator + unit test (verify `ProfileAlreadyExistsException` thrown when profile exists)
- [ ] T023 [P] `GetEngineerProfileUseCase` + request + validator + unit test
- [ ] T024 [P] `UpdateUserPreferencesUseCase` + `UpdateUserPreferencesRequest` + validator + unit test
- [ ] T025 [P] `GetUserPreferencesUseCase` + request + validator + unit test
- [ ] T026 [P] `GetCompensationRateTableUseCase` + request + validator + unit test
- [ ] T027 [P] `UpdateCompensationRateUseCase` + `UpdateCompensationRateRequest` + validator + unit test
- [ ] T028 [P] `CreateCompensationRateUseCase` (OVERTIME_ALLOWANCE rows only) + `CreateCompensationRateRequest` + validator + unit test
- [ ] T029 [P] `DeleteCompensationRateUseCase` + `DeleteCompensationRateRequest` + validator + unit test (verify base rate rows are protected)
- [ ] T030 `OnboardingController` in `backend/src/main/java/com/dutytracker/presentation/api/` — `GET /api/v1/onboarding/status`, `POST /api/v1/onboarding/advance`; MockMvc test class
- [ ] T031 [P] `ProfileController` — `POST /api/v1/profile`, `GET /api/v1/profile`, `PUT /api/v1/profile`; MockMvc test class
- [ ] T032 [P] `PreferencesController` — `GET /api/v1/preferences`, `PUT /api/v1/preferences`; MockMvc test class
- [ ] T033 [P] `CompensationRateController` — `GET /api/v1/compensation-rates`, `POST /api/v1/compensation-rates`, `PUT /api/v1/compensation-rates/{id}`, `DELETE /api/v1/compensation-rates/{id}`; MockMvc test class

### Frontend — Onboarding Wizard & Settings

- [ ] T034 [P] `frontend/stores/profile.ts` — Pinia store: `profile` state, `fetchProfile`, `createProfile`, `updateProfile` actions
- [ ] T035 [P] `frontend/stores/preferences.ts` — Pinia store: `preferences` state, `fetchPreferences`, `updatePreferences` actions; call `useColorMode().preference` setter on update (from `@nuxtjs/color-mode`, auto-registered by `@nuxt/ui`)
- [ ] T036 [P] `frontend/stores/compensation.ts` — Pinia store: `rates` state, `fetchRates`, `createRate`, `updateRate`, `deleteRate` actions
- [ ] T037 [P] `frontend/components/onboarding/ProfileStep.vue` — uses `USelect` (employee type), `UCheckbox` group (working days), `UInput` time pickers; `UForm` + `UFormField` for validation; emits `saved`
- [ ] T038 [P] `frontend/components/onboarding/PreferencesStep.vue` — uses `UColorModeSwitch` (Nuxt UI built-in) for dark/light/auto toggle with live preview; emits `saved`
- [ ] T039 [P] `frontend/components/onboarding/CompensationRatesStep.vue` — uses `UTable` for rate rows, `UInput` (percentage), `UButton` (save/add/delete); emits `saved`
- [ ] T040 `frontend/pages/onboarding/index.vue` — wizard shell: `UProgress` or `USteps` indicator, step routing (PROFILE → PREFERENCES → COMPENSATION_RATES), advance on each `saved` event, redirect to `/` on `COMPLETE`
- [ ] T041 [P] `frontend/middleware/onboarding.global.ts` — global route middleware: fetch onboarding status; redirect non-`/onboarding` routes to `/onboarding` when status is not `COMPLETE`
- [ ] T042 [P] `frontend/pages/settings/index.vue` — `UTabs` with panels for Profile (locked `UBadge`), Preferences, Compensation Rate Table; reuses wizard step components

**Checkpoint**: After T042, the full onboarding flow works end-to-end, the main app is accessible after completion, and settings are editable at `/settings`.

---

## Phase 4: User Story 1 — Register On-Call Hours (Priority: P1)

**Goal**: The engineer enters an on-call period, optionally marks holiday overrides, triggers calculation, and receives one on-call day entry per day with correct hours, rate type, and cap.

**Independent Test**: POST an on-call period for a known week (e.g., Mon 14:00 → following Mon 14:00), call `/calculate`, and verify the 7 day entries match expected hours, rate types, and cap values — without any prior incident or summary data.

### Backend — On-Call Periods

- [ ] T043 Create `JollyDayPublicHolidayGateway` in `backend/src/main/java/com/dutytracker/infrastructure/holiday/` — wraps `de.focus-shift:jollyday-core`, Dutch calendar (`HolidayCalendar.NETHERLANDS`); `isHoliday(LocalDate)` and `getHolidays(year)` methods; integration test with known Dutch holidays (Koningsdag, Kerst)
- [ ] T044 [P] `CreateOnCallPeriodUseCase` + `CreateOnCallPeriodRequest` + validator (endDateTime > startDateTime, period ≥ 1 hour) + unit test
- [ ] T045 [P] `GetOnCallPeriodUseCase` + request + validator + unit test
- [ ] T046 [P] `ListOnCallPeriodsUseCase` + request + validator + unit test
- [ ] T047 [P] `UpdateOnCallPeriodUseCase` + `UpdateOnCallPeriodRequest` + validator + unit test
- [ ] T048 [P] `DeleteOnCallPeriodUseCase` + request + validator + unit test
- [ ] T049 [P] `AddHolidayOverrideUseCase` + `AddHolidayOverrideRequest` + validator (date within period, no duplicate) + unit test
- [ ] T050 [P] `RemoveHolidayOverrideUseCase` + request + validator + unit test
- [ ] T051 `CalculateOnCallDayEntriesUseCase` in `backend/src/main/java/com/dutytracker/application/usecase/oncall/` — implements full algorithm from data-model.md (per-day hours from period boundaries, rateType, 15h/24h cap, timeForTimeFlag); unit tests covering: full week, Sunday rate, holiday rate, partial start/end days, 15h cap on working days, no cap on non-working days, multi-holiday period
- [ ] T052 `OnCallPeriodController` — all endpoints from `contracts/api.md` (`POST`, `GET`, `GET/{id}`, `PUT/{id}`, `DELETE/{id}`, `POST/{id}/holidays`, `DELETE/{id}/holidays/{date}`, `POST/{id}/calculate`); MockMvc test class

### Frontend — On-Call Periods

- [ ] T053 [P] `frontend/stores/oncall.ts` — Pinia store: `periods`, `currentPeriod`, `dayEntries` state; all CRUD + calculate actions
- [ ] T054 [P] `frontend/pages/oncall/index.vue` — `UTable` of on-call periods; `UButton` create/delete; `UModal` for `PeriodForm`; link to detail
- [ ] T055 `frontend/pages/oncall/[id].vue` — period detail: holiday override list with add/delete (`UPopover` date picker), "Calculate" `UButton`, `DayEntryTable` with timeForTimeFlag `UBadge`
- [ ] T056 [P] `frontend/components/oncall/PeriodForm.vue` — `UForm` + `UFormField` with datetime `UInput`; `UAlert` for validation errors
- [ ] T057 [P] `frontend/components/oncall/HolidayOverrideList.vue` — `UChip`/tag list with add (`UPopover` + date input) and `UButton` delete
- [ ] T058 [P] `frontend/components/oncall/DayEntryTable.vue` — `UTable`: date, hours, rateType `UBadge`, capped `UBadge`, timeForTimeFlag `UIcon`

**Checkpoint**: After T058, full on-call registration flow works end-to-end.

---

## Phase 5: User Story 2 — Record Overtime Hours (Priority: P2)

**Goal**: The engineer logs an incident with start/end time, triggers calculation, and receives overtime entries split by allowance time zone with correct rounding and percentages.

**Independent Test**: POST an incident on a known date/time outside working hours, call `/calculate`, verify entries contain correctly rounded hours, correct allowance zones, and correct percentages — without needing a complete on-call period.

### Backend — Incidents & Overtime

- [ ] T059 [P] `LogIncidentUseCase` + `LogIncidentRequest` + validator (date not in future, endTime > startTime or overnight-flag, date within onCallPeriod if provided) + unit test
- [ ] T060 [P] `UpdateIncidentUseCase` + request + validator + unit test
- [ ] T061 [P] `DeleteIncidentUseCase` + request + validator + unit test
- [ ] T062 [P] `ListIncidentsUseCase` + request + validator + unit test
- [ ] T063 `CalculateOvertimeEntriesUseCase` in `backend/src/main/java/com/dutytracker/application/usecase/incident/` — implements full algorithm from data-model.md (exclude working hours, split by allowance zones, ceil rounding, base + allowance entry pairs); unit tests covering: normal-hours exclusion, rounding to next hour, multi-zone split, holiday rate, overnight incident, `IncidentDuringWorkingHoursException`, `OvertimeDayOffException`
- [ ] T064 `IncidentController` — all endpoints from `contracts/api.md` (`POST`, `GET`, `GET/{id}`, `PUT/{id}`, `DELETE/{id}`, `POST/{id}/calculate`); MockMvc test class

### Frontend — Incidents & Overtime

- [ ] T065 [P] Extend `frontend/stores/oncall.ts` (or create `frontend/stores/incident.ts`) — incidents, overtimeEntries state; CRUD + calculate actions
- [ ] T066 `frontend/pages/oncall/[id].vue` — extend with Incident section below day entries: `UTable` incident list, `UButton` add/edit/delete, per-incident "Calculate" `UButton`, overtime entries `UTable` sub-row
- [ ] T067 [P] `frontend/components/oncall/IncidentForm.vue` — `UForm` + `UFormField`: date `UInput`, start/end time `UInput`, optional onCallPeriodId; `UAlert` error for `IncidentDuringWorkingHoursException` and `OvertimeDayOffException`
- [ ] T068 [P] `frontend/components/oncall/OvertimeEntryTable.vue` — `UTable`: timeFrom, timeTo, isAllowanceEntry `UBadge`, overtimeHours, allowanceHours, allowancePercentage, manualOverride `UBadge`
- [ ] T069 [P] `frontend/composables/useOvertimeCalculation.ts` — wraps calculate API call; surfaces typed errors as `useToast()` notifications

**Checkpoint**: After T069, incident logging and overtime calculation work end-to-end.

---

## Phase 6: User Story 3 — Time-for-Time Identification (Priority: P3)

**Goal**: The system proactively identifies days flagged as time-for-time (day off during on-call) and blocks overtime registration for those days, directing the engineer to speak with their manager.

**Independent Test**: Set `timeForTimeFlag = true` on a day entry, attempt to calculate overtime for an incident on that day, verify `OvertimeDayOffException` is returned. Verify the frontend shows a blocking warning instead of an overtime table.

**Note**: The core backend logic (`OvertimeDayOffException`) is already handled in `CalculateOvertimeEntriesUseCase` (T063). This phase adds the frontend UX and the mechanism to persist the flag from the UI.

- [ ] T070 [P] Backend — add `PUT /api/v1/oncall-periods/{id}/day-entries/{entryId}/flag-day-off` to `OnCallPeriodController`; routes to `OverrideOnCallDayEntryUseCase` setting `timeForTimeFlag = true`; MockMvc test
- [ ] T071 [P] `frontend/components/oncall/DayEntryTable.vue` — extend: "Mark as day off" `UToggle` per row; on toggle calls flag endpoint; row shows time-for-time `UBadge` when flagged
- [ ] T072 [P] `frontend/pages/oncall/[id].vue` — when day is flagged as day off, disable "Calculate Overtime" `UButton` for incidents on that day; show `UAlert` info: "Time-for-time applies — discuss with your manager"
- [ ] T073 [P] `frontend/components/oncall/IncidentForm.vue` — if incident date matches a flagged day entry, show `UAlert` warning before submitting

**Checkpoint**: After T073, time-for-time scenarios are correctly identified, flagged, and communicated to the engineer.

---

## Phase 7: User Story 4 — Registration Summary & Reporting (Priority: P4)

**Goal**: The engineer sees a structured reporting screen with all on-call and overtime entries for a period, can edit/delete individual entries inline (quick-edit popup), and can persist the summary for future reference.

**Independent Test**: Complete an on-call period + incidents (or use existing data), create a summary, open the reporting screen, verify all line items appear grouped correctly, edit one entry via quick-edit popup, and verify the change persists.

### Backend — Registration Summaries

- [ ] T074 [P] `CreateRegistrationSummaryUseCase` + `CreateRegistrationSummaryRequest` + validator + unit test (auto-generates label if omitted)
- [ ] T075 [P] `GetRegistrationSummaryUseCase` + request + validator + unit test
- [ ] T076 [P] `ListRegistrationSummariesUseCase` + request + validator + unit test
- [ ] T077 [P] `DeleteRegistrationSummaryUseCase` + request + validator + unit test
- [ ] T078 [P] `OverrideOnCallDayEntryUseCase` + `OverrideOnCallDayEntryRequest` + validator (sets `manualOverride = true`) + unit test
- [ ] T079 [P] `DeleteOnCallDayEntryUseCase` + request + validator + unit test
- [ ] T080 [P] `OverrideOvertimeEntryUseCase` + `OverrideOvertimeEntryRequest` + validator + unit test
- [ ] T081 [P] `DeleteOvertimeEntryUseCase` + request + validator + unit test
- [ ] T082 [P] `AddOnCallDayEntryUseCase` (manual add to summary) + `AddOnCallDayEntryRequest` + validator + unit test
- [ ] T083 [P] `AddOvertimeEntryUseCase` (manual add to summary) + `AddOvertimeEntryRequest` + validator + unit test
- [ ] T084 `RegistrationSummaryController` — all endpoints from `contracts/api.md` (`GET /summaries`, `POST /summaries`, `GET /summaries/{id}`, `DELETE /summaries/{id}`, `POST/{id}/oncall-entries`, `PUT/{id}/oncall-entries/{entryId}`, `DELETE/{id}/oncall-entries/{entryId}`, `POST/{id}/overtime-entries`, `PUT/{id}/overtime-entries/{entryId}`, `DELETE/{id}/overtime-entries/{entryId}`); MockMvc test class

### Frontend — Reporting Screen

- [ ] T085 [P] `frontend/stores/report.ts` — Pinia store: `summaries`, `currentSummary` state; all CRUD, override, and add-entry actions
- [ ] T086 [P] `frontend/pages/report/index.vue` — `UTable` of summaries with create (`UModal` + form) / delete (`UButton`); each row links to detail
- [ ] T087 `frontend/pages/report/[id].vue` — reporting screen: two `UCard` sections (On-Call Day Entries, Overtime Entries); each row has quick-edit (`UPopover` or `UModal`) and delete (`UButton`) actions; `UButton` "Add row" per section
- [ ] T088 [P] `frontend/components/report/QuickEditPopup.vue` — `UModal` for editing a single entry; fields vary by entry type using `UForm` + `UFormField`; saves on confirm
- [ ] T089 [P] `frontend/components/report/AddEntryModal.vue` — `UModal` for manually adding on-call or overtime entry; `UForm` with conditional fields based on entry type
- [ ] T090 [P] `frontend/components/report/SummaryEntryTable.vue` — `UTable` reusable for both entry types; manualOverride `UBadge` on overridden rows

**Checkpoint**: After T090, the full reporting and summary workflow works end-to-end.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Completeness, UX quality, and deployment verification across all stories.

- [ ] T091 [P] Backend CORS configuration in `backend/src/main/java/com/dutytracker/presentation/api/WebConfig.java` — allow `http://localhost:3000`
- [ ] T092 [P] `frontend/composables/useApi.ts` — centralized `$fetch` wrapper with base URL from `runtimeConfig`, typed error extraction from RFC 7807 `ProblemDetail`, and `useToast()` (Nuxt UI) notification for domain exceptions
- [ ] T093 [P] Frontend global error handling — `frontend/plugins/error-handler.ts`; catches unhandled API errors; displays `useToast()` messages for all domain exception types
- [ ] T094 [P] Add `UpdateEngineerProfileUseCase` to `ProfileController` `PUT /api/v1/profile` — throw `ProfileLockedException` if any `RegistrationSummary` exists; unit test + MockMvc test
- [ ] T095 [P] Backend — verify all controllers return correct HTTP status codes and response shapes per `contracts/api.md`; add integration smoke tests for full request-response shape
- [ ] T096 [P] Frontend — add navigation guard in `frontend/middleware/app.global.ts` for non-onboarding routes: redirect to `/onboarding` if status ≠ `COMPLETE`; ensure `/onboarding` is accessible pre-setup
- [ ] T097 Run quickstart validation — `docker compose up --build`, verify postgres starts, Flyway applies V1 + V2, backend health check passes, frontend loads at `http://localhost:3000`, onboarding wizard appears on first visit
- [ ] T098 [P] Validate mobile-first layout across all pages — verify `pages/onboarding/index.vue`, `pages/oncall/[id].vue`, and `pages/report/[id].vue` render correctly at 375 px width (iPhone SE baseline); confirm `UTable` columns collapse or scroll horizontally on small screens; confirm `UModal` and `UPopover` are touch-friendly and full-screen on mobile; apply Tailwind breakpoint prefixes (`sm:`, `md:`, `lg:`) and Nuxt UI responsive props throughout all page and component templates

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup completion — BLOCKS all user stories
- **US5 (Phase 3)** and **US1 (Phase 4)**: Both depend on Foundational — can proceed in parallel (different files)
- **US2 (Phase 5)**: Depends on US1 backend entities (OnCallPeriod) and Phase 2 foundation
- **US3 (Phase 6)**: Depends on US2 completion (timeForTimeFlag flows from CalculateOvertimeEntriesUseCase)
- **US4 (Phase 7)**: Depends on US1 and US2 completion (summaries aggregate both entry types)
- **Polish (Phase 8)**: Depends on all prior phases

### Within Each Phase

- All tasks marked `[P]` can be run in parallel (different files, no intra-task dependencies)
- Unmarked tasks within a phase have dependencies — implement them in listed order

### Key Intra-Phase Dependencies

| Task | Depends On |
|------|-----------|
| T014 (JDBC repositories) | T008–T011 (domain records), T013 (gateway interfaces) |
| T030–T033 (controllers) | T020–T029 (use cases) |
| T051 (CalculateOnCallDayEntries) | T043 (Jollyday gateway), T044–T050 (period use cases) |
| T063 (CalculateOvertimeEntries) | T059–T062 (incident use cases) |
| T084 (SummaryController) | T074–T083 (summary use cases) |
| T087 (reporting screen) | T085 (report store), T086 (summaries list) |

---

## Implementation Strategy

### MVP (P1 Stories Only)

1. Phase 1: Setup
2. Phase 2: Foundational
3. Phase 3: US5 (Onboarding)
4. Phase 4: US1 (On-Call Hours)
5. **STOP and VALIDATE** — engineer can complete full on-call registration flow
6. Phase 8: Polish (T091–T097 only if needed for MVP demo)

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. Phase 3 + 4 → On-call registration MVP (P1 complete)
3. Phase 5 → Overtime calculation (P2 complete)
4. Phase 6 → Time-for-time guidance (P3 complete)
5. Phase 7 → Reporting screen (P4 complete)
6. Phase 8 → Polish and validation

---

## Notes

- `[P]` = parallel — different files, no intra-task dependency
- Each UseCase task includes the Request record, RequestValidator, and unit test class
- Each Controller task includes the MockMvc test class
- Tests must fail before implementation (TDD where applicable)
- Commit after each checkpoint at minimum
- Stop at each checkpoint to validate the user story end-to-end before advancing
- WCA compensation rate percentages (`0.0000` placeholders in V2) must be updated manually from the WCA PDF before first real use — engineer is prompted during onboarding Step 3
