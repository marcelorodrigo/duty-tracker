# Implementation Plan: Spring Data JPA Migration

**Branch**: `master` | **Date**: 2026-04-27 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/002-spring-data-jpa-migration/spec.md`

## Summary

Replace the nine raw `NamedParameterJdbcTemplate`-based JDBC gateway implementations with Spring Data JPA repository-backed implementations. Domain model records remain unchanged (zero framework annotations). New infrastructure-layer JPA entity classes bridge the JPA layer and domain records. All timestamps use `Instant` end-to-end with Hibernate configured to store and read UTC. Integration tests cover every gateway using `@DataJpaTest` + Testcontainers.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**:
- Backend: Spring Boot 4.0.0, Spring Data JPA (replacing Spring Data JDBC), Hibernate 7.x (managed by Boot BOM), `org.postgresql:postgresql`, `spring-boot-starter-flyway`, `flyway-database-postgresql`, `jollyday-core` + `jollyday-jaxb` v0.26.0
- Test: JUnit 5, AssertJ, Mockito, `spring-boot-starter-test`, `spring-boot-starter-webmvc-test`, `archunit-junit5` v1.3.0, `testcontainers` + `testcontainers:postgresql` v1.20.4

**Storage**: PostgreSQL 18 via Docker; schema owned exclusively by Flyway  
**Testing**: JUnit 5, AssertJ, Mockito, Spring Boot Test (`@DataJpaTest` slice + Testcontainers for gateway integration tests)  
**Target Platform**: Local web application at `localhost`; Docker Compose deployment  
**Project Type**: Web application — Spring Boot REST API backend (this feature scope: backend only)  
**Performance Goals**: Sub-second response for all API calls; single-user load  
**Constraints**: Constructor injection only; domain records must remain annotation-free; Flyway is the sole schema authority (`ddl-auto=validate`); all timestamps stored as UTC  
**Scale/Scope**: 1 user, ~52 registration summaries per year, 9 persistence gateway implementations to migrate

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Gate | Check | Status |
|------|-------|--------|
| **CA-01** | No domain/application class imports infrastructure or presentation types | ✅ Domain records remain pure Java records. JPA entity classes live only in `infrastructure.persistence.entity` — no import from domain or application layers into infrastructure, and critically no infrastructure import into domain/application. |
| **CA-02** | Every business operation is represented by a dedicated UseCase class | ✅ This migration touches only the infrastructure persistence layer. No business operations are added or changed; all existing UseCases remain intact. |
| **CA-03** | Every UseCase has a corresponding Request record and RequestValidator | ✅ No new UseCases introduced. Existing pairs unaffected. |
| **CC-01** | No business logic exists in controllers or gateway implementations | ✅ New `JpaXxxGateway` implementations contain only I/O translation (entity ↔ domain record mapping) and JPA repository delegation — zero business logic. |
| **CC-02** | No field injection (`@Autowired` on fields) anywhere in the codebase | ✅ All new gateway implementations and JPA repositories use constructor injection exclusively. |
| **T-01** | Every UseCase, Validator, and Controller has a corresponding test class | ✅ No new UseCases, Validators, or Controllers. New gateway implementations each get an integration test class (`JpaXxxGatewayTest`). |
| **S-01** | Every new dependency or abstraction layer is documented in Complexity Tracking | ✅ Dependency change documented below. |

## Project Structure

### Documentation (this feature)

```text
specs/002-spring-data-jpa-migration/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── api.md           ← Phase 1 output (no API changes; cross-references feature 001)
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code Changes

