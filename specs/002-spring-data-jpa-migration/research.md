# Research: Spring Data JPA Migration

**Feature**: `002-spring-data-jpa-migration`  
**Date**: 2026-04-27  
**Phase**: 0 — Research

## Summary

This document resolves all open questions required before Phase 1 design. All decisions are final and documented with rationale and rejected alternatives.

---

## R-01: JPA Entity Strategy with Clean Architecture

### Decision
Use the **dual-object pattern**: infrastructure-layer JPA entity classes that are separate from domain records. Domain records remain pure Java with zero persistence annotations.

### Rationale
JPA mandates `@Entity` on persistent classes. Java records — which this project uses for all domain models — cannot be JPA entity roots because JPA requires:
1. A no-arg constructor (records have none by default)
2. Mutable state (records are immutable)
3. A non-final class (records are implicitly `final`)

The only clean solution is dedicated entity classes living entirely in `infrastructure.persistence.entity`. The domain gateway interface takes and returns domain records; the gateway implementation maps between them.

### Structure
```
infrastructure/persistence/
├── entity/          # JPA @Entity classes — one per table, package-private
├── repository/      # JpaRepository<Entity, Long> interfaces
├── converter/       # JPA AttributeConverter implementations
└── gateway/         # Implements domain gateways, maps entity ↔ domain record
```

### Mapping strategy
Hand-written `toEntity(domain)` and `toDomain(entity)` private methods inside each gateway implementation. No MapStruct or reflection-based mapper: the codebase's 9 aggregates are small, the mapping is straightforward, and adding MapStruct would be an unjustified dependency per the YAGNI principle.

### Alternatives considered
- **Annotate domain records with `@Entity`**: Rejected. Violates CA-01; records cannot be JPA entity roots anyway.
- **Spring Data JDBC `ListCrudRepository` (no raw JDBC)**: Technically viable and simpler, but explicitly out of scope — the user's requirement names `@Entity` and `@Repository` which are JPA concepts.
- **MapStruct**: Rejected per YAGNI — not worth a new dependency for 9 simple flat mappings.

---

## R-02: JPA Entity Field Associations

### Decision
Use **bare FK columns** (`@Column Long onCallPeriodId`) rather than `@ManyToOne` associations in JPA entities.

### Rationale
Domain records use flat FK fields (`Long onCallPeriodId`), not nested objects. Introducing `@ManyToOne` would create eager/lazy loading complexity with no benefit for this single-user, small-data application. The gateway implementations perform any necessary joins explicitly (or via separate repository calls), exactly as the current JDBC gateways do.

### Alternatives considered
- **`@ManyToOne` associations**: Rejected. Adds lazy-loading risk (`LazyInitializationException`), requires `FetchType.EAGER` workarounds, and introduces Hibernate session state management with no benefit.

---

## R-03: Timestamp Storage and `Instant` Mapping

### Decision
JPA entity timestamp fields use **`Instant`** directly (same as the domain record fields). `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` is set in `application.yml`. The database schema uses `TIMESTAMP` (without timezone) and all values are stored and read as UTC. The frontend is responsible for timezone-aware display formatting.

### Rationale
The backend is timezone-agnostic: `Instant` represents a UTC point in time. Using `Instant` in both domain records and JPA entities eliminates all `LocalDateTime ↔ Instant` conversion in mappers — timestamp fields are a direct pass-through. With `hibernate.jdbc.time_zone=UTC`, Hibernate 7 maps `Instant` to `TIMESTAMP` columns using UTC consistently regardless of the JVM's default timezone.

### Alternatives considered
- **`LocalDateTime` in entity + `ZoneOffset.UTC` conversion in mapper**: More explicit about the UTC assumption in code, but adds boilerplate and splits the type contract across two representations. Rejected in favour of `Instant` end-to-end.
- **`TIMESTAMPTZ` columns in PostgreSQL**: More correct at the DB level but requires a schema migration (adding a new Flyway script). The `TIMESTAMP` + UTC-pinned Hibernate approach is equally safe for a single-timezone application and avoids touching the schema.

### Hibernate `@CreationTimestamp` / `@UpdateTimestamp`
Used for `createdAt` and `updatedAt` entity fields (Hibernate-specific annotations, acceptable in infrastructure entities). Both annotations work with `Instant` in Hibernate 7 and set the field automatically at persist/merge time, so the mapper does not need to populate these fields for new records.

---

## R-04: Enum Column Mapping

### Decision
All enum fields in JPA entities use `@Enumerated(EnumType.STRING)`. No additional converter needed.

### Rationale
All enums are stored as VARCHAR in the schema with exact name-match values (`'INTERNAL'`, `'EXTERNAL'`, `'DARK'`, etc.). `@Enumerated(STRING)` maps directly to the column without any custom converter. Any typo or mismatch would have been caught by the existing data, so the risk of misalignment is zero.

