# Quickstart: Spring Data JPA Migration

**Feature**: 002-spring-data-jpa-migration
**Branch**: `feat/spring-jpa`
**Last updated**: 2026-04-27

---

## What this migration does

Replaces the nine `JdbcXxxGateway` implementations (backed by `NamedParameterJdbcTemplate` and raw SQL) with nine `JpaXxxGateway` implementations (backed by Spring Data JPA repositories and Hibernate 7 `@Entity` classes). Domain model records remain unchanged and annotation-free. The Flyway schema is not modified.

---

## Prerequisites

- Docker Desktop running (Testcontainers pulls `postgres:18-alpine` for integration tests)
- Java 25 installed (`java -version`)
- Maven 3.9+ (`mvn -version`)

---

## Step 1: Update `pom.xml`

In `backend/pom.xml`, swap the persistence starter and add the Testcontainers bridge:

```xml
<!-- REMOVE this dependency -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<!-- ADD this dependency -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- ADD this test dependency -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Step 2: Update `application.yml`

In `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}
    username: ${DB_USER:dutytracker}
    password: ${DB_PASSWORD:dutytracker}
  jpa:
    hibernate:
      ddl-auto: validate          # Hibernate validates schema vs Flyway — never creates/alters
    show-sql: false
    properties:
      hibernate:
        jdbc:
          time_zone: UTC          # Ensures Instant round-trips correctly
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080
```

Remove the `spring.data.jdbc.dialect: postgresql` line.

---

## Step 3: Create new infrastructure classes

### Order of implementation

1. `DayOfWeekSetConverter` (converter — no dependencies)
2. Nine `@Entity` classes in `infrastructure.persistence.entity` (depend only on domain enums)
3. Nine `JpaXxxRepository extends JpaRepository<XxxEntity, Long>` interfaces in `infrastructure.persistence.repository`
4. Nine `JpaXxxGateway` implementations in `infrastructure.persistence.gateway`

### Package locations

```
src/main/java/com/dutytracker/infrastructure/persistence/
├── converter/
│   └── DayOfWeekSetConverter.java         ← @Converter(autoApply=true)
├── entity/
│   ├── CompensationRateEntity.java
│   ├── EngineerProfileEntity.java
│   ├── HolidayOverrideEntity.java
│   ├── IncidentEntity.java
│   ├── OnCallDayEntryEntity.java
│   ├── OnCallPeriodEntity.java
│   ├── OvertimeEntryEntity.java
│   ├── RegistrationSummaryEntity.java
│   └── UserPreferencesEntity.java
├── repository/
│   ├── CompensationRateJpaRepository.java
│   ├── ...
│   └── UserPreferencesJpaRepository.java
└── gateway/
    ├── JpaCompensationRateGateway.java
    ├── ...
    └── JpaUserPreferencesGateway.java
```

### Entity conventions

- Class visibility: **package-private** (no `public`)
- Primary key: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- Enum fields: `@Enumerated(EnumType.STRING)`
- FK relationships: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "xxx_id")`
- Nullable FK (`incident.on_call_period_id`): `@JoinColumn(name = "on_call_period_id", nullable = true)`
- `Instant` fields: direct, no converter (configured via `hibernate.jdbc.time_zone`)
- `LocalTime` fields: direct, no converter (native Hibernate 7)
- `Set<DayOfWeek>` fields: no `@Convert` annotation — `DayOfWeekSetConverter` auto-applies

### Gateway conventions

- Constructor injects one `JpaXxxRepository` field (`final`)
- Contains: `toEntity()`, `toDomain()`, `toDomainList()` private helpers
- `save()` always re-fetches: `repository.findById(saved.getId()).orElseThrow()`
- Zero raw SQL

---

## Step 4: Delete obsolete classes

After all JPA gateways compile and tests pass:

```
DELETE: infrastructure/config/JdbcConfig.java
DELETE: infrastructure/persistence/converter/WorkingDaysConverter.java
DELETE: infrastructure/persistence/gateway/JdbcCompensationRateGateway.java
DELETE: infrastructure/persistence/gateway/JdbcEngineerProfileGateway.java
DELETE: infrastructure/persistence/gateway/JdbcHolidayOverrideGateway.java
DELETE: infrastructure/persistence/gateway/JdbcIncidentGateway.java
DELETE: infrastructure/persistence/gateway/JdbcOnCallDayEntryGateway.java
DELETE: infrastructure/persistence/gateway/JdbcOnCallPeriodGateway.java
DELETE: infrastructure/persistence/gateway/JdbcOvertimeEntryGateway.java
DELETE: infrastructure/persistence/gateway/JdbcRegistrationSummaryGateway.java
DELETE: infrastructure/persistence/gateway/JdbcUserPreferencesGateway.java
```

---

## Step 5: Write gateway integration tests

Each test class in `src/test/java/com/dutytracker/infrastructure/persistence/gateway/`:

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

    @Test void saveThenFindById_returnsAllFields() { ... }
    @Test void deleteById_removesRecord() { ... }
    // Additional tests per gateway (see data-model.md)
}
```

**Minimum coverage per gateway** (from FR-006 / SC-003):
- `save()` → non-null ID returned
- `findById()` → all fields match saved domain record
- `deleteById()` → subsequent `findById()` returns empty
- Custom finder → returns correct subset
- Custom delete (where applicable) → removes correct rows
- `JpaIncidentGatewayTest` additionally: save with `onCallPeriodId = null`, verify retrieved domain has `onCallPeriodId = null`

---

## Verification checklist

Run after all changes are complete:

```bash
# 1. Compile (zero errors required)
cd backend && mvn clean package -DskipTests

# 2. Unit tests only (no Docker needed)
cd backend && mvn test -Dtest="!Jpa*GatewayTest"

# 3. Gateway integration tests (requires Docker)
cd backend && mvn test -Dtest="Jpa*GatewayTest"

# 4. Full test suite
cd backend && mvn test

# 5. ArchUnit gates
cd backend && mvn test -Dtest="*ArchitectureTest"

# 6. Zero JDBC template references in persistence layer
grep -r "NamedParameterJdbcTemplate" backend/src/main/java   # expect: no output

# 7. Application startup
docker compose up -d postgres
cd backend && mvn spring-boot:run   # should start without SchemaManagementException
```

All six checks must pass before the migration is considered complete.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `SchemaManagementException` on startup | Column/table mismatch between entity mapping and Flyway schema | Check `@Column` names match Flyway DDL exactly |
| `ClassCastException` on `Set<DayOfWeek>` | Old `WorkingDaysConverter` still registered | Delete `WorkingDaysConverter.java` and `JdbcConfig.java` |
| ArchUnit CA-01 failure | An entity import leaked into domain/application | Move the entity reference to the gateway layer |
| `@Autowired` ArchUnit failure | Field injection introduced | Switch to constructor injection |
| `Instant` off by one hour | `hibernate.jdbc.time_zone` missing | Confirm `application.yml` has `time_zone: UTC` |
| `LazyInitializationException` | FK entity accessed outside session | Call `entity.getFkEntity().getId()` inside the gateway method, not after returning |
