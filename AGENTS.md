<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
at specs/002-spring-data-jpa-migration/plan.md
<!-- SPECKIT END -->

## Active migration

`specs/002-spring-data-jpa-migration/` — in progress, all tasks unchecked.
Replacing 9 `JdbcXxxGateway` implementations with `JpaXxxGateway` + Spring Data JPA repositories.
`pom.xml` and `application.yml` still contain the old JDBC config; that is the first thing to update (T001–T002 in `tasks.md`).

## Commands

```bash
# Backend
cd backend && mvn clean package -DskipTests   # build
cd backend && mvn test                         # all tests (unit + integration)
cd backend && mvn test -Dtest="Jpa*GatewayTest"   # gateway integration tests only (requires Docker)
cd backend && mvn test -Dtest="!Jpa*GatewayTest"  # unit tests only (no Docker)
cd backend && mvn spring-boot:run              # run locally (needs postgres on 5432)

# Frontend (pnpm — not npm/yarn)
cd frontend && pnpm dev
cd frontend && pnpm build
cd frontend && pnpm test        # vitest

# Full stack
docker compose up --build       # starts postgres:18, backend:8080, frontend:3000
docker compose up -d postgres   # only DB (for local backend dev)
```

## Architecture

Clean Architecture; ArchUnit enforces it in `ArchitectureTest.java`.

```
domain/model/         pure Java records — zero framework annotations, ever
domain/gateway/       interfaces only
application/usecase/  UseCase<Req,Res> + Request record + RequestValidator (three files per operation)
infrastructure/       gateways, JPA entities, converters, holiday, config
presentation/api/     controllers + GlobalExceptionHandler
```

- **Constructor injection only** — `@Autowired` on fields is banned and will fail `ArchitectureTest`.
- **Domain/application cannot import infrastructure/presentation** — also enforced by ArchUnit.
- **Flyway is the sole schema authority** — `ddl-auto: validate` (Hibernate validates only, never creates/alters).

## JPA migration conventions (spec 002)

- JPA `@Entity` classes live in `infrastructure.persistence.entity` — **package-private** (not `public`).
- Domain records must remain annotation-free; `@Entity` on domain types violates CA-01.
- All `Instant` timestamp fields: direct pass-through; configure `spring.jpa.properties.hibernate.jdbc.time_zone=UTC`.
- `DayOfWeekSetConverter` (`@Converter(autoApply=true)`) replaces the old `WorkingDaysConverter`.
- Custom `delete` derived-query methods on repositories need `@Transactional`.
- Gateway implementations follow the pattern: constructor-injects one `JpaXxxRepository`, contains private `toEntity()` / `toDomain()` / `toDomainList()` helpers, zero raw SQL.

## Testing conventions

**Controller tests** (`@WebMvcTest`):
```java
@WebMvcTest(XxxController.class)
@Import(GlobalExceptionHandler.class)
class XxxControllerTest {
    @Autowired MockMvcTester mvc;
    @MockitoBean XxxUseCase xxxUseCase;  // @MockitoBean, not @MockBean (Spring Boot 4)
}
```
Import path: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` (Boot 4 package).

**Gateway integration tests** (`@DataJpaTest` + Testcontainers):
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaXxxGatewayTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired XxxJpaRepository repository;
    private JpaXxxGateway gateway;

    @BeforeEach void setUp() { gateway = new JpaXxxGateway(repository); }
}
```
Tests exercise the gateway interface, not the repository directly.

**UseCase/Validator unit tests**: plain JUnit 5 + Mockito, no Spring context.

Minimum gateway test coverage: save → non-null ID; findById → all fields match; deleteById → empty; custom finder returns correct subset; custom delete removes correct rows.

## Verification checklist (after JPA migration)

1. `mvn clean package` — zero compilation errors
2. `mvn test` — all tests green
3. Application starts without `SchemaManagementException`
4. `mvn test -Dtest="*ArchitectureTest"` — all CA/CC gates pass
5. `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` — zero results

## Spec / planning workflow

Custom `/speckit.*` slash commands live in `.opencode/command/`. Specs are stored under `specs/<id>-<slug>/` with `spec.md`, `plan.md`, `data-model.md`, `quickstart.md`, and `tasks.md`.
