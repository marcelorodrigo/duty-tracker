# Quickstart: Spring Data JPA Migration

**Feature**: `002-spring-data-jpa-migration`  
**Date**: 2026-04-27

## Prerequisites

- Docker and Docker Compose installed
- Java 25 + Maven on `PATH`
- `docker-compose up -d postgres` running (PostgreSQL 18 on port 5432)

## Build

```bash
cd backend
mvn clean package -DskipTests
```

## Run

```bash
# via Docker Compose (recommended)
docker-compose up --build backend

# or directly with Maven
cd backend
mvn spring-boot:run
```

The application starts at `http://localhost:8080`. On startup Flyway runs pending migrations, then Hibernate validates all JPA entities against the schema. If any entity-to-column mismatch exists, the application refuses to start with a clear `SchemaManagementException`.

## Run Tests

```bash
cd backend
# all tests (unit + integration)
mvn test

# integration tests only (gateway tests — requires Docker for Testcontainers)
mvn test -pl . -Dtest="Jpa*GatewayTest"

# unit tests only (UseCase, Validator, Controller tests — no Docker required)
mvn test -Dtest="!Jpa*GatewayTest"
```

Integration tests spin up a PostgreSQL 18 Testcontainers container automatically. Docker must be running.

## Key configuration

`backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dutytracker}
    username: ${DB_USER:dutytracker}
    password: ${DB_PASSWORD:dutytracker}
  jpa:
    hibernate:
      ddl-auto: validate     # Flyway owns the schema; Hibernate validates only
    show-sql: false
    properties:
      hibernate:
        jdbc:
          time_zone: UTC     # All timestamps stored and read as UTC
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  port: 8080
```

## Verifying the migration

1. Build passes with zero compilation errors.
2. `mvn test` exits with code 0 — all unit and integration tests green.
3. Application starts without `SchemaManagementException` (Hibernate entity validation passes).
4. ArchUnit test suite (`ArchitectureTest`) passes — no CA-01/CA-02/CC-01/CC-02 violations.
5. `grep -r "NamedParameterJdbcTemplate" backend/src/main/java` returns no results.
