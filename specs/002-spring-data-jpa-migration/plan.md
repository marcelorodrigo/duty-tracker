# Implementation Plan: Spring Data JPA Migration

**Branch**: `feat/spring-jpa` | **Date**: 2026-04-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-spring-data-jpa-migration/spec.md`

## Summary

Replace nine `JdbcXxxGateway` implementations (backed by `NamedParameterJdbcTemplate`) with nine `JpaXxxGateway` implementations (backed by Spring Data JPA repositories and `@Entity` classes in the infrastructure layer). Domain model records remain annotation-free. Clean Architecture, constructor injection, and the full ArchUnit gate suite are preserved throughout.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0.0, Spring Data JPA (replaces Spring Data JDBC), Hibernate 7.x (transitive), Flyway, Jollyday 0.26.0
**Storage**: PostgreSQL (schema owned by Flyway; `ddl-auto: validate`)
**Testing**: JUnit 5, AssertJ, Mockito, Spring Boot Test, Testcontainers 1.20.4 (`@DataJpaTest` + `@ServiceConnection`)
**Target Platform**: Linux server / Docker Compose (postgres:18-alpine)
**Project Type**: Web service (Spring Boot REST API)
**Performance Goals**: No latency regression vs. JDBC baseline; N+1 prevented by `FetchType.LAZY` for all `@ManyToOne` associations
**Constraints**: Flyway migration scripts are immutable; `ddl-auto: validate`; all ArchUnit gates (CA-01, CA-02, CA-03, CC-01, CC-02, T-01, S-01) must continue to pass; zero raw SQL in the persistence gateway package after migration
**Scale/Scope**: 9 aggregates, ~40 existing test classes, 9 new JPA gateway integration test classes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Check | Status |
|------|-------|--------|
| **CA-01** | No domain/application class imports infrastructure or presentation types | ✅ — JPA `@Entity` classes are placed in `infrastructure.persistence.entity`; domain records have zero persistence annotations |
| **CA-02** | Every business operation is represented by a dedicated UseCase class | ✅ — This migration does not add any new business operations; existing UseCase classes are untouched |
| **CA-03** | Every UseCase has a corresponding Request record and RequestValidator | ✅ — No new UseCases introduced; existing UseCase/Validator/Request triples unchanged |
| **CC-01** | No business logic exists in controllers or gateway implementations | ✅ — JPA gateway implementations contain only I/O translation (toEntity/toDomain) and repository delegation; no business rules |
| **CC-02** | No field injection (`@Autowired` on fields) anywhere in the codebase | ✅ — All JPA gateway implementations and repository interfaces use constructor injection; `@Autowired` on fields is banned by ArchUnit |
| **T-01** | Every UseCase, Validator, and Controller has a corresponding test class | ✅ — No new UseCases or Controllers added; 9 new `JpaXxxGatewayTest` integration tests are required by SC-003 |
| **S-01** | Every new dependency or abstraction layer is documented in Complexity Tracking | ✅ — see Complexity Tracking below |

**Post-design re-check**: All gates remain green. The separation between `infrastructure.persistence.entity` (JPA entities) and `domain.model` (domain records) is explicit in the data model. No domain class touches a JPA type.

## Project Structure

### Documentation (this feature)

```text
specs/002-spring-data-jpa-migration/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit.tasks command — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/
├── pom.xml                                          ← T001: swap data-jdbc → data-jpa, add spring-boot-testcontainers
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── application.yml                      ← T002: remove data.jdbc, add jpa config
    │   └── java/com/dutytracker/
    │       ├── domain/                              ← UNCHANGED (model records, gateway interfaces)
    │       ├── application/                         ← UNCHANGED (UseCases, validators)
    │       ├── infrastructure/
    │       │   ├── config/
    │       │   │   └── JdbcConfig.java              ← DELETE (replaced by Spring Boot JPA auto-config)
    │       │   ├── holiday/                         ← UNCHANGED
    │       │   └── persistence/
    │       │       ├── converter/
    │       │       │   ├── WorkingDaysConverter.java  ← DELETE (JDBC converter)
    │       │       │   └── DayOfWeekSetConverter.java ← NEW (JPA @Converter(autoApply=true))
    │       │       ├── entity/                        ← NEW package
    │       │       │   ├── CompensationRateEntity.java
    │       │       │   ├── EngineerProfileEntity.java
    │       │       │   ├── HolidayOverrideEntity.java
    │       │       │   ├── IncidentEntity.java
    │       │       │   ├── OnCallDayEntryEntity.java
    │       │       │   ├── OnCallPeriodEntity.java
    │       │       │   ├── OvertimeEntryEntity.java
    │       │       │   ├── RegistrationSummaryEntity.java
    │       │       │   └── UserPreferencesEntity.java
    │       │       ├── repository/                    ← NEW package
    │       │       │   ├── CompensationRateJpaRepository.java
    │       │       │   ├── EngineerProfileJpaRepository.java
    │       │       │   ├── HolidayOverrideJpaRepository.java
    │       │       │   ├── IncidentJpaRepository.java
    │       │       │   ├── OnCallDayEntryJpaRepository.java
    │       │       │   ├── OnCallPeriodJpaRepository.java
    │       │       │   ├── OvertimeEntryJpaRepository.java
    │       │       │   ├── RegistrationSummaryJpaRepository.java
    │       │       │   └── UserPreferencesJpaRepository.java
    │       │       └── gateway/
    │       │           ├── JdbcCompensationRateGateway.java  ← DELETE
    │       │           ├── JdbcEngineerProfileGateway.java   ← DELETE
    │       │           ├── JdbcHolidayOverrideGateway.java   ← DELETE
    │       │           ├── JdbcIncidentGateway.java          ← DELETE
    │       │           ├── JdbcOnCallDayEntryGateway.java    ← DELETE
    │       │           ├── JdbcOnCallPeriodGateway.java      ← DELETE
    │       │           ├── JdbcOvertimeEntryGateway.java     ← DELETE
    │       │           ├── JdbcRegistrationSummaryGateway.java ← DELETE
    │       │           ├── JdbcUserPreferencesGateway.java   ← DELETE
    │       │           ├── JpaCompensationRateGateway.java   ← NEW
    │       │           ├── JpaEngineerProfileGateway.java    ← NEW
    │       │           ├── JpaHolidayOverrideGateway.java    ← NEW
    │       │           ├── JpaIncidentGateway.java           ← NEW
    │       │           ├── JpaOnCallDayEntryGateway.java     ← NEW
    │       │           ├── JpaOnCallPeriodGateway.java       ← NEW
    │       │           ├── JpaOvertimeEntryGateway.java      ← NEW
    │       │           ├── JpaRegistrationSummaryGateway.java ← NEW
    │       │           └── JpaUserPreferencesGateway.java    ← NEW
    │       └── presentation/                        ← UNCHANGED
    └── test/
        └── java/com/dutytracker/
            ├── ArchitectureTest.java                ← UNCHANGED (must still pass)
            ├── application/usecase/                 ← UNCHANGED
            ├── infrastructure/
            │   ├── holiday/                         ← UNCHANGED
            │   └── persistence/gateway/             ← NEW test classes
            │       ├── JpaCompensationRateGatewayTest.java
            │       ├── JpaEngineerProfileGatewayTest.java
            │       ├── JpaHolidayOverrideGatewayTest.java
            │       ├── JpaIncidentGatewayTest.java
            │       ├── JpaOnCallDayEntryGatewayTest.java
            │       ├── JpaOnCallPeriodGatewayTest.java
            │       ├── JpaOvertimeEntryGatewayTest.java
            │       ├── JpaRegistrationSummaryGatewayTest.java
            │       └── JpaUserPreferencesGatewayTest.java
            └── presentation/api/                    ← UNCHANGED
