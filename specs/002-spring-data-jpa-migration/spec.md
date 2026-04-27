# Feature Specification: Spring Data JPA Migration

**Feature Branch**: `002-spring-data-jpa-migration`  
**Created**: 2026-04-27  
**Status**: Draft  
**Input**: User description: "We will migrate the backend project to use Spring Data and not use raw JDBC. So we will start using @Repository and @Entity classes instead of constructing raw JDBC queries. Make sure to update dependencies and tests"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Data Persistence via JPA Repositories (Priority: P1)

A developer working on the backend no longer writes raw SQL or manually maps result sets. All database reads and writes for the nine aggregate types (EngineerProfile, UserPreferences, OnCallPeriod, OnCallDayEntry, OvertimeEntry, Incident, CompensationRate, HolidayOverride, RegistrationSummary) go through JPA repository interfaces. No `NamedParameterJdbcTemplate` call sites remain in the persistence layer.

**Why this priority**: This is the core deliverable of the migration. All other stories depend on it.

**Independent Test**: Can be verified by confirming the persistence package contains zero references to `NamedParameterJdbcTemplate` and that all nine gateway implementations delegate to JPA repositories, with integration tests confirming data round-trips correctly.

**Acceptance Scenarios**:

1. **Given** the application is running, **When** a developer inspects the persistence package, **Then** no raw JDBC template calls exist — only JPA repository interface invocations.
2. **Given** any aggregate is saved through its gateway, **When** the same aggregate is retrieved by ID, **Then** all fields are identical to what was saved.
3. **Given** a collection query (e.g., find all on-call periods), **When** the query executes, **Then** all persisted records matching the criteria are returned without data loss.

---

### User Story 2 - Clean Architecture Preserved (Priority: P2)

A developer inspects the domain model package and finds zero JPA or persistence framework annotations on any domain class. Domain records (`OnCallPeriod`, `EngineerProfile`, etc.) remain plain Java records with no dependency on any persistence library. The ports-and-adapters boundary is intact: the `domain` and `application` packages have no imports from the `infrastructure` package.

**Why this priority**: The existing architecture contract (CA-01 gate) is a non-negotiable constraint. Violating it would introduce coupling that undermines the entire design of the system.

**Independent Test**: Can be verified by the existing ArchUnit architecture test suite — all CA-01 and CA-02 gate checks must pass after migration. No new architecture violations introduced.

**Acceptance Scenarios**:

1. **Given** the migration is complete, **When** the ArchUnit architecture tests run, **Then** all tests pass with no domain class importing infrastructure types.
2. **Given** a developer adds a new domain field, **When** they edit the domain record, **Then** they do not need to touch any JPA annotation — the mapping concern lives entirely in the infrastructure layer.

---

### User Story 3 - Full Test Coverage for Persistence Layer (Priority: P3)

A developer runs the full test suite and sees integration tests for each of the nine JPA-backed gateway implementations, covering the primary CRUD operations. The existing holiday gateway unit test is unaffected. All test classes pass.

**Why this priority**: The migration replaces the entire persistence mechanism. Without integration tests that exercise the real database (via Testcontainers), there is no confidence that the JPA layer behaves correctly for domain-specific queries and type mappings.

**Independent Test**: Running `mvn test` produces a green build with integration tests for each gateway confirmed by inspecting test output.

**Acceptance Scenarios**:

1. **Given** the test suite runs, **When** all tests complete, **Then** zero failures or errors are reported.
2. **Given** a gateway integration test for `OnCallPeriodGateway`, **When** a period is saved and retrieved, **Then** all fields — including timestamps and foreign key relationships — match exactly.
3. **Given** a gateway integration test for `EngineerProfileGateway`, **When** a profile with a non-trivial `workingDays` set is saved and retrieved, **Then** the `Set<DayOfWeek>` is deserialized correctly.

---

### Edge Cases

- What happens when a custom Java type (e.g., `Set<DayOfWeek>`, `LocalTime`) does not have a built-in JPA converter — is conversion handled consistently across all gateways?
- For all aggregates (both new with null ID and existing with non-null ID), the JPA gateway calls `repository.save()` followed by `repository.findById()` to return the domain record populated with DB-assigned ID and accurate DB-generated timestamp columns (`created_at`, `updated_at`). This pattern (save then re-fetch per FR-009) ensures DB-generated values are always returned from the database, not from the passed-in record.
- What happens when Flyway migration scripts and JPA schema expectations are out of sync — does the application fail fast at startup with a clear error?
- How does the system handle concurrent save operations for the singleton `EngineerProfile` (only one profile row should ever exist)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The persistence layer MUST implement all nine domain gateway interfaces using JPA repositories — no `NamedParameterJdbcTemplate` or raw SQL template calls may remain in the persistence package.
- **FR-002**: Domain model classes (records in the `domain.model` package) MUST NOT be annotated with any persistence framework annotations — they MUST remain plain Java records.
- **FR-003**: The `pom.xml` MUST be updated to replace the Spring Data JDBC starter with the Spring Data JPA starter; no raw JDBC infrastructure dependency may remain unless required by another non-persistence concern.
- **FR-004**: All custom Java type mappings (e.g., `Set<DayOfWeek>` to/from a string column, `LocalTime`, enums) MUST be handled via JPA attribute converters in the infrastructure layer.
- **FR-005**: All existing Flyway migration scripts MUST remain unchanged — the JPA layer MUST NOT auto-generate or modify the database schema (`spring.jpa.hibernate.ddl-auto=validate` or equivalent).
- **FR-006**: Each of the nine gateway implementations MUST have a corresponding integration test that exercises save, find-by-id, and delete operations against a real PostgreSQL database (Testcontainers). Each test class uses `@Transactional` auto-rollback (the `@DataJpaTest` default) for data isolation — no manual `@AfterEach` cleanup is required.
- **FR-007**: All existing ArchUnit architecture tests MUST continue to pass after migration with no new violations introduced.
- **FR-008**: Constructor injection MUST remain the only injection style — no `@Autowired` on fields in any JPA-related class.
- **FR-009**: Gateway `save()` methods MUST return a domain record populated with the DB-assigned ID and accurate DB-generated timestamp values by re-fetching via `repository.findById()` after the JPA `save()` call.

