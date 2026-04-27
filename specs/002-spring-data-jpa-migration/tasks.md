# Tasks: Spring Data JPA Migration

**Input**: Design documents from `specs/002-spring-data-jpa-migration/`  
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓

**Organization**: Tasks grouped by user story (US1, US2, US3) to enable independent implementation of each story.

**Path Conventions**: Backend-only migration — all paths relative to `backend/` directory.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization - update dependencies and configuration for JPA

- [ ] T001 Update `pom.xml`: Replace `spring-boot-starter-data-jdbc` with `spring-boot-starter-data-jpa` (line 33-36)
- [ ] T002 Update `src/main/resources/application.yml`: Add JPA config with `ddl-auto: validate`, UTC timezone, remove JDBC dialect (lines 6-8)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST complete before any user story implementation

**⚠️ CRITICAL**: No user story work begins until this phase is complete

### Create JPA Entity Classes (9 entities)

- [ ] T003 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/OnCallPeriodJpaEntity.java` with `@Entity`, `id`, `startDateTime`, `endDateTime`, `createdAt` fields
- [ ] T004 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/EngineerProfileJpaEntity.java` with `@Entity`, `employeeType`, `workingDays`, `workStartTime`, `workEndTime` fields
- [ ] T005 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/UserPreferencesJpaEntity.java` with `@Entity`, `colorScheme`, `onboardingStep` fields
- [ ] T006 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/CompensationRateJpaEntity.java` with `@Entity`, `employeeType`, `rateCategory`, `label`, `timeFrom`, `timeTo`, `percentage` fields
- [ ] T007 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/OnCallDayEntryJpaEntity.java` with `@Entity`, `onCallPeriodId`, `date`, `hours`, `rateType`, `capped`, `timeForTimeFlag`, `manualOverride` fields
- [ ] T008 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/IncidentJpaEntity.java` with `@Entity`, `onCallPeriodId`, `date`, `startTime`, `endTime`, `createdAt` fields
- [ ] T009 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/OvertimeEntryJpaEntity.java` with `@Entity`, `incidentId`, `overtimeHours`, `allowanceHours`, `allowancePercentage`, `timeFrom`, `timeTo`, `isAllowanceEntry`, `manualOverride` fields
- [ ] T010 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/HolidayOverrideJpaEntity.java` with `@Entity`, `onCallPeriodId`, `date` fields
- [ ] T011 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/entity/RegistrationSummaryJpaEntity.java` with `@Entity`, `label`, `periodStart`, `periodEnd`, `createdAt`, `updatedAt` fields

### Create JPA Repository Interfaces (9 repositories)

- [ ] T012 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/OnCallPeriodJpaRepository.java` extending `JpaRepository<OnCallPeriodJpaEntity, Long>` with derived query `findAllByOrderByStartDateTimeDesc()`
- [ ] T013 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/EngineerProfileJpaRepository.java` extending `JpaRepository<EngineerProfileJpaEntity, Long>` with `findFirstBy()`
- [ ] T014 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/UserPreferencesJpaRepository.java` extending `JpaRepository<UserPreferencesJpaEntity, Long>` with `findFirstBy()`
- [ ] T015 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/CompensationRateJpaRepository.java` extending `JpaRepository<CompensationRateJpaEntity, Long>` with `findByEmployeeType(EmployeeType)`
- [ ] T016 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/OnCallDayEntryJpaRepository.java` extending `JpaRepository<OnCallDayEntryJpaEntity, Long>` with `findByOnCallPeriodIdOrderByDateAsc(Long)`, `deleteByOnCallPeriodId(Long)`
- [ ] T017 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/IncidentJpaRepository.java` extending `JpaRepository<IncidentJpaEntity, Long>` with `findByOnCallPeriodId(Long)`, `findAllByOrderByDateAsc()`
- [ ] T018 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/OvertimeEntryJpaRepository.java` extending `JpaRepository<OvertimeEntryJpaEntity, Long>` with `findByIncidentId(Long)`, `deleteByIncidentId(Long)`
- [ ] T019 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/HolidayOverrideJpaRepository.java` extending `JpaRepository<HolidayOverrideJpaEntity, Long>` with `findByOnCallPeriodId(Long)`, `findByOnCallPeriodIdAndDate(Long, LocalDate)`
- [ ] T020 [P] Create `src/main/java/com/dutytracker/infrastructure/persistence/repository/RegistrationSummaryJpaRepository.java` extending `JpaRepository<RegistrationSummaryJpaEntity, Long>` with `findAllByOrderByPeriodStartDesc()`, `existsBy()`

### Create Attribute Converter

- [ ] T021 Create `src/main/java/com/dutytracker/infrastructure/persistence/converter/DayOfWeekSetConverter.java` implementing `AttributeConverter<Set<DayOfWeek>, String>` with `@Converter(autoApply=true)`, comma-separated storage format

### Delete Old JDBC Components

- [ ] T022 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcOnCallPeriodGateway.java`
- [ ] T023 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcEngineerProfileGateway.java`
- [ ] T024 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcUserPreferencesGateway.java`
- [ ] T025 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcCompensationRateGateway.java`
- [ ] T026 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcOnCallDayEntryGateway.java`
- [ ] T027 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcIncidentGateway.java`
- [ ] T028 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcOvertimeEntryGateway.java`
- [ ] T029 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcHolidayOverrideGateway.java`
- [ ] T030 [P] Delete `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JdbcRegistrationSummaryGateway.java`
- [ ] T031 Delete `src/main/java/com/dutytracker/infrastructure/persistence/converter/WorkingDaysConverter.java`

**Checkpoint**: Foundation complete - all entities, repositories, and converters in place. User story implementation can now proceed.

---

## Phase 3: User Story 1 - Data Persistence via JPA Repositories (Priority: P1) 🎯 MVP

**Goal**: Replace raw `NamedParameterJdbcTemplate` JDBC calls with JPA repository-backed gateway implementations. All 9 aggregates (EngineerProfile, UserPreferences, OnCallPeriod, OnCallDayEntry, OvertimeEntry, Incident, CompensationRate, HolidayOverride, RegistrationSummary) persist and retrieve data through JPA repositories with zero raw SQL remaining.

**Independent Test**: Can verify independently by:
1. Confirming zero `NamedParameterJdbcTemplate` references in persistence.gateway package (`grep -r "NamedParameterJdbcTemplate" backend/src/main/java`)
2. Running all 9 gateway integration tests: `mvn test -Dtest="Jpa*GatewayTest"`
3. Verifying save → find-by-id round-trip returns identical data for each aggregate

### Implementation for User Story 1

#### Create JPA Gateway Implementations (9 gateways)

- [ ] T032 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallPeriodGateway.java` implementing `OnCallPeriodGateway`, delegating to `OnCallPeriodJpaRepository`, with `toEntity()` and `toDomain()` mappers
- [ ] T033 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaEngineerProfileGateway.java` implementing `EngineerProfileGateway`, delegating to `EngineerProfileJpaRepository`, handling `Set<DayOfWeek>` conversion
- [ ] T034 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaUserPreferencesGateway.java` implementing `UserPreferencesGateway`, delegating to `UserPreferencesJpaRepository`
- [ ] T035 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaCompensationRateGateway.java` implementing `CompensationRateGateway`, delegating to `CompensationRateJpaRepository`, with `findByEmployeeType()` support
- [ ] T036 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallDayEntryGateway.java` implementing `OnCallDayEntryGateway`, delegating to `OnCallDayEntryJpaRepository`, with `findByOnCallPeriodId()` and `deleteByOnCallPeriodId()` support
- [ ] T037 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaIncidentGateway.java` implementing `IncidentGateway`, delegating to `IncidentJpaRepository`, with `findByOnCallPeriodId()` support
- [ ] T038 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOvertimeEntryGateway.java` implementing `OvertimeEntryGateway`, delegating to `OvertimeEntryJpaRepository`, with `findByIncidentId()` and `deleteByIncidentId()` support
- [ ] T039 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaHolidayOverrideGateway.java` implementing `HolidayOverrideGateway`, delegating to `HolidayOverrideJpaRepository`, with `findByOnCallPeriodId()` and `findByOnCallPeriodIdAndDate()` support
- [ ] T040 [P] [US1] Create `src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaRegistrationSummaryGateway.java` implementing `RegistrationSummaryGateway`, delegating to `RegistrationSummaryJpaRepository`, with `existsAny()` support

#### Create Integration Tests for Gateways (9 tests)

- [ ] T041 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallPeriodGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing save/find-by-id/delete/findAll scenarios
- [ ] T042 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaEngineerProfileGatewayTest.java` using `@DataJpaTest` + Testcontainers, verifying `Set<DayOfWeek>` serialization/deserialization
- [ ] T043 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaUserPreferencesGatewayTest.java` using `@DataJpaTest` + Testcontainers
- [ ] T044 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaCompensationRateGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `findByEmployeeType()`
- [ ] T045 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallDayEntryGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `findByOnCallPeriodId()`, `deleteByOnCallPeriodId()`
- [ ] T046 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaIncidentGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `findByOnCallPeriodId()`
- [ ] T047 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOvertimeEntryGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `findByIncidentId()`, `deleteByIncidentId()`
- [ ] T048 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaHolidayOverrideGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `findByOnCallPeriodId()`, `findByOnCallPeriodIdAndDate()`
- [ ] T049 [P] [US1] Create `src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaRegistrationSummaryGatewayTest.java` using `@DataJpaTest` + Testcontainers, testing `existsAny()`

#### Validation for User Story 1

- [ ] T050 [US1] Build project: `cd backend && mvn clean package -DskipTests` (verifies no compilation errors)
- [ ] T051 [US1] Run all gateway integration tests: `cd backend && mvn test -Dtest="Jpa*GatewayTest"` (verifies all 9 gateways work)
- [ ] T052 [US1] Verify no raw JDBC references: `grep -r "NamedParameterJdbcTemplate" backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway` (should return zero results)

**Checkpoint**: User Story 1 complete. All 9 gateway implementations delegate to JPA repositories with integration tests passing.

---

## Phase 4: User Story 2 - Clean Architecture Preserved (Priority: P2)

**Goal**: Verify domain model records remain pure Java with zero persistence framework annotations. All CA-01, CA-02, CC-01, CC-02 ArchUnit gates pass. No infrastructure imports leak into domain or application layers.

**Independent Test**: Can verify independently by:
1. Running ArchUnit tests: `cd backend && mvn test -Dtest="*ArchitectureTest"` (all gates pass)
2. Confirming zero `@Entity`, `@Table`, `@Column` annotations in `domain.model` package
3. Confirming zero imports from `infrastructure` in `domain` or `application` packages

### Implementation for User Story 2

- [ ] T053 [US2] Verify domain records unchanged: Inspect all 14 domain model classes in `src/main/java/com/dutytracker/domain/model/` for JPA annotations (should find none)
- [ ] T054 [US2] Verify no infrastructure imports in domain: `grep -r "import.*infrastructure" backend/src/main/java/com/dutytracker/domain` (should return zero results)
- [ ] T055 [US2] Verify no infrastructure imports in application: `grep -r "import.*infrastructure" backend/src/main/java/com/dutytracker/application` (should return zero results)
- [ ] T056 [US2] Run ArchUnit architecture tests: `cd backend && mvn test -Dtest="*ArchitectureTest"` (verifies CA-01, CA-02, CC-01, CC-02 gates pass with no violations)

**Checkpoint**: User Story 2 complete. Clean Architecture preserved - domain records remain annotation-free, all architecture gates pass.

---

## Phase 5: User Story 3 - Full Test Coverage for Persistence Layer (Priority: P3)

**Goal**: Run complete test suite and verify all persistence layer tests pass. Integration tests for all 9 gateways exercise save, find-by-id, delete, and custom query operations against real PostgreSQL via Testcontainers. Edge cases handled consistently.

**Independent Test**: Can verify independently by:
1. Running full test suite: `cd backend && mvn test` (exits with code 0, all tests pass)
2. Confirming 9 integration tests for each gateway in test output
3. Verifying application starts in Docker Compose and API endpoints respond
4. Confirming edge cases (null IDs, concurrent saves, schema validation) behave correctly

### Implementation for User Story 3

- [ ] T057 [US3] Run full test suite: `cd backend && mvn test` (all unit and integration tests must pass)
- [ ] T058 [US3] Verify test output includes all 9 gateway integration tests passing
- [ ] T059 [US3] Test edge case - null ID handling: Create new aggregate via gateway (ID is null), save, verify returned aggregate has non-null ID
- [ ] T060 [US3] Test edge case - Set<DayOfWeek> round-trip: Save EngineerProfile with `{MONDAY, WEDNESDAY, FRIDAY}`, retrieve, verify exact set matches (via JpaEngineerProfileGatewayTest)
- [ ] T061 [US3] Test edge case - Instant timestamp storage: Save aggregate with `Instant.now()` as createdAt, retrieve, verify instant is preserved (no timezone drift)
- [ ] T062 [US3] Test edge case - Foreign key relationships: Save OnCallPeriod, save OnCallDayEntry with onCallPeriodId reference, retrieve by period ID, verify all entries returned correctly
- [ ] T063 [US3] Start Docker Compose environment: `docker-compose up -d` (backend + postgres running)
- [ ] T064 [US3] Test application startup: Verify application starts without `SchemaManagementException` (Hibernate validation succeeds)
- [ ] T065 [US3] Test API endpoints: `curl http://localhost:8080/api/v1/[endpoint]` returns valid responses (spot-check a few endpoints work end-to-end)
- [ ] T066 [US3] Verify Flyway migrations ran correctly: Check PostgreSQL schema matches migration scripts

