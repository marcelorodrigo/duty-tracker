# Data Model: Spring Data JPA Migration

**Feature**: 002-spring-data-jpa-migration
**Date**: 2026-04-27

This document defines the nine JPA entity classes, their Spring Data repository interfaces, the `DayOfWeekSetConverter`, and the gateway mapping patterns. No changes to domain model records.

---

## Layer Separation

```
domain.model          ← Pure Java records — ZERO persistence annotations (unchanged)
domain.gateway        ← Interfaces — ZERO persistence annotations (unchanged)
infrastructure.persistence.converter  ← JPA @Converter (new: DayOfWeekSetConverter)
infrastructure.persistence.entity     ← JPA @Entity classes (new package, 9 classes)
infrastructure.persistence.repository ← JpaRepository interfaces (new package, 9 interfaces)
infrastructure.persistence.gateway   ← JPA gateway implementations (9 new, 9 old deleted)
```

---

## Attribute Converter

### `DayOfWeekSetConverter`

**Package**: `com.dutytracker.infrastructure.persistence.converter`
**File**: `DayOfWeekSetConverter.java`

```java
package com.dutytracker.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = true)
class DayOfWeekSetConverter implements AttributeConverter<Set<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream()
                .map(DayOfWeek::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return EnumSet.noneOf(DayOfWeek.class);
        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }
}
```

**Notes**: `autoApply = true` — no per-field `@Convert` annotation needed on `EngineerProfileEntity.workingDays`. Replace/delete `WorkingDaysConverter.java` after migration.

---

## JPA Entity Classes

All entities are **package-private** (no `public` on the class declaration). All live in `com.dutytracker.infrastructure.persistence.entity`.

### `EngineerProfileEntity`

Maps to: `engineer_profile`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `employee_type` | `employeeType` | `EmployeeType` | `@Enumerated(STRING)` |
| `working_days` | `workingDays` | `Set<DayOfWeek>` | `DayOfWeekSetConverter` auto-applied |
| `work_start_time` | `workStartTime` | `LocalTime` | native Hibernate 7 |
| `work_end_time` | `workEndTime` | `LocalTime` | native Hibernate 7 |
| `created_at` | `createdAt` | `Instant` | UTC timezone via config |

**Domain mapping**: `EngineerProfile(id, employeeType, workingDays, workStartTime, workEndTime, createdAt)`

---

### `UserPreferencesEntity`

Maps to: `user_preferences`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `color_scheme` | `colorScheme` | `ColorScheme` | `@Enumerated(STRING)` |
| `onboarding_step` | `onboardingStep` | `OnboardingStep` | `@Enumerated(STRING)` |

**Domain mapping**: `UserPreferences(id, colorScheme, onboardingStep)`

---

### `CompensationRateEntity`

Maps to: `compensation_rate`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `employee_type` | `employeeType` | `EmployeeType` | `@Enumerated(STRING)` |
| `rate_category` | `rateCategory` | `RateCategory` | `@Enumerated(STRING)` |
| `label` | `label` | `String` | |
| `time_from` | `timeFrom` | `LocalTime` | nullable; native Hibernate 7 |
| `time_to` | `timeTo` | `LocalTime` | nullable; native Hibernate 7 |
| `percentage` | `percentage` | `BigDecimal` | |

**Schema constraint**: `UNIQUE(employee_type, rate_category, time_from, time_to)` — enforced by DB, not by JPA annotations.
**Domain mapping**: `CompensationRate(id, employeeType, rateCategory, label, timeFrom, timeTo, percentage)`

---

### `OnCallPeriodEntity`

Maps to: `on_call_period`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `start_date_time` | `startDateTime` | `LocalDateTime` | native Hibernate 7 |
| `end_date_time` | `endDateTime` | `LocalDateTime` | native Hibernate 7 |
| `created_at` | `createdAt` | `Instant` | UTC timezone via config |

**Domain mapping**: `OnCallPeriod(id, startDateTime, endDateTime, createdAt)`

---

### `HolidayOverrideEntity`

Maps to: `holiday_override`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `on_call_period_id` | `onCallPeriod` | `OnCallPeriodEntity` | `@ManyToOne(fetch=LAZY) @JoinColumn(nullable=false)` |
| `date` | `date` | `LocalDate` | |

