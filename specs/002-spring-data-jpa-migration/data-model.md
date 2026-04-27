# Data Model: Spring Data JPA Migration

**Feature**: `002-spring-data-jpa-migration`  
**Date**: 2026-04-27  
**Phase**: 1 — Design

## Overview

This document defines the complete JPA persistence layer introduced by the migration. Domain model records (in `domain.model`) are **unchanged**. All new types live in `infrastructure.persistence.*`.

### Timestamp strategy
All timestamp fields in JPA entities use **`Instant`** — the same type as in domain records. `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` ensures Hibernate maps `Instant ↔ TIMESTAMP` using UTC. Timestamp fields are a direct pass-through in mappers (no `LocalDateTime` conversion). The frontend handles all timezone-aware display formatting.

---

## JPA Entity Classes

All entity classes reside in `com.dutytracker.infrastructure.persistence.entity`. They are **package-private** (no `public` modifier) — only the gateway implementations in the same `infrastructure.persistence` subtree need to access them. Each entity is a mutable class with a no-arg constructor (required by JPA).

### `OnCallPeriodJpaEntity`

Table: `on_call_period`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `startDateTime` | `start_date_time` | `TIMESTAMP` / `LocalDateTime` | |
| `endDateTime` | `end_date_time` | `TIMESTAMP` / `LocalDateTime` | |
| `createdAt` | `created_at` | `TIMESTAMP` / `Instant` | `@CreationTimestamp`, `@Column(updatable=false)` |

Domain record: `OnCallPeriod(Long id, LocalDateTime startDateTime, LocalDateTime endDateTime, Instant createdAt)`  
Mapping: `createdAt` — direct pass-through (`Instant` in both domain record and entity).

---

### `EngineerProfileJpaEntity`

Table: `engineer_profile`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `employeeType` | `employee_type` | `VARCHAR(20)` | `@Enumerated(EnumType.STRING)` |
| `workingDays` | `working_days` | `VARCHAR(100)` | `@Convert(DayOfWeekSetConverter)` auto-applied |
| `workStartTime` | `work_start_time` | `TIME` / `LocalTime` | |
| `workEndTime` | `work_end_time` | `TIME` / `LocalTime` | |
| `createdAt` | `created_at` | `TIMESTAMP` / `Instant` | `@CreationTimestamp`, `@Column(updatable=false)` |

Domain record: `EngineerProfile(Long id, EmployeeType employeeType, Set<DayOfWeek> workingDays, LocalTime workStartTime, LocalTime workEndTime, Instant createdAt)`

---

### `UserPreferencesJpaEntity`

Table: `user_preferences`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `colorScheme` | `color_scheme` | `VARCHAR(10)` | `@Enumerated(EnumType.STRING)` |
| `onboardingStep` | `onboarding_step` | `VARCHAR(30)` | `@Enumerated(EnumType.STRING)` |

Domain record: `UserPreferences(Long id, ColorScheme colorScheme, OnboardingStep onboardingStep)`

---

### `CompensationRateJpaEntity`

Table: `compensation_rate`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `employeeType` | `employee_type` | `VARCHAR(20)` | `@Enumerated(EnumType.STRING)` |
| `rateCategory` | `rate_category` | `VARCHAR(40)` | `@Enumerated(EnumType.STRING)` |
| `label` | `label` | `VARCHAR(100)` | |
| `timeFrom` | `time_from` | `TIME` / `LocalTime` | nullable |
| `timeTo` | `time_to` | `TIME` / `LocalTime` | nullable |
| `percentage` | `percentage` | `NUMERIC(10,4)` / `BigDecimal` | |

Domain record: `CompensationRate(Long id, EmployeeType employeeType, RateCategory rateCategory, String label, LocalTime timeFrom, LocalTime timeTo, BigDecimal percentage)`

---

### `OnCallDayEntryJpaEntity`

Table: `on_call_day_entry`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `onCallPeriodId` | `on_call_period_id` | `BIGINT` / `Long` | `@Column(name="on_call_period_id")`, FK |
| `date` | `date` | `DATE` / `LocalDate` | |
| `hours` | `hours` | `NUMERIC(10,4)` / `BigDecimal` | |
| `rateType` | `rate_type` | `VARCHAR(25)` | `@Enumerated(EnumType.STRING)` |
| `capped` | `capped` | `BOOLEAN` | |
| `timeForTimeFlag` | `time_for_time_flag` | `BOOLEAN` | |
| `manualOverride` | `manual_override` | `BOOLEAN` | |

Domain record: `OnCallDayEntry(Long id, Long onCallPeriodId, LocalDate date, BigDecimal hours, StandbyRateType rateType, boolean capped, boolean timeForTimeFlag, boolean manualOverride)`

---

### `IncidentJpaEntity`

