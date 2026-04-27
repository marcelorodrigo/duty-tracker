# Research: Spring Data JPA Migration

**Feature**: 002-spring-data-jpa-migration
**Date**: 2026-04-27
**Status**: Complete — all NEEDS CLARIFICATION resolved

---

## R-001: Maven Dependency for Spring Data JPA (Spring Boot 4.x)

**Decision**: Replace `spring-boot-starter-data-jdbc` with `spring-boot-starter-data-jpa`.

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

<!-- ADD (test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

**Rationale**: `spring-boot-starter-data-jpa` is the canonical starter for Hibernate/JPA with Spring Boot. It transitively brings Hibernate 7.x and Spring Data JPA 4.x — no explicit Hibernate version needed. `spring-boot-testcontainers` is added for `@ServiceConnection` support in integration tests.

**Alternatives considered**: Adding raw Hibernate artifacts without the starter — rejected, Boot starter manages version alignment automatically.

---

## R-002: `Set<DayOfWeek>` JPA Attribute Converter

**Decision**: Create `DayOfWeekSetConverter` in `infrastructure.persistence.converter` as a JPA `@Converter(autoApply = true)`:

```java
@Converter(autoApply = true)
class DayOfWeekSetConverter implements AttributeConverter<Set<DayOfWeek>, String> {
    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> attribute) {
        if (attribute == null || attribute.isEmpty()) return "";
        return attribute.stream().map(DayOfWeek::name).sorted().collect(Collectors.joining(","));
    }

    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return EnumSet.noneOf(DayOfWeek.class);
        return Arrays.stream(dbData.split(","))
                .map(String::trim).map(DayOfWeek::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }
}
```

**Rationale**: `autoApply = true` means Hibernate auto-applies the converter to every `Set<DayOfWeek>` field across all entities — no per-field `@Convert` annotation needed. This is the canonical JPA pattern for custom type mappings. `EnumSet` preserves natural `DayOfWeek` ordering.

**Action required**: Delete the existing `WorkingDaysConverter.java` (Spring Data JDBC `@WritingConverter`/`@ReadingConverter`) and `JdbcConfig.java` after the JPA migration is complete.

**Alternatives considered**: Using `@Enumerated(EnumType.STRING)` on a `List<DayOfWeek>` — rejected; requires an element collection table, which conflicts with the existing single-column Flyway schema (`working_days VARCHAR(100)`).

---

## R-003: `LocalTime` Mapping

**Decision**: No custom converter needed. Hibernate 7 natively maps `LocalTime` → SQL `TIME` column.

**Rationale**: Hibernate 7 (Jakarta Persistence 3.2) has built-in support for all `java.time` types including `LocalTime`. The Flyway schema uses plain `TIME` columns (not `TIME WITH TIME ZONE`), so no extra configuration is required.

**Affected fields**: `CompensationRate.timeFrom`, `CompensationRate.timeTo`, `Incident.startTime`, `Incident.endTime`, `OvertimeEntry.timeFrom`, `OvertimeEntry.timeTo`, `EngineerProfile.workStartTime`, `EngineerProfile.workEndTime`.

---

## R-004: `Instant` Timezone Configuration

**Decision**: Add `spring.jpa.properties.hibernate.jdbc.time_zone: UTC` to `application.yml`. Pass `Instant` fields through directly — no converter needed.

**Rationale**: Without the UTC timezone hint, Hibernate may convert `Instant` values through the JVM's default timezone when reading/writing `TIMESTAMP` columns, causing subtle off-by-one hour bugs in non-UTC environments. Setting the property globally ensures correct round-trips.

**Affected fields**: `EngineerProfile.createdAt`, `OnCallPeriod.createdAt`, `Incident.createdAt`, `RegistrationSummary.createdAt`, `RegistrationSummary.updatedAt`.

---

## R-005: `@DataJpaTest` + Testcontainers Pattern (Spring Boot 4.x)

**Decision**: Use `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@Testcontainers` + `@ServiceConnection` on a `static PostgreSQLContainer<?>` field.

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class JpaXxxGatewayTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired XxxJpaRepository repository;
    private JpaXxxGateway gateway;

    @BeforeEach void setUp() { gateway = new JpaXxxGateway(repository); }
}
```

**Rationale**:
- `@DataJpaTest` loads only the persistence slice (entities, repositories, Flyway) — no full application context overhead.
- `replace = NONE` prevents Boot from substituting an H2 in-memory DB (the default), ensuring Flyway scripts and PostgreSQL-specific types are exercised.
- `@ServiceConnection` auto-registers the container's JDBC URL, username, and password as Spring datasource properties — no manual `@DynamicPropertySource` needed.
- The `static` field ensures the container is started once per class and reused across test methods.
- `@DataJpaTest` applies `@Transactional` on every test method by default — no `@AfterEach` cleanup required.

**Imports**:
| Annotation/Type | Import |
|---|---|
| `@DataJpaTest` | `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` |
| `@AutoConfigureTestDatabase` | `org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase` |
| `@ServiceConnection` | `org.springframework.boot.testcontainers.service.connection.ServiceConnection` |
| `@Container` | `org.testcontainers.junit.jupiter.Container` |
| `@Testcontainers` | `org.testcontainers.junit.jupiter.Testcontainers` |
| `PostgreSQLContainer` | `org.testcontainers.containers.PostgreSQLContainer` |

**Alternatives considered**: `@SpringBootTest` with Testcontainers — rejected; loads the full application context including web layer, making tests slower and requiring more configuration.

---

## R-006: `@WebMvcTest` Import (Spring Boot 4.x)

**Decision**: The import in Spring Boot 4.x is still `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`.

**Rationale**: Boot 4.x reorganized some internal packages, but `@WebMvcTest` remains in the same `org.springframework.boot.test.autoconfigure.web.servlet` package. The existing controller tests already use this annotation; no changes needed to those test classes.

**Note**: The `spring-boot-starter-webmvc-test` artifact (already in `pom.xml`) provides this dependency in Spring Boot 4.x.

---

## R-007: FK Relationships — `@ManyToOne` vs. Storing Raw `Long` ID

**Decision**: Use `@ManyToOne(fetch = FetchType.LAZY)` object references on entity fields that correspond to foreign keys. The domain record still stores a plain `Long` FK ID. The gateway `toDomain()` helper extracts the ID from the entity relationship.

**Rationale**: The Flyway schema defines FK constraints between tables. JPA `@ManyToOne` is the idiomatic representation. `FetchType.LAZY` prevents N+1 query issues — FK-referenced objects are not loaded until accessed. For nullable FKs (e.g., `incident.on_call_period_id` uses `SET NULL` on cascade), the `@ManyToOne` field is declared as `@ManyToOne(fetch = LAZY) @JoinColumn(nullable = true)`.

**FK relationships in the schema**:
| Child entity | Parent | Nullable | On Delete |
|---|---|---|---|
| `HolidayOverrideEntity` → `OnCallPeriodEntity` | `on_call_period_id` | No | CASCADE |
| `OnCallDayEntryEntity` → `OnCallPeriodEntity` | `on_call_period_id` | No | CASCADE |
| `IncidentEntity` → `OnCallPeriodEntity` | `on_call_period_id` | **Yes** | SET NULL |
| `OvertimeEntryEntity` → `IncidentEntity` | `incident_id` | No | CASCADE |

---

## R-008: `save()` Return Value — DB-Generated Timestamps

**Decision**: After `repository.save(entity)`, always call `repository.findById(savedEntity.getId()).orElseThrow()` and convert the result to the domain record.

**Rationale**: JPA `save()` returns the managed entity after the flush, but `TIMESTAMP` columns with `DEFAULT now()` at the database level are only populated after a real INSERT (which happens on flush). Re-fetching via `findById` guarantees the returned domain record contains the actual DB-assigned `created_at` and `updated_at` timestamps. This is consistent with the clarification in the spec (session 2026-04-27, Q1).

**Pattern per gateway**:
```java
@Override
public XxxDomain save(XxxDomain domain) {
    XxxEntity entity = toEntity(domain);
    XxxEntity saved = repository.save(entity);
    return toDomain(repository.findById(saved.getId()).orElseThrow());
}
```

**Alternatives considered**: Calling `entityManager.refresh(saved)` — rejected; requires injecting `EntityManager` directly, adding unnecessary dependency to each gateway.

---

## R-009: Package Visibility for JPA Entity Classes

**Decision**: JPA `@Entity` classes (in `infrastructure.persistence.entity`) are declared **package-private** (no `public` modifier on the class declaration).

**Rationale**: The AGENTS.md migration conventions explicitly state entities should be package-private. This prevents other layers from depending on entity types directly — only gateway implementations in the `infrastructure.persistence.gateway` package can reference them. Domain and application layers have no visibility into entity types.

**Exception**: Enum fields on entities (e.g., `EmployeeType`, `ColorScheme`) are domain enums and remain `public` since they are defined in the domain layer.

---

## R-010: `application.yml` Changes

**Before (current)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}
    username: ${DB_USER:dutytracker}
    password: ${DB_PASSWORD:dutytracker}
  data:
    jdbc:
      dialect: postgresql
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
```