### Enums affected
| Entity | Field | DB column | Values |
|--------|-------|-----------|--------|
| `EngineerProfileJpaEntity` | `employeeType` | VARCHAR(20) | `INTERNAL`, `EXTERNAL` |
| `CompensationRateJpaEntity` | `employeeType` | VARCHAR(20) | `INTERNAL`, `EXTERNAL` |
| `CompensationRateJpaEntity` | `rateCategory` | VARCHAR(40) | `ONCALL_WEEKDAY_SATURDAY`, etc. |
| `UserPreferencesJpaEntity` | `colorScheme` | VARCHAR(10) | `DARK`, `LIGHT`, `AUTO` |
| `UserPreferencesJpaEntity` | `onboardingStep` | VARCHAR(30) | `PROFILE`, etc. |
| `OnCallDayEntryJpaEntity` | `rateType` | VARCHAR(25) | `WEEKDAY_SATURDAY`, `SUNDAY_HOLIDAY` |

---

## R-05: `Set<DayOfWeek>` Attribute Converter

### Decision
Replace `WorkingDaysConverter` (Spring Data JDBC converter) with a JPA `AttributeConverter<Set<DayOfWeek>, String>` using the same comma-separated string format (`"MONDAY,TUESDAY,…"`) to maintain backward compatibility with existing data.

### Rationale
The existing `working_days` column uses `VARCHAR(100)` with comma-separated day names. Switching to a bitmask integer would require a schema migration. Preserving the format means zero data migration risk.

### Implementation sketch
```java
// infrastructure/persistence/converter/DayOfWeekSetConverter.java
@Converter(autoApply = true)
public class DayOfWeekSetConverter implements AttributeConverter<Set<DayOfWeek>, String> {
    @Override
    public String convertToDatabaseColumn(Set<DayOfWeek> days) { … }
    @Override
    public Set<DayOfWeek> convertToEntityAttribute(String csv) { … }
}
```

`autoApply = true` applies globally to all `Set<DayOfWeek>` fields, eliminating the need for per-field `@Convert` annotations. The old `WorkingDaysConverter` class (Spring Data JDBC) is deleted.

---

## R-06: DDL Strategy

### Decision
`spring.jpa.hibernate.ddl-auto=validate`. Flyway exclusively owns schema creation and evolution.

### Rationale
The project already uses Flyway for schema versioning. Hibernate's `validate` mode checks entity-to-schema compatibility at startup and fails fast with a precise `SchemaManagementException` if there is any mismatch. It never modifies the schema.

### `application.yml` changes
```yaml
# Remove
spring.data.jdbc.dialect: postgresql

# Add
spring.jpa:
  hibernate:
    ddl-auto: validate
  properties:
    hibernate:
      jdbc:
        time_zone: UTC
      format_sql: true
  show-sql: false
```

---

## R-07: pom.xml Changes

### Decision
Replace `spring-boot-starter-data-jdbc` with `spring-boot-starter-data-jpa`. All other dependencies remain unchanged.

### Changes
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

`spring-boot-starter-flyway` and `flyway-database-postgresql` remain (already present). `spring-boot-starter-test` provides JUnit 5, AssertJ, and Mockito for tests. A `@DataJpaTest` slice is available from `spring-boot-starter-test` in Boot 4 — no additional test starter is needed.

---

## R-08: Integration Test Pattern

### Decision
Use `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + Testcontainers `@ServiceConnection` PostgreSQL container. One shared base class per test class hierarchy. Each of the 9 gateway implementations gets its own integration test class that tests through the **gateway interface** (not the JPA repository directly), preserving the port-and-adapter boundary.

### Pattern
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
    void setUp() { gateway = new JpaOnCallPeriodGateway(repository); }

    @Test
    void savesAndRetrievesOnCallPeriod() { … }
}
```

`@DataJpaTest` loads only the JPA slice (entities, repositories, converters) plus Flyway migrations (which run against the Testcontainers PostgreSQL). `replace = NONE` prevents Spring Boot from substituting an H2 database.

### Alternatives considered
- **`@SpringBootTest` full context**: Heavier, loads the entire application. Not needed — `@DataJpaTest` slice is sufficient and faster.
- **Shared static container base class**: Viable for performance, but `@ServiceConnection` on `@Container` is simpler and equally effective for 9 test classes.

---

## R-09: Hibernate 7 / Spring Boot 4 Compatibility

### Key notes for implementation
- Spring Boot 4.0 ships Hibernate 7.1; upgrade to 4.0.1+ for Hibernate 7.2 (Java 25 certified). Boot 4.0.0 with Hibernate 7.1 still compiles and runs on Java 25 but is not officially certified — acceptable since the project is a local tool.
- `javax.persistence.*` → `jakarta.persistence.*`: already handled by Boot 4.x BOM.
- No version-specific PostgreSQL dialect needed: Boot auto-detects `PostgreSQLDialect` from the JDBC URL.
- `@MockBean` / `@SpyBean` are removed in Boot 4: use `@MockitoBean` / `@MockitoSpyBean` in new tests (the existing tests use neither — no change needed in existing test files).
- `@SpringBootTest` no longer auto-provides `MockMvc`: add `@AutoConfigureMockMvc` if needed (existing controller tests already use `@WebMvcTest`; not affected by this migration).