Table: `incident`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `onCallPeriodId` | `on_call_period_id` | `BIGINT` / `Long` | `@Column(name="on_call_period_id")`, nullable FK |
| `date` | `date` | `DATE` / `LocalDate` | |
| `startTime` | `start_time` | `TIME` / `LocalTime` | |
| `endTime` | `end_time` | `TIME` / `LocalTime` | |
| `createdAt` | `created_at` | `TIMESTAMP` / `Instant` | `@CreationTimestamp`, `@Column(updatable=false)` |

Domain record: `Incident(Long id, Long onCallPeriodId, LocalDate date, LocalTime startTime, LocalTime endTime, Instant createdAt)`

---

### `OvertimeEntryJpaEntity`

Table: `overtime_entry`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `incidentId` | `incident_id` | `BIGINT` / `Long` | `@Column(name="incident_id")`, FK |
| `overtimeHours` | `overtime_hours` | `NUMERIC(10,4)` / `BigDecimal` | |
| `allowanceHours` | `allowance_hours` | `NUMERIC(10,4)` / `BigDecimal` | nullable |
| `allowancePercentage` | `allowance_percentage` | `NUMERIC(10,4)` / `BigDecimal` | nullable |
| `timeFrom` | `time_from` | `TIME` / `LocalTime` | nullable |
| `timeTo` | `time_to` | `TIME` / `LocalTime` | nullable |
| `isAllowanceEntry` | `is_allowance_entry` | `BOOLEAN` | |
| `manualOverride` | `manual_override` | `BOOLEAN` | |

Domain record: `OvertimeEntry(Long id, Long incidentId, BigDecimal overtimeHours, BigDecimal allowanceHours, BigDecimal allowancePercentage, LocalTime timeFrom, LocalTime timeTo, boolean isAllowanceEntry, boolean manualOverride)`

---

### `HolidayOverrideJpaEntity`

Table: `holiday_override`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `onCallPeriodId` | `on_call_period_id` | `BIGINT` / `Long` | `@Column(name="on_call_period_id")`, FK |
| `date` | `date` | `DATE` / `LocalDate` | |

Domain record: `HolidayOverride(Long id, Long onCallPeriodId, LocalDate date)`

---

### `RegistrationSummaryJpaEntity`

Table: `registration_summary`