**After (JPA migration)**:
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
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
```

**Changes**: Remove `spring.data.jdbc.dialect`; add `spring.jpa` block with `ddl-auto: validate`, `show-sql: false`, and `hibernate.jdbc.time_zone: UTC`.

---

## R-011: `JdbcConfig.java` — Deletion

**Decision**: Delete `JdbcConfig.java` entirely after migration.

**Rationale**: `JdbcConfig` only registers `JdbcCustomConversions` (for the JDBC-era `WorkingDaysConverter`). Once Spring Data JDBC is removed and replaced with JPA, this class has no purpose. The JPA `DayOfWeekSetConverter` with `autoApply = true` is self-registering via Hibernate's auto-detection of `AttributeConverter` beans on the classpath.

---

## Summary: All NEEDS CLARIFICATION Resolved

| Item | Decision |
|------|----------|
| JPA starter artifact | `spring-boot-starter-data-jpa` (replaces `data-jdbc`) |
| `Set<DayOfWeek>` converter | `DayOfWeekSetConverter` with `@Converter(autoApply=true)` |
| `LocalTime` mapping | Native Hibernate 7 — no converter needed |
| `Instant` mapping | Direct pass-through + `hibernate.jdbc.time_zone=UTC` |
| Test pattern | `@DataJpaTest` + `replace=NONE` + `@ServiceConnection` |
| FK representation | `@ManyToOne(fetch=LAZY)` on entity; plain `Long` id in domain |
| `save()` return | Re-fetch via `findById` after `save()` |
| Entity visibility | Package-private class declarations |
| `application.yml` | Remove `data.jdbc`; add `jpa` block |
| `JdbcConfig.java` | Delete after migration |
