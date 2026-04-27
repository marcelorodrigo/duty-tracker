# Tasks: Spring Data JPA Migration

**Input**: Design documents from `/specs/002-spring-data-jpa-migration/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, quickstart.md ✓

**Organization**: Tasks are grouped by user story. US1 (JPA gateways) blocks US2 (ArchUnit verification) and US3 (integration tests). US3 depends on US1 gateways existing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies between them)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)

---

## Phase 1: Setup (Dependencies & Config)

**Purpose**: Swap persistence starter and align application configuration — must be complete before any JPA class compiles.

- [ ] T001 Update `backend/pom.xml`: remove `spring-boot-starter-data-jdbc`; add `spring-boot-starter-data-jpa`; add `spring-boot-testcontainers` (test scope)
- [ ] T002 Update `backend/src/main/resources/application.yml`: remove `spring.data.jdbc.dialect: postgresql`; add `spring.jpa.hibernate.ddl-auto: validate`, `spring.jpa.show-sql: false`, `spring.jpa.properties.hibernate.jdbc.time_zone: UTC`

**Checkpoint**: `mvn clean compile` should fail cleanly (no JDBC beans) before any JPA classes are added.

---

## Phase 2: Foundational (JPA Infrastructure)

**Purpose**: Attribute converter, all nine `@Entity` classes, and all nine `JpaRepository` interfaces. **No gateway can be implemented until this phase is complete.**

**⚠️ CRITICAL**: All user story work in Phases 3–5 blocks on this phase.

### Attribute Converter

- [ ] T003 Create `DayOfWeekSetConverter` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/converter/DayOfWeekSetConverter.java` — implements `AttributeConverter<Set<DayOfWeek>, String>` with `@Converter(autoApply = true)`; class is package-private; replaces `WorkingDaysConverter`

### Root Entities (no FK references to other entities — fully parallel)

- [ ] T004 [P] Create `OnCallPeriodEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/OnCallPeriodEntity.java` — fields: `id` (`@Id @GeneratedValue(IDENTITY)`), `startDateTime` (`LocalDateTime`), `endDateTime` (`LocalDateTime`), `createdAt` (`Instant`); table `on_call_period`; class package-private
- [ ] T005 [P] Create `EngineerProfileEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/EngineerProfileEntity.java` — fields: `id`, `employeeType` (`@Enumerated(STRING)`), `workingDays` (`Set<DayOfWeek>` — converter auto-applied), `workStartTime` (`LocalTime`), `workEndTime` (`LocalTime`), `createdAt` (`Instant`); table `engineer_profile`
- [ ] T006 [P] Create `UserPreferencesEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/UserPreferencesEntity.java` — fields: `id`, `colorScheme` (`@Enumerated(STRING)`), `onboardingStep` (`@Enumerated(STRING)`); table `user_preferences`
- [ ] T007 [P] Create `CompensationRateEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/CompensationRateEntity.java` — fields: `id`, `employeeType` (`@Enumerated(STRING)`), `rateCategory` (`@Enumerated(STRING)`), `label` (`String`), `timeFrom` (`LocalTime`, nullable), `timeTo` (`LocalTime`, nullable), `percentage` (`BigDecimal`); table `compensation_rate`
- [ ] T008 [P] Create `RegistrationSummaryEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/RegistrationSummaryEntity.java` — fields: `id`, `label`, `periodStart` (`LocalDate`), `periodEnd` (`LocalDate`), `createdAt` (`Instant`), `updatedAt` (`Instant`); table `registration_summary`

### FK Child Entities (depend on T004 `OnCallPeriodEntity` — parallel with each other)