**FK**: `on_call_period_id` → `on_call_period(id)` ON DELETE CASCADE (not nullable)
**Domain mapping**: `HolidayOverride(id, onCallPeriodId, date)` — `toDomain()` calls `entity.getOnCallPeriod().getId()`

---

### `OnCallDayEntryEntity`

Maps to: `on_call_day_entry`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `on_call_period_id` | `onCallPeriod` | `OnCallPeriodEntity` | `@ManyToOne(fetch=LAZY) @JoinColumn(nullable=false)` |
| `date` | `date` | `LocalDate` | |
| `hours` | `hours` | `BigDecimal` | |
| `rate_type` | `rateType` | `StandbyRateType` | `@Enumerated(STRING)` |
| `capped` | `capped` | `boolean` | |
| `time_for_time_flag` | `timeForTimeFlag` | `boolean` | |
| `manual_override` | `manualOverride` | `boolean` | |

**FK**: `on_call_period_id` → `on_call_period(id)` ON DELETE CASCADE (not nullable)
**Domain mapping**: `OnCallDayEntry(id, onCallPeriodId, date, hours, rateType, capped, timeForTimeFlag, manualOverride)`

**Repository — custom query methods**:
```java
List<OnCallDayEntryEntity> findByOnCallPeriodId(Long onCallPeriodId);

@Transactional
void deleteByOnCallPeriod(OnCallPeriodEntity onCallPeriod);
```

---

### `IncidentEntity`

Maps to: `incident`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `on_call_period_id` | `onCallPeriod` | `OnCallPeriodEntity` | `@ManyToOne(fetch=LAZY) @JoinColumn(nullable=true)` — **nullable** |
| `date` | `date` | `LocalDate` | |
| `start_time` | `startTime` | `LocalTime` | native Hibernate 7 |
| `end_time` | `endTime` | `LocalTime` | native Hibernate 7 |
| `created_at` | `createdAt` | `Instant` | UTC timezone via config |

**FK**: `on_call_period_id` → `on_call_period(id)` ON DELETE SET NULL (**nullable**)
**Domain mapping**: `Incident(id, onCallPeriodId, date, startTime, endTime, createdAt)` — `toDomain()` calls `entity.getOnCallPeriod() == null ? null : entity.getOnCallPeriod().getId()`

**Test note**: `JpaIncidentGatewayTest` MUST include a scenario saving an `Incident` with `onCallPeriodId = null` (SC-003).

---

### `OvertimeEntryEntity`

Maps to: `overtime_entry`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `incident_id` | `incident` | `IncidentEntity` | `@ManyToOne(fetch=LAZY) @JoinColumn(nullable=false)` |
| `overtime_hours` | `overtimeHours` | `BigDecimal` | |
| `allowance_hours` | `allowanceHours` | `BigDecimal` | |
| `allowance_percentage` | `allowancePercentage` | `BigDecimal` | |
| `time_from` | `timeFrom` | `LocalTime` | nullable; native Hibernate 7 |
| `time_to` | `timeTo` | `LocalTime` | nullable; native Hibernate 7 |
| `is_allowance_entry` | `isAllowanceEntry` | `boolean` | |
| `manual_override` | `manualOverride` | `boolean` | |

**FK**: `incident_id` → `incident(id)` ON DELETE CASCADE (not nullable)
**Domain mapping**: `OvertimeEntry(id, incidentId, overtimeHours, allowanceHours, allowancePercentage, timeFrom, timeTo, isAllowanceEntry, manualOverride)`

**Repository — custom query methods**:
```java
List<OvertimeEntryEntity> findByIncidentId(Long incidentId);

@Transactional
void deleteByIncident(IncidentEntity incident);
```

---

### `RegistrationSummaryEntity`

Maps to: `registration_summary`

| Column | Java field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `Long` | `@Id @GeneratedValue(IDENTITY)` |
| `label` | `label` | `String` | |
| `period_start` | `periodStart` | `LocalDate` | |
| `period_end` | `periodEnd` | `LocalDate` | |
| `created_at` | `createdAt` | `Instant` | UTC timezone via config |
| `updated_at` | `updatedAt` | `Instant` | UTC timezone via config |

**Domain mapping**: `RegistrationSummary(id, label, periodStart, periodEnd, createdAt, updatedAt)`

**Special method**: `RegistrationSummaryGateway.existsAny()` — implemented as `repository.count() > 0`.

---

## Spring Data JPA Repositories