```

**Structure Decision**: Web application (backend-only change). The migration is confined to `infrastructure.persistence`. Frontend is entirely out of scope.

## Complexity Tracking

| Violation / Addition | Why Needed | Simpler Alternative Rejected Because |
|----------------------|------------|--------------------------------------|
| New `infrastructure.persistence.entity` sub-package | JPA `@Entity` classes must not be in `domain.model` (CA-01). A separate package is the minimal structural change to isolate persistence annotations from the domain. | Annotating domain records with `@Entity` would couple domain to Hibernate and violate CA-01 — not acceptable. |
| New `infrastructure.persistence.repository` sub-package | Spring Data repository interfaces are infrastructure concerns; isolating them keeps the `gateway` package focused on gateway implementations. | Placing repositories in the gateway package would mix interface declarations with implementations and make the package's role ambiguous. |
| `DayOfWeekSetConverter` (new JPA `@Converter`) | `Set<DayOfWeek>` has no built-in JDBC→JPA equivalent; the existing `WorkingDaysConverter` is a Spring Data JDBC `@WritingConverter`/`@ReadingConverter` and is incompatible with the JPA `AttributeConverter` API. | No simpler option: type safety for the enum set requires an explicit converter regardless of which Spring Data module is used. |
| `spring-boot-testcontainers` test dependency | Needed to use `@ServiceConnection` on `PostgreSQLContainer` — the idiomatic Spring Boot 4.x approach that eliminates manual `@DynamicPropertySource` boilerplate. | Manual `@DynamicPropertySource` alternative is more verbose and less integrated with Boot's auto-configuration. |