- [ ] T009 [P] Create `HolidayOverrideEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/HolidayOverrideEntity.java` — fields: `id`, `onCallPeriod` (`@ManyToOne(fetch=LAZY) @JoinColumn(name="on_call_period_id", nullable=false)`), `date` (`LocalDate`); table `holiday_override`
- [ ] T010 [P] Create `OnCallDayEntryEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/OnCallDayEntryEntity.java` — fields: `id`, `onCallPeriod` (`@ManyToOne(fetch=LAZY) @JoinColumn(name="on_call_period_id", nullable=false)`), `date`, `hours` (`BigDecimal`), `rateType` (`@Enumerated(STRING)`), `capped` (`boolean`), `timeForTimeFlag` (`boolean`), `manualOverride` (`boolean`); table `on_call_day_entry`
- [ ] T011 [P] Create `IncidentEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/IncidentEntity.java` — fields: `id`, `onCallPeriod` (`@ManyToOne(fetch=LAZY) @JoinColumn(name="on_call_period_id", nullable=true)` — **nullable**), `date` (`LocalDate`), `startTime` (`LocalTime`), `endTime` (`LocalTime`), `createdAt` (`Instant`); table `incident`
- [ ] T012 Create `OvertimeEntryEntity` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/entity/OvertimeEntryEntity.java` — fields: `id`, `incident` (`@ManyToOne(fetch=LAZY) @JoinColumn(name="incident_id", nullable=false)`), `overtimeHours`, `allowanceHours`, `allowancePercentage` (all `BigDecimal`), `timeFrom` (`LocalTime`, nullable), `timeTo` (`LocalTime`, nullable), `isAllowanceEntry` (`boolean`), `manualOverride` (`boolean`); table `overtime_entry`

### JPA Repositories (parallel within each group; depend on their entity existing)

- [ ] T013 [P] Create `OnCallPeriodJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/OnCallPeriodJpaRepository.java` — `extends JpaRepository<OnCallPeriodEntity, Long>`; no custom methods
- [ ] T014 [P] Create `EngineerProfileJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/EngineerProfileJpaRepository.java` — `extends JpaRepository<EngineerProfileEntity, Long>`; no custom methods
- [ ] T015 [P] Create `UserPreferencesJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/UserPreferencesJpaRepository.java` — `extends JpaRepository<UserPreferencesEntity, Long>`; no custom methods
- [ ] T016 [P] Create `CompensationRateJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/CompensationRateJpaRepository.java` — `extends JpaRepository<CompensationRateEntity, Long>`; add `List<CompensationRateEntity> findByEmployeeType(EmployeeType type)`
- [ ] T017 [P] Create `RegistrationSummaryJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/RegistrationSummaryJpaRepository.java` — `extends JpaRepository<RegistrationSummaryEntity, Long>`; no custom methods
- [ ] T018 [P] Create `HolidayOverrideJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/HolidayOverrideJpaRepository.java` — `extends JpaRepository<HolidayOverrideEntity, Long>`; add `List<HolidayOverrideEntity> findByOnCallPeriodId(Long onCallPeriodId)` and `Optional<HolidayOverrideEntity> findByOnCallPeriodIdAndDate(Long onCallPeriodId, LocalDate date)`
- [ ] T019 [P] Create `OnCallDayEntryJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/OnCallDayEntryJpaRepository.java` — `extends JpaRepository<OnCallDayEntryEntity, Long>`; add `List<OnCallDayEntryEntity> findByOnCallPeriodId(Long onCallPeriodId)` and `@Transactional void deleteByOnCallPeriod(OnCallPeriodEntity onCallPeriod)`
- [ ] T020 [P] Create `IncidentJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/IncidentJpaRepository.java` — `extends JpaRepository<IncidentEntity, Long>`; add `List<IncidentEntity> findByOnCallPeriodId(Long onCallPeriodId)`
- [ ] T021 Create `OvertimeEntryJpaRepository` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/repository/OvertimeEntryJpaRepository.java` — `extends JpaRepository<OvertimeEntryEntity, Long>`; add `List<OvertimeEntryEntity> findByIncidentId(Long incidentId)` and `@Transactional void deleteByIncident(IncidentEntity incident)`