**Checkpoint**: User Story 3 complete. Full test suite passes, edge cases handled, application starts and endpoints respond.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, cleanup, and documentation

- [ ] T067 [P] Run Quickstart verification: Follow `specs/002-spring-data-jpa-migration/quickstart.md` steps (build, run tests, verify no NamedParameterJdbcTemplate references)
- [ ] T068 [P] Code cleanup: Remove any commented-out Jdbc code or temporary debugging statements from new JPA gateway classes
- [ ] T069 Verify migration completeness: Check off all success criteria from spec.md (SC-001 through SC-006)
- [ ] T070 Verify functional requirements: Check off all FR-001 through FR-008 from spec.md
- [ ] T071 Final build validation: `cd backend && mvn clean package` (entire project builds successfully)
- [ ] T072 Final test run: `cd backend && mvn test` (all tests pass, zero failures)

**Checkpoint**: All phases complete. Migration successful.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies - start immediately
  - Updates pom.xml and application.yml configuration

- **Phase 2 (Foundational)**: Depends on Phase 1 completion
  - **BLOCKS all user stories** - creates 9 entities, 9 repositories, 1 converter
  - All [P] tasks (entity/repository creation) can run in parallel
  - Old JDBC deletion (T022-T031) can run in parallel

- **Phase 3 (US1, P1)**: Depends on Phase 2 completion
  - Creates 9 JPA gateway implementations (T032-T040)
  - All [P] tasks can run in parallel (different files)
  - Creates 9 integration tests (T041-T049) in parallel
  - Can proceed independently while US2/US3 planned