All reside in `com.dutytracker.infrastructure.persistence.repository`. Each extends `JpaRepository<XxxEntity, Long>`.

| Interface | Entity | Custom methods |
|---|---|---|
| `EngineerProfileJpaRepository` | `EngineerProfileEntity` | *(none — `find` uses `findAll().stream().findFirst()`)* |
| `UserPreferencesJpaRepository` | `UserPreferencesEntity` | *(none — same pattern as EngineerProfile)* |
| `CompensationRateJpaRepository` | `CompensationRateEntity` | `List<CompensationRateEntity> findByEmployeeType(EmployeeType type)` |
| `OnCallPeriodJpaRepository` | `OnCallPeriodEntity` | *(none — standard CRUD)* |
| `HolidayOverrideJpaRepository` | `HolidayOverrideEntity` | `List<HolidayOverrideEntity> findByOnCallPeriodId(Long id)`; `Optional<HolidayOverrideEntity> findByOnCallPeriodIdAndDate(Long id, LocalDate date)` |
| `OnCallDayEntryJpaRepository` | `OnCallDayEntryEntity` | `List<OnCallDayEntryEntity> findByOnCallPeriodId(Long id)`; `@Transactional void deleteByOnCallPeriod(OnCallPeriodEntity p)` |
| `IncidentJpaRepository` | `IncidentEntity` | `List<IncidentEntity> findByOnCallPeriodId(Long id)` |
| `OvertimeEntryJpaRepository` | `OvertimeEntryEntity` | `List<OvertimeEntryEntity> findByIncidentId(Long id)`; `@Transactional void deleteByIncident(IncidentEntity i)` |
| `RegistrationSummaryJpaRepository` | `RegistrationSummaryEntity` | *(none — `existsAny` via `count()`)* |

---

## Gateway Implementation Pattern

All 9 new gateways follow the same structure:

```java
package com.dutytracker.infrastructure.persistence.gateway;

import com.dutytracker.domain.gateway.XxxGateway;
import com.dutytracker.domain.model.XxxDomain;
import com.dutytracker.infrastructure.persistence.entity.XxxEntity;
import com.dutytracker.infrastructure.persistence.repository.XxxJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class JpaXxxGateway implements XxxGateway {

    private final XxxJpaRepository repository;

    public JpaXxxGateway(XxxJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public XxxDomain save(XxxDomain domain) {
        XxxEntity entity = toEntity(domain);
        XxxEntity saved = repository.save(entity);
        return toDomain(repository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public Optional<XxxDomain> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private XxxEntity toEntity(XxxDomain domain) { /* field mapping */ }
    private XxxDomain toDomain(XxxEntity entity) { /* field mapping */ }
    private List<XxxDomain> toDomainList(List<XxxEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }
}
```

---

## Files to Delete After Migration

| File | Reason |
|------|--------|
| `infrastructure/config/JdbcConfig.java` | Registers JDBC converters — no longer needed |
| `infrastructure/persistence/converter/WorkingDaysConverter.java` | Spring Data JDBC converter — replaced by `DayOfWeekSetConverter` |
| `infrastructure/persistence/gateway/JdbcCompensationRateGateway.java` | Replaced by `JpaCompensationRateGateway` |
| `infrastructure/persistence/gateway/JdbcEngineerProfileGateway.java` | Replaced by `JpaEngineerProfileGateway` |
| `infrastructure/persistence/gateway/JdbcHolidayOverrideGateway.java` | Replaced by `JpaHolidayOverrideGateway` |
| `infrastructure/persistence/gateway/JdbcIncidentGateway.java` | Replaced by `JpaIncidentGateway` |
| `infrastructure/persistence/gateway/JdbcOnCallDayEntryGateway.java` | Replaced by `JpaOnCallDayEntryGateway` |
| `infrastructure/persistence/gateway/JdbcOnCallPeriodGateway.java` | Replaced by `JpaOnCallPeriodGateway` |
| `infrastructure/persistence/gateway/JdbcOvertimeEntryGateway.java` | Replaced by `JpaOvertimeEntryGateway` |
| `infrastructure/persistence/gateway/JdbcRegistrationSummaryGateway.java` | Replaced by `JpaRegistrationSummaryGateway` |
| `infrastructure/persistence/gateway/JdbcUserPreferencesGateway.java` | Replaced by `JpaUserPreferencesGateway` |