**Checkpoint**: `mvn clean compile` must succeed with zero errors before Phase 3 begins.

---

## Phase 3: User Story 1 — Data Persistence via JPA Repositories (Priority: P1) 🎯 MVP

**Goal**: Replace all nine `JdbcXxxGateway` implementations with `JpaXxxGateway` implementations backed by Spring Data JPA. Zero `NamedParameterJdbcTemplate` calls remain in the persistence gateway package.

**Independent Test**: `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` returns no output; `mvn clean package -DskipTests` exits 0.

### Gateway Implementations (all [P] — independent files)

- [ ] T022 [P] [US1] Implement `JpaOnCallPeriodGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallPeriodGateway.java` — constructor-injects `OnCallPeriodJpaRepository`; implements `save` (re-fetches after save), `findById`, `findAll`, `deleteById`; private `toEntity`/`toDomain`/`toDomainList` helpers; annotated `@Component`
- [ ] T023 [P] [US1] Implement `JpaEngineerProfileGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaEngineerProfileGateway.java` — implements `save` (upsert: if `id==null` insert; else update) and `find` (returns `findAll().stream().findFirst()`); re-fetches after save via `findById`
- [ ] T024 [P] [US1] Implement `JpaUserPreferencesGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaUserPreferencesGateway.java` — same `save`/`find` pattern as `EngineerProfileGateway`
- [ ] T025 [P] [US1] Implement `JpaCompensationRateGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaCompensationRateGateway.java` — implements `saveAll` (iterates save), `findAll`, `findByEmployeeType` (delegates to repo), `update` (save entity with existing id), `deleteById`, `findById`
- [ ] T026 [P] [US1] Implement `JpaRegistrationSummaryGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaRegistrationSummaryGateway.java` — implements `save`, `findById`, `findAll`, `deleteById`, `existsAny` (`repository.count() > 0`)
- [ ] T027 [P] [US1] Implement `JpaHolidayOverrideGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaHolidayOverrideGateway.java` — implements `save`, `findByOnCallPeriodId`, `deleteById`, `findByOnCallPeriodIdAndDate`; `toDomain` extracts `entity.getOnCallPeriod().getId()`
- [ ] T028 [P] [US1] Implement `JpaOnCallDayEntryGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallDayEntryGateway.java` — implements `save`, `saveAll`, `findByOnCallPeriodId`, `findById`, `deleteById`, `deleteByOnCallPeriodId` (loads period entity then calls `repo.deleteByOnCallPeriod(entity)`)
- [ ] T029 [P] [US1] Implement `JpaIncidentGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaIncidentGateway.java` — implements `save`, `findById`, `findByOnCallPeriodId`, `findAll`, `deleteById`; `toEntity` sets `onCallPeriod = null` when `domain.onCallPeriodId() == null`; `toDomain` handles null `onCallPeriod` → `null` id
- [ ] T030 [US1] Implement `JpaOvertimeEntryGateway` in `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/JpaOvertimeEntryGateway.java` — implements `save`, `saveAll`, `findByIncidentId`, `findById`, `deleteById`, `deleteByIncidentId` (loads incident entity then calls `repo.deleteByIncident(entity)`)

### Remove Obsolete JDBC Classes

- [ ] T031 [US1] Delete all nine `JdbcXxxGateway` classes from `backend/src/main/java/com/dutytracker/infrastructure/persistence/gateway/`: `JdbcCompensationRateGateway.java`, `JdbcEngineerProfileGateway.java`, `JdbcHolidayOverrideGateway.java`, `JdbcIncidentGateway.java`, `JdbcOnCallDayEntryGateway.java`, `JdbcOnCallPeriodGateway.java`, `JdbcOvertimeEntryGateway.java`, `JdbcRegistrationSummaryGateway.java`, `JdbcUserPreferencesGateway.java`
- [ ] T032 [US1] Delete `backend/src/main/java/com/dutytracker/infrastructure/config/JdbcConfig.java`
- [ ] T033 [US1] Delete `backend/src/main/java/com/dutytracker/infrastructure/persistence/converter/WorkingDaysConverter.java`
- [ ] T034 [US1] Verify compilation and unit tests pass: `cd backend && mvn clean package -DskipTests` then `mvn test -Dtest="!Jpa*GatewayTest"` (all existing unit + controller tests must pass)