- **Phase 4 (US2, P2)**: Depends on Phase 2 completion
  - Verification tasks (T053-T056) run after Phase 3 (needs gateways in place for complete picture)
  - Can start in parallel with Phase 3 after Phase 2

- **Phase 5 (US3, P3)**: Depends on Phase 3 completion
  - Integration test suite validation (T057-T066)
  - Needs all 9 gateway implementations working

- **Phase 6 (Polish)**: Depends on Phase 5 completion
  - Final validation and cleanup

### User Story Dependencies

- **User Story 1 (P1)**: Foundational → US1 (independent, core deliverable)
- **User Story 2 (P2)**: Foundational → US2 (independent, validates architecture)
- **User Story 3 (P3)**: Foundational → US1 → US3 (depends on US1 working to validate integration)

### Within Each User Story

- Models/Entities: Phase 2 (pre-requisite)
- Repositories: Phase 2 (pre-requisite)
- Gateway implementations: T032-T040
- Integration tests: T041-T049 (can write tests before implementations)
- Validation: T050-T052 (after all implementations and tests)

### Parallel Opportunities

**Phase 1**: Sequential (small, non-blocking)

**Phase 2**: HIGH PARALLELISM
- Entity creation (T003-T011): 9 tasks in parallel
- Repository creation (T012-T020): 9 tasks in parallel
- JDBC deletion (T022-T030): 9 tasks in parallel
- Converter creation (T021): Can run with any parallel task
- Old converter deletion (T031): Runs after T021

