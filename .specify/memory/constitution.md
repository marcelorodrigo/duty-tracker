<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0
Constitution status: Initial population from template

Modified principles:
  - [PRINCIPLE_1_NAME] → I. Clean Architecture
  - [PRINCIPLE_2_NAME] → II. UseCase Pattern
  - [PRINCIPLE_3_NAME] → III. Clean Code
  - [PRINCIPLE_4_NAME] → IV. Test Coverage (NON-NEGOTIABLE)
  - [PRINCIPLE_5_NAME] → V. Simplicity & YAGNI

Added sections:
  - Technology Stack (replaces [SECTION_2_NAME])
  - Development Workflow (replaces [SECTION_3_NAME])

Removed sections: none

Templates requiring updates:
  ✅ .specify/templates/plan-template.md — Constitution Check gates now reflect Clean Architecture
  ✅ .specify/templates/spec-template.md — no structural changes required
  ✅ .specify/templates/tasks-template.md — no structural changes required

Deferred TODOs: none
-->

# Duty Tracker Constitution

## Core Principles

### I. Clean Architecture

The codebase MUST follow Clean Architecture with strict layer separation:
- **Domain layer**: entities, value objects, domain exceptions — zero external dependencies.
- **Application layer**: UseCases and gateway interfaces — depends only on the domain.
- **Infrastructure layer**: gateway implementations, persistence, HTTP clients — depends on application layer.
- **Presentation layer**: REST controllers, serialization — depends only on application layer.

Dependency direction MUST always point inward (toward the domain). No domain or application class
may import from infrastructure or presentation. Violations are not permitted without a documented
architectural decision record (ADR).

### II. UseCase Pattern

Every business operation MUST be encapsulated in a dedicated UseCase class:
- A UseCase class MUST implement a single, named use case (e.g., `CreateDutyUseCase`).
- UseCases MUST accept a typed `Request` record and return a typed `Response` record (or `void`).
- Business validation MUST reside in a `RequestValidator` companion class, not inside controllers
  or gateway implementations.
- Controllers MUST delegate entirely to UseCases — no business logic in controllers.
- Gateway implementations MUST NOT contain business logic — only I/O translation.

### III. Clean Code

All production code MUST be readable, maintainable, and self-documenting:
- Methods MUST be short and focused (single responsibility, ideally ≤ 20 lines).
- Names MUST be intention-revealing: no abbreviations, no generic names (`data`, `obj`, `temp`).
- No magic numbers or magic strings — use named constants or enums.
- No dead code, no commented-out blocks, no TODOs in committed code.
- Constructor injection MUST be used exclusively — no field injection (`@Autowired` on fields).
- Java records MUST be used for immutable DTOs, request/response objects, and value objects.
- `final` MUST be used on all injected fields and wherever immutability is appropriate.

### IV. Test Coverage (NON-NEGOTIABLE)

All non-trivial code MUST be covered by automated tests before merging:
- **UseCases**: unit tests with mocked gateways covering happy path, validation failures, and
  domain edge cases.
- **Validators**: unit tests for every business rule, both valid and invalid inputs.
- **Controllers**: API tests (MockMvc / WebTestClient) covering request/response mapping and
  error responses.
- **Gateway implementations**: integration tests where they interact with real external systems.
- No UseCase, Validator, or Controller MUST be merged without a corresponding test suite.
- Tests MUST be independent, deterministic, and run without external infrastructure unless
  annotated as integration tests.

### V. Simplicity & YAGNI

Complexity MUST be justified; simplicity is the default:
- Start with the simplest design that satisfies the current requirement. Do not anticipate future
  requirements that have not been specified.
- No abstraction layer MUST be introduced unless it reduces duplication, enforces a boundary, or
  enables testability.
- Every deviation from the default project structure or additional dependency MUST be documented
  in the plan's Complexity Tracking table.

## Technology Stack

This project is a **Spring Boot** (Java) backend service:

- **Language**: Java 21+
- **Framework**: Spring Boot 3.x
- **Dependency Injection**: Spring IoC (constructor injection only)
- **Validation**: Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, etc.) on request records
- **Testing**: JUnit 5, AssertJ, Mockito, Spring Boot Test
- **Build tool**: Maven or Gradle (follow the existing project configuration)
- **Persistence**: Spring Data JPA or Spring Data JDBC (as applicable per feature)
- **Error handling**: `@ControllerAdvice` / `@RestControllerAdvice` for centralized exception mapping

Third-party dependencies MUST be evaluated for necessity before adoption. Every new dependency
MUST appear in the plan with justification.

## Development Workflow

### Feature Development

1. Every feature begins with a specification (`spec.md`) and implementation plan (`plan.md`).
2. The Constitution Check in `plan.md` MUST be completed and all gates MUST pass before
   implementation begins.
3. Implementation order MUST follow: domain models → domain exceptions → gateway interfaces →
   request/response records → validators → UseCases → gateway implementations →
   controllers → configuration → tests.
4. The build MUST pass (compile + all tests) before a pull request is opened.

### Constitution Check Gates (for use in plan.md)

Every plan MUST assert compliance with the following gates:

| Gate | Check |
|------|-------|
| **CA-01** | No domain/application class imports infrastructure or presentation types |
| **CA-02** | Every business operation is represented by a dedicated UseCase class |
| **CA-03** | Every UseCase has a corresponding Request record and RequestValidator |
| **CC-01** | No business logic exists in controllers or gateway implementations |
| **CC-02** | No field injection (`@Autowired` on fields) anywhere in the codebase |
| **T-01** | Every UseCase, Validator, and Controller has a corresponding test class |
| **S-01** | Every new dependency or abstraction layer is documented in Complexity Tracking |

### Code Review Requirements

- Reviewers MUST verify all Constitution Check gates before approving.
- Complexity deviations require explicit acknowledgment in the PR description.
- Test coverage for new UseCases, Validators, and Controllers is a hard requirement — PRs
  without tests MUST NOT be merged.

## Governance

This Constitution supersedes all other practices, conventions, and informal agreements.

- **Amendments** MUST be proposed with a rationale, reviewed by the team, and recorded as a
  version bump in this file. The `LAST_AMENDED_DATE` MUST be updated on every change.
- **Versioning** follows semantic versioning:
  - MAJOR: backward-incompatible principle removal or redefinition.
  - MINOR: new principle, section, or materially expanded guidance added.
  - PATCH: clarifications, wording fixes, non-semantic refinements.
- **Compliance reviews** MUST occur at every pull request via the Constitution Check gates in the
  plan template.
- **Deviations** from any principle require a documented justification in the plan's Complexity
  Tracking table. Undocumented deviations are not acceptable.
- All agent guidance files (e.g., `AGENTS.md`) MUST reference this constitution as the authority
  for project conventions.

**Version**: 1.0.0 | **Ratified**: 2026-04-25 | **Last Amended**: 2026-04-25