**Checkpoint**: `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` returns no output. All 40 existing non-gateway tests pass.

---

## Phase 4: User Story 2 — Clean Architecture Preserved (Priority: P2)

**Goal**: Confirm domain records are annotation-free, all ArchUnit gates pass (CA-01, CA-02, CA-03, CC-01, CC-02), and the application starts correctly.

**Independent Test**: `mvn test -Dtest="*ArchitectureTest"` passes; `grep -r "jakarta.persistence" backend/src/main/java/com/dutytracker/domain` returns no output.

- [ ] T035 [US2] Verify zero persistence imports in domain layer: run `grep -r "jakarta.persistence" backend/src/main/java/com/dutytracker/domain` — must produce no output; fix any violation found
- [ ] T036 [US2] Run ArchUnit architecture tests and confirm all gates pass: `cd backend && mvn test -Dtest="*ArchitectureTest"` — CA-01 (no infra imports in domain/app), CA-02 (UseCases present), CC-02 (no field injection) must all be green; fix any failure
- [ ] T037 [US2] Start application against local Postgres and verify no `SchemaManagementException`: `docker compose up -d postgres && cd backend && mvn spring-boot:run` — application must reach "Started DutyTrackerApplication" without schema validation errors

**Checkpoint**: All ArchUnit gates green. Application starts cleanly.

---

## Phase 5: User Story 3 — Integration Tests for All Nine Gateways (Priority: P3)

**Goal**: Each `JpaXxxGateway` has a `@DataJpaTest` + Testcontainers integration test covering save, findById, deleteById, and custom finder/delete methods. `JpaIncidentGatewayTest` includes a null-FK scenario.

**Independent Test**: `mvn test -Dtest="Jpa*GatewayTest"` passes (green) with all nine test classes running.

### Integration Test Classes (all [P] — independent files, same test pattern)

- [ ] T038 [P] [US3] Implement `JpaOnCallPeriodGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallPeriodGatewayTest.java` — `@DataJpaTest @AutoConfigureTestDatabase(replace=NONE) @Testcontainers`; `@Container @ServiceConnection static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")`; tests: `save_returnsNonNullId`, `findById_returnsAllFields`, `deleteById_removesRecord`, `findAll_returnsPersistedRecords`
- [ ] T039 [P] [US3] Implement `JpaEngineerProfileGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaEngineerProfileGatewayTest.java` — tests: `save_returnsNonNullId`, `find_returnsAllFields` (including `workingDays` `Set<DayOfWeek>` round-trip for non-trivial set per SC-003), `save_updatesExistingRecord`
- [ ] T040 [P] [US3] Implement `JpaUserPreferencesGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaUserPreferencesGatewayTest.java` — tests: `save_returnsNonNullId`, `find_returnsAllFields`, `save_updatesExistingRecord`
- [ ] T041 [P] [US3] Implement `JpaCompensationRateGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaCompensationRateGatewayTest.java` — tests: `saveAll_persistsAllRates`, `findById_returnsAllFields`, `findByEmployeeType_returnsCorrectSubset`, `deleteById_removesRecord`, `update_changesPersistedFields`
- [ ] T042 [P] [US3] Implement `JpaRegistrationSummaryGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaRegistrationSummaryGatewayTest.java` — tests: `save_returnsNonNullId`, `findById_returnsAllFields`, `deleteById_removesRecord`, `findAll_returnsAll`, `existsAny_returnsTrueWhenRecordExists`, `existsAny_returnsFalseWhenEmpty`
- [ ] T043 [P] [US3] Implement `JpaHolidayOverrideGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaHolidayOverrideGatewayTest.java` — tests: `save_returnsNonNullId`, `findByOnCallPeriodId_returnsCorrectSubset`, `findByOnCallPeriodIdAndDate_returnsMatchingOverride`, `deleteById_removesRecord`
- [ ] T044 [P] [US3] Implement `JpaOnCallDayEntryGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOnCallDayEntryGatewayTest.java` — tests: `save_returnsNonNullId`, `findById_returnsAllFields`, `findByOnCallPeriodId_returnsCorrectSubset`, `deleteById_removesRecord`, `deleteByOnCallPeriodId_removesAllEntriesForPeriod`
- [ ] T045 [P] [US3] Implement `JpaIncidentGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaIncidentGatewayTest.java` — tests: `save_returnsNonNullId`, `findById_returnsAllFields`, `findByOnCallPeriodId_returnsCorrectSubset`, `deleteById_removesRecord`, **`save_withNullOnCallPeriodId_returnsNullOnCallPeriodId`** (SC-003 mandatory null-FK scenario)
- [ ] T046 [P] [US3] Implement `JpaOvertimeEntryGatewayTest` in `backend/src/test/java/com/dutytracker/infrastructure/persistence/gateway/JpaOvertimeEntryGatewayTest.java` — tests: `save_returnsNonNullId`, `findById_returnsAllFields`, `findByIncidentId_returnsCorrectSubset`, `deleteById_removesRecord`, `deleteByIncidentId_removesAllEntriesForIncident`
- [ ] T047 [US3] Run all gateway integration tests: `cd backend && mvn test -Dtest="Jpa*GatewayTest"` — all nine test classes must pass