### Key Entities

- **JPA Entity (infrastructure)**: A persistence-layer representation of each domain aggregate, annotated with `@Entity` and `@Table`, living in the `infrastructure.persistence` package. Maps directly to the database schema defined by Flyway migrations. Primary keys use `@GeneratedValue(strategy = GenerationType.IDENTITY)` to match Flyway-defined `BIGSERIAL`/`IDENTITY` columns. Inter-entity foreign-key relationships (e.g., `Incident → OnCallPeriod`) are represented as `@ManyToOne(fetch = FetchType.LAZY)` object references with `@JoinColumn`; nullable FKs (e.g., `Incident.onCallPeriodId`) map to a nullable `@ManyToOne` field.
- **JPA Repository**: A Spring Data `JpaRepository` interface per aggregate, providing standard CRUD and any custom query methods. Lives in `infrastructure.persistence`.
- **JPA Attribute Converter**: An `AttributeConverter` implementation per custom type (`Set<DayOfWeek>`, etc.), replacing the current manual serialization in gateway `mapRow` methods.
- **Gateway Implementation**: The concrete class implementing each domain `Gateway` port, now delegating to a `JpaRepository` instead of `NamedParameterJdbcTemplate`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After migration, zero references to `NamedParameterJdbcTemplate` or `JdbcTemplate` exist in the `infrastructure.persistence.gateway` package.
- **SC-002**: All existing tests pass — zero test failures or errors after migration (`mvn test` exits with code 0).
- **SC-003**: Integration tests cover all nine gateway implementations, each with at minimum a save + find-by-id + delete scenario. The `JpaIncidentGatewayTest` MUST additionally include a scenario where an `Incident` with a null `onCallPeriodId` is saved and retrieved, verifying the nullable `@ManyToOne` mapping is handled correctly.
- **SC-004**: All ArchUnit architecture gate checks (CA-01, CA-02, CC-01, CC-02) pass with no new violations.
- **SC-005**: The application starts successfully in the Docker Compose environment and all API endpoints respond correctly after migration.
- **SC-006**: Zero domain model classes in the `domain.model` package contain any import from a persistence framework.

## Assumptions

- The existing Flyway migration scripts define the authoritative database schema; the JPA layer will validate against them, not the other way around. No schema changes are needed as part of this migration.
- The nine existing domain gateway interfaces remain unchanged — only their infrastructure implementations are replaced.
- The `WorkingDaysConverter` already in the codebase will be adapted (or replaced) as a JPA `AttributeConverter` — it is not a new concern.
- Spring Boot 4.x with Java 25 is compatible with Spring Data JPA and Hibernate 7.x; no version blockers exist.
- The migration scope is limited to the `infrastructure.persistence` package. The `holiday` gateway and all `application` and `presentation` code are out of scope.
- Testcontainers is already declared as a test dependency — no new testing infrastructure needs to be introduced.

## Clarifications

### Session 2026-04-27

- Q: How should JPA gateway `save()` methods return DB-generated timestamp values? → A: Re-fetch via `repository.findById()` after `save()` — always returns accurate DB timestamps.
- Q: How should JPA entities represent foreign-key relationships to other entities? → A: `@ManyToOne(fetch = FetchType.LAZY)` object references with `@JoinColumn`; nullable FKs map to nullable `@ManyToOne` fields.
- Q: What data isolation strategy should gateway integration tests use? → A: `@Transactional` auto-rollback per test method (`@DataJpaTest` default).
- Q: Should `JpaIncidentGatewayTest` explicitly test saving/retrieving an `Incident` with a null `onCallPeriodId`? → A: Yes — extend SC-003 to require a null-FK scenario for the `Incident` gateway test.
- Q: What `@GeneratedValue` strategy should JPA entities use for their `Long id` primary keys? → A: `GenerationType.IDENTITY` — matches Flyway-defined BIGSERIAL/IDENTITY columns, no extra sequence objects needed.