**Phase 3**: HIGH PARALLELISM
- Gateway creation (T032-T040): 9 tasks in parallel (different files, no dependencies)
- Integration tests (T041-T049): 9 tasks in parallel (write tests first, they should fail, then implement gateways)
- Validation (T050-T052): Sequential, after implementations

**Phase 4**: Sequential (verification tasks)
- All 4 tasks must run after Phase 3

**Phase 5**: Mostly sequential (integration testing)
- Some edge case tests (T059-T062) can run in parallel

**Phase 6**: [P] tasks can run in parallel

---

## Parallel Example: Phase 2 Foundational

```text
Run ALL Phase 2 tasks in parallel after Phase 1:

Parallel batch 1 (Entity creation):
  T003, T004, T005, T006, T007, T008, T009, T010, T011

Parallel batch 2 (Repository creation):
  T012, T013, T014, T015, T016, T017, T018, T019, T020

Sequential/Overlap:
  T021 (Converter creation) - can run during batch 1 or 2
  T022-T030 (JDBC deletion) - can run during batch 1 or 2
  T031 (WorkingDaysConverter deletion) - runs after T021
```

---

## Parallel Example: Phase 3 User Story 1

```text
Parallel batch 1 (Write failing tests first):
  T041, T042, T043, T044, T045, T046, T047, T048, T049

Parallel batch 2 (Implement gateways to make tests pass):
  T032, T033, T034, T035, T036, T037, T038, T039, T040

Sequential validation:
  T050 (Build)
  T051 (Run tests)
  T052 (Verify no JDBC references)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete **Phase 1**: Setup (pom.xml, application.yml)
2. Complete **Phase 2**: Foundational (all entities, repositories, converter)
3. Complete **Phase 3**: User Story 1 (9 gateway implementations + 9 integration tests)
4. **STOP and VALIDATE**: Run `mvn test`, verify all 9 gateway tests pass
5. Deploy/demo if ready — users can now use JPA-backed persistence

### Incremental Delivery (MVP + Validation)

1. Phases 1-2 + Phase 3 → Core JPA persistence working (MVP)
2. Add Phase 4 → Verify Clean Architecture maintained
3. Add Phase 5 → Full test coverage validated
4. Add Phase 6 → Final cleanup and documentation

### Sequential (Single Developer)

1. Phase 1 (Setup)
2. Phase 2 (Foundational) - start with entities, then repositories, then converter, then JDBC deletion
3. Phase 3 (US1) - write integration tests first (should fail), implement gateways to pass tests
4. Phase 4 (US2) - verify architecture
5. Phase 5 (US3) - validate full integration
6. Phase 6 (Polish) - final checks

### Parallel Team Strategy (3 developers)

1. All developers complete Phase 1 + 2 together
   - Dev A: Entity creation (T003-T011)
   - Dev B: Repository creation (T012-T020)
   - Dev C: Converter + JDBC deletion (T021-T031)

2. Phase 3 - parallel by gateway:
   - Dev A: OnCallPeriod + EngineerProfile gateways + tests (T032-T033, T041-T042)
   - Dev B: UserPreferences + CompensationRate + OnCallDayEntry gateways + tests (T034-T036, T043-T045)
   - Dev C: Incident + OvertimeEntry + HolidayOverride + RegistrationSummary gateways + tests (T037-T040, T046-T049)
   - All: Validation (T050-T052)

3. Phase 4: Any developer (sequential verification)

4. Phase 5: Any developer (full integration testing)

5. Phase 6: All developers (final validation)

---

## Checklist for Each Task

Before marking a task complete:

- ✅ Code compiles with zero errors
- ✅ Task-specific tests pass (if tests added)
- ✅ File paths are correct (use absolute paths in IDE)
- ✅ Constructor injection used (no `@Autowired` on fields)
- ✅ Domain records untouched (no JPA annotations added)
- ✅ Entity classes package-private (not public, only for infrastructure use)
- ✅ Commit changes with conventional commit message per `AGENTS.md`

---

## Notes

- **[P] marker**: Tasks can run in parallel (different files, no dependencies between them)
- **[Story] label**: Identifies which user story a task belongs to (US1, US2, US3)
- Each user story is independently testable and can be validated before moving to the next
- Verify tests fail before implementing (TDD approach for gateways)
- Stop at any checkpoint to validate story independently
- No raw SQL or `NamedParameterJdbcTemplate` should appear in any new code
- All new gateway implementations inherit from domain gateway interfaces (no new interfaces added)