**Checkpoint**: All 9 `Jpa*GatewayTest` classes pass. FR-006 and SC-003 satisfied.

---

## Phase 6: Polish & Verification

**Purpose**: Full-suite green, success criteria confirmed, documentation verified.

- [ ] T048 Run full test suite and confirm zero failures: `cd backend && mvn test` — all 40 existing tests + 9 new gateway integration tests must pass
- [ ] T049 Confirm SC-001: `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` — must produce zero output; fix any stragglers
- [ ] T050 Confirm SC-004: `cd backend && mvn test -Dtest="*ArchitectureTest"` — all CA/CC gates green
- [ ] T051 [P] Confirm SC-006: `grep -r "jakarta.persistence\|org.hibernate" backend/src/main/java/com/dutytracker/domain` — must produce zero output
- [ ] T052 Confirm application starts in Docker Compose: `docker compose up --build` — backend reaches healthy state; spot-check one API endpoint responds correctly

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    └── Phase 2 (Foundational) ← BLOCKS all user stories
            ├── Phase 3 (US1 — Gateways) ← MVP delivery point
            │       ├── Phase 4 (US2 — ArchUnit verification)
            │       └── Phase 5 (US3 — Integration tests)
            │               └── Phase 6 (Polish)
```

### User Story Dependencies

| Story | Depends On | Can Start When |
|-------|-----------|----------------|
| **US1** (P1) | Phase 2 complete | After T021 ✓ |
| **US2** (P2) | US1 complete | After T034 ✓ |
| **US3** (P3) | US1 complete | After T034 ✓ |

### Within Phase 2 (Foundational)

```
T003 (DayOfWeekSetConverter)          — no dependencies
T004–T008 [P]  (root entities)        — depend only on T003 (converter referenced in EngineerProfileEntity)
T009–T011 [P]  (OnCallPeriod children)— depend on T004 (OnCallPeriodEntity must exist)
T012           (OvertimeEntryEntity)  — depends on T011 (IncidentEntity must exist)
T013–T017 [P]  (root repos)           — depend on their respective entity
T018–T020 [P]  (FK child repos)       — depend on their respective entity
T021           (OvertimeEntryRepo)    — depends on T012
```

### Within Phase 3 (US1 Gateways)

```
T022–T030 [P]  (9 gateways)          — all independent; all depend on Phase 2
T031–T033      (deletions)           — depend on T022–T030 compiling successfully
T034           (verification)        — depends on T031–T033
```

---

## Parallel Execution Examples

### Phase 2: All Root Entities at Once

```
# Parallel:
Task: "Create OnCallPeriodEntity in .../entity/OnCallPeriodEntity.java"      (T004)
Task: "Create EngineerProfileEntity in .../entity/EngineerProfileEntity.java" (T005)
Task: "Create UserPreferencesEntity in .../entity/UserPreferencesEntity.java" (T006)
Task: "Create CompensationRateEntity in .../entity/CompensationRateEntity.java" (T007)
Task: "Create RegistrationSummaryEntity in .../entity/RegistrationSummaryEntity.java" (T008)
# Then parallel:
Task: "Create HolidayOverrideEntity" (T009)
Task: "Create OnCallDayEntryEntity"  (T010)
Task: "Create IncidentEntity"        (T011)
```

### Phase 3: All Root Gateways at Once

```
# Parallel (any order after Phase 2):
Task: "Implement JpaOnCallPeriodGateway"         (T022)
Task: "Implement JpaEngineerProfileGateway"      (T023)
Task: "Implement JpaUserPreferencesGateway"      (T024)
Task: "Implement JpaCompensationRateGateway"     (T025)
Task: "Implement JpaRegistrationSummaryGateway"  (T026)
Task: "Implement JpaHolidayOverrideGateway"      (T027)
Task: "Implement JpaOnCallDayEntryGateway"       (T028)
Task: "Implement JpaIncidentGateway"             (T029)
Task: "Implement JpaOvertimeEntryGateway"        (T030)
```

### Phase 5: All Integration Tests at Once

```
# Parallel (all after T034):
Task: "JpaOnCallPeriodGatewayTest"     (T038)
Task: "JpaEngineerProfileGatewayTest"  (T039)
Task: "JpaUserPreferencesGatewayTest"  (T040)
Task: "JpaCompensationRateGatewayTest" (T041)
Task: "JpaRegistrationSummaryGatewayTest" (T042)
Task: "JpaHolidayOverrideGatewayTest"  (T043)
Task: "JpaOnCallDayEntryGatewayTest"   (T044)
Task: "JpaIncidentGatewayTest"         (T045)
Task: "JpaOvertimeEntryGatewayTest"    (T046)
```

---

## Implementation Strategy

### MVP First (US1 Only — minimum shippable)

1. Complete Phase 1: Setup (T001–T002)
2. Complete Phase 2: Foundational (T003–T021)
3. Complete Phase 3: US1 Gateways (T022–T034)
4. **STOP and VALIDATE**: `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` → zero results; `mvn clean package -DskipTests` → exits 0; existing 40 tests pass
5. ✅ Core persistence migration is done and provably correct

### Incremental Delivery

1. MVP (US1) → Persistent layer fully migrated
2. Add US2 verification (T035–T037) → Architecture integrity confirmed
3. Add US3 integration tests (T038–T047) → Full regression coverage
4. Polish (T048–T052) → All success criteria confirmed

---

## Notes

- `[P]` tasks = different files, no blocking dependencies between them in the same group
- `[US1]/[US2]/[US3]` labels map tasks to the three user stories in spec.md
- All entities are **package-private** — never `public class XxxEntity`
- `save()` in every gateway **always** re-fetches: `repository.findById(saved.getId()).orElseThrow()`
- `JpaIncidentGatewayTest` MUST include the null-onCallPeriodId test (T045) — this is a hard SC-003 requirement
- `@Transactional` is required on `deleteByOnCallPeriod` (T019) and `deleteByIncident` (T021) derived-delete methods
- The `DayOfWeekSetConverter` has `autoApply=true` — no `@Convert` annotation needed on entity fields
- Commit after each phase or logical group; verify build passes at each checkpoint
