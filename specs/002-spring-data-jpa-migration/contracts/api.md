# API Contracts: Spring Data JPA Migration

**Feature**: `002-spring-data-jpa-migration`  
**Date**: 2026-04-27

## Scope

This feature is a **pure infrastructure refactoring**. The REST API surface is entirely unchanged:

- No endpoints added, removed, or renamed
- No request/response DTOs modified
- No HTTP status codes or error shapes changed
- No breaking changes for the frontend

The existing API contracts documented in [`specs/001-oncall-hours-tracker/contracts/api.md`](../../001-oncall-hours-tracker/contracts/api.md) remain authoritative and fully in effect.

## What changes internally

| Layer | Before | After |
|-------|--------|-------|
| Persistence dependency | `spring-boot-starter-data-jdbc` | `spring-boot-starter-data-jpa` |
| Gateway implementations | `JdbcXxxGateway` (raw `NamedParameterJdbcTemplate`) | `JpaXxxGateway` (`JpaRepository` delegation) |
| Persistence entities | none (domain records used directly) | `XxxJpaEntity` classes in `infrastructure.persistence.entity` |
| Attribute converter | `WorkingDaysConverter` (Spring Data JDBC) | `DayOfWeekSetConverter` (JPA `AttributeConverter`) |
| application.yml | `spring.data.jdbc.dialect: postgresql` | `spring.jpa.hibernate.ddl-auto: validate` + UTC timezone config |

## Compatibility guarantee

All existing API calls, payloads, and response shapes produced by the current `JdbcXxxGateway` implementations must produce identical results from the new `JpaXxxGateway` implementations. Integration tests enforce this at the gateway level.