| JPA field | DB column | Type | Notes |
|-----------|-----------|------|-------|
| `id` | `id` | `BIGSERIAL` / `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `label` | `label` | `VARCHAR(200)` | |
| `periodStart` | `period_start` | `DATE` / `LocalDate` | |
| `periodEnd` | `period_end` | `DATE` / `LocalDate` | |
| `createdAt` | `created_at` | `TIMESTAMP` / `Instant` | `@CreationTimestamp`, `@Column(updatable=false)` |
| `updatedAt` | `updated_at` | `TIMESTAMP` / `Instant` | `@UpdateTimestamp` |

Domain record: `RegistrationSummary(Long id, String label, LocalDate periodStart, LocalDate periodEnd, Instant createdAt, Instant updatedAt)`

---

## JPA Repository Interfaces

All repositories reside in `com.dutytracker.infrastructure.persistence.repository` and extend `JpaRepository<Entity, Long>`. Derived query methods are listed where non-standard CRUD is needed.

| Interface | Entity | Additional methods |
|-----------|--------|--------------------|
| `OnCallPeriodJpaRepository` | `OnCallPeriodJpaEntity` | `List<…> findAllByOrderByStartDateTimeDesc()` |
| `EngineerProfileJpaRepository` | `EngineerProfileJpaEntity` | `Optional<…> findFirstBy()` |
| `UserPreferencesJpaRepository` | `UserPreferencesJpaEntity` | `Optional<…> findFirstBy()` |
| `CompensationRateJpaRepository` | `CompensationRateJpaEntity` | `List<…> findByEmployeeType(EmployeeType)` |
| `OnCallDayEntryJpaRepository` | `OnCallDayEntryJpaEntity` | `List<…> findByOnCallPeriodIdOrderByDateAsc(Long)`, `void deleteByOnCallPeriodId(Long)` |
| `IncidentJpaRepository` | `IncidentJpaEntity` | `List<…> findByOnCallPeriodId(Long)`, `List<…> findAllByOrderByDateAsc()` |
| `OvertimeEntryJpaRepository` | `OvertimeEntryJpaEntity` | `List<…> findByIncidentId(Long)`, `void deleteByIncidentId(Long)` |
| `HolidayOverrideJpaRepository` | `HolidayOverrideJpaEntity` | `List<…> findByOnCallPeriodId(Long)`, `Optional<…> findByOnCallPeriodIdAndDate(Long, LocalDate)` |
| `RegistrationSummaryJpaRepository` | `RegistrationSummaryJpaEntity` | `List<…> findAllByOrderByPeriodStartDesc()`, `boolean existsBy()` |

---

## Attribute Converters

Resides in `com.dutytracker.infrastructure.persistence.converter`.

### `DayOfWeekSetConverter`
- Implements `AttributeConverter<Set<DayOfWeek>, String>`
- `@Converter(autoApply = true)` — applies to all `Set<DayOfWeek>` fields automatically
- Database column type: `VARCHAR(100)`
- Format: comma-separated day names, e.g. `"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"` (same as existing data)
- Replaces: `WorkingDaysConverter` (Spring Data JDBC `@WritingConverter`/`@ReadingConverter`) — **deleted**

---

## Gateway Implementations (new)

Resides in `com.dutytracker.infrastructure.persistence.gateway`. Each class implements the corresponding domain gateway interface and delegates to a JPA repository.

| New class | Replaces | Implements |
|-----------|----------|------------|
| `JpaOnCallPeriodGateway` | `JdbcOnCallPeriodGateway` | `OnCallPeriodGateway` |
| `JpaEngineerProfileGateway` | `JdbcEngineerProfileGateway` | `EngineerProfileGateway` |
| `JpaUserPreferencesGateway` | `JdbcUserPreferencesGateway` | `UserPreferencesGateway` |
| `JpaCompensationRateGateway` | `JdbcCompensationRateGateway` | `CompensationRateGateway` |
| `JpaOnCallDayEntryGateway` | `JdbcOnCallDayEntryGateway` | `OnCallDayEntryGateway` |
| `JpaIncidentGateway` | `JdbcIncidentGateway` | `IncidentGateway` |
| `JpaOvertimeEntryGateway` | `JdbcOvertimeEntryGateway` | `OvertimeEntryGateway` |
| `JpaHolidayOverrideGateway` | `JdbcHolidayOverrideGateway` | `HolidayOverrideGateway` |
| `JpaRegistrationSummaryGateway` | `JdbcRegistrationSummaryGateway` | `RegistrationSummaryGateway` |

### Naming convention for gateway methods
Each `JpaXxxGateway`:
- Constructor-injects its `JpaXxxRepository` (single dependency, no `NamedParameterJdbcTemplate`)
- Contains private `toEntity(domain)` and `toDomain(entity)` methods
- Contains private `toDomainList(List<entity>)` convenience method where needed
- All `Instant` timestamp fields: direct pass-through — no `ZoneOffset` conversion needed
- `@Transactional` on `deleteByXxxId` derived queries in repository (Spring Data JPA requires `@Transactional` on custom delete methods)

---

## Files Deleted
- `infrastructure/persistence/gateway/JdbcOnCallPeriodGateway.java`
- `infrastructure/persistence/gateway/JdbcEngineerProfileGateway.java`
- `infrastructure/persistence/gateway/JdbcUserPreferencesGateway.java`
- `infrastructure/persistence/gateway/JdbcCompensationRateGateway.java`
- `infrastructure/persistence/gateway/JdbcOnCallDayEntryGateway.java`
- `infrastructure/persistence/gateway/JdbcIncidentGateway.java`
- `infrastructure/persistence/gateway/JdbcOvertimeEntryGateway.java`
- `infrastructure/persistence/gateway/JdbcHolidayOverrideGateway.java`
- `infrastructure/persistence/gateway/JdbcRegistrationSummaryGateway.java`
- `infrastructure/persistence/converter/WorkingDaysConverter.java`

---

## Configuration Changes

### `pom.xml`
```xml
<!-- REMOVE -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<!-- ADD -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}
    username: ${DB_USER:dutytracker}
    password: ${DB_PASSWORD:dutytracker}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
```

Note: `spring.data.jdbc.dialect: postgresql` is **removed** (Spring Data JDBC config, no longer needed).

---

## Integration Test Pattern

### Base setup (per test class, using `@ServiceConnection`)
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaOnCallPeriodGatewayTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired OnCallPeriodJpaRepository repository;

    private JpaOnCallPeriodGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new JpaOnCallPeriodGateway(repository);
    }
    // tests exercise OnCallPeriodGateway interface, not repository directly
}
```

### Required test classes (one per gateway)
| Test class | Location |
|------------|----------|
| `JpaOnCallPeriodGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaEngineerProfileGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaUserPreferencesGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaCompensationRateGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaOnCallDayEntryGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaIncidentGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaOvertimeEntryGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaHolidayOverrideGatewayTest` | `infrastructure/persistence/gateway/` |
| `JpaRegistrationSummaryGatewayTest` | `infrastructure/persistence/gateway/` |

### Minimum test coverage per gateway test class
1. `save()` a new record → returned record has non-null `id`
2. `findById()` the saved record → all fields match
3. `deleteById()` → subsequent `findById()` returns `Optional.empty()`
4. Gateway-specific finder (e.g., `findByOnCallPeriodId`, `findByEmployeeType`) returns correct subset
5. Custom delete (e.g., `deleteByOnCallPeriodId`) removes the correct rows