```text
backend/
├── pom.xml                                   ← swap data-jdbc → data-jpa
├── src/
│   ├── main/
│   │   ├── java/com/dutytracker/
│   │   │   ├── domain/                       ← UNCHANGED
│   │   │   ├── application/                  ← UNCHANGED
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── entity/               ← NEW: 9 JPA @Entity classes
│   │   │   │   │   │   ├── OnCallPeriodJpaEntity.java
│   │   │   │   │   │   ├── EngineerProfileJpaEntity.java
│   │   │   │   │   │   ├── UserPreferencesJpaEntity.java
│   │   │   │   │   │   ├── CompensationRateJpaEntity.java
│   │   │   │   │   │   ├── OnCallDayEntryJpaEntity.java
│   │   │   │   │   │   ├── IncidentJpaEntity.java
│   │   │   │   │   │   ├── OvertimeEntryJpaEntity.java
│   │   │   │   │   │   ├── HolidayOverrideJpaEntity.java
│   │   │   │   │   │   └── RegistrationSummaryJpaEntity.java
│   │   │   │   │   ├── repository/           ← NEW: 9 JpaRepository interfaces
│   │   │   │   │   │   ├── OnCallPeriodJpaRepository.java
│   │   │   │   │   │   ├── EngineerProfileJpaRepository.java
│   │   │   │   │   │   ├── UserPreferencesJpaRepository.java
│   │   │   │   │   │   ├── CompensationRateJpaRepository.java
│   │   │   │   │   │   ├── OnCallDayEntryJpaRepository.java
│   │   │   │   │   │   ├── IncidentJpaRepository.java
│   │   │   │   │   │   ├── OvertimeEntryJpaRepository.java
│   │   │   │   │   │   ├── HolidayOverrideJpaRepository.java
│   │   │   │   │   │   └── RegistrationSummaryJpaRepository.java
│   │   │   │   │   ├── converter/
│   │   │   │   │   │   ├── DayOfWeekSetConverter.java   ← NEW (JPA AttributeConverter)
│   │   │   │   │   │   └── WorkingDaysConverter.java    ← DELETED
│   │   │   │   │   └── gateway/
│   │   │   │   │       ├── JpaOnCallPeriodGateway.java        ← NEW
│   │   │   │   │       ├── JpaEngineerProfileGateway.java     ← NEW
│   │   │   │   │       ├── JpaUserPreferencesGateway.java     ← NEW
│   │   │   │   │       ├── JpaCompensationRateGateway.java    ← NEW
│   │   │   │   │       ├── JpaOnCallDayEntryGateway.java      ← NEW
│   │   │   │   │       ├── JpaIncidentGateway.java            ← NEW
│   │   │   │   │       ├── JpaOvertimeEntryGateway.java       ← NEW
│   │   │   │   │       ├── JpaHolidayOverrideGateway.java     ← NEW
│   │   │   │   │       ├── JpaRegistrationSummaryGateway.java ← NEW
│   │   │   │   │       ├── JdbcOnCallPeriodGateway.java       ← DELETED
│   │   │   │   │       ├── JdbcEngineerProfileGateway.java    ← DELETED
│   │   │   │   │       ├── JdbcUserPreferencesGateway.java    ← DELETED
│   │   │   │   │       ├── JdbcCompensationRateGateway.java   ← DELETED
│   │   │   │   │       ├── JdbcOnCallDayEntryGateway.java     ← DELETED
│   │   │   │   │       ├── JdbcIncidentGateway.java           ← DELETED
│   │   │   │   │       ├── JdbcOvertimeEntryGateway.java      ← DELETED
│   │   │   │   │       ├── JdbcHolidayOverrideGateway.java    ← DELETED
│   │   │   │   │       └── JdbcRegistrationSummaryGateway.java← DELETED
│   │   │   │   └── holiday/                  ← UNCHANGED
│   │   │   └── presentation/                 ← UNCHANGED
│   │   └── resources/
│   │       ├── application.yml               ← UPDATED (JPA config replaces JDBC config)
│   │       └── db/migration/                 ← UNCHANGED (Flyway scripts untouched)
│   └── test/
│       └── java/com/dutytracker/
│           ├── application/                  ← UNCHANGED
│           ├── infrastructure/
│           │   ├── holiday/                  ← UNCHANGED
│           │   └── persistence/
│           │       └── gateway/              ← NEW: 9 JpaXxxGatewayTest classes
│           └── presentation/                 ← UNCHANGED
```

**Structure Decision**: Web application (Option 2 from plan template) — `backend/` for the Spring Boot REST API, `frontend/` for Nuxt 4 SPA. This migration is backend-only; frontend is unaffected.

## Complexity Tracking

| Dependency / Deviation | Why Needed | Simpler Alternative Rejected Because |
|------------------------|------------|--------------------------------------|
| `spring-boot-starter-data-jpa` (replaces `spring-boot-starter-data-jdbc`) | JPA `@Entity` + `JpaRepository` pattern explicitly requested; eliminates all raw SQL in gateway implementations | Spring Data JDBC `ListCrudRepository`: also eliminates raw JDBC but does not use `@Entity` — out of scope per the user requirement |
| `infrastructure.persistence.entity` package (new abstraction layer) | JPA requires `@Entity` on persistent classes; domain records cannot be `@Entity` (immutable, final, no no-arg constructor); CA-01 forbids `@Entity` on domain types | Annotating domain records: technically impossible for JPA; violates CA-01 |
