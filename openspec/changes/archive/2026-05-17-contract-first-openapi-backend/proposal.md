## Why

The backend currently owns its API contract implicitly — springdoc scans annotations at runtime and generates a spec on-the-fly. This makes the frontend dependent on a running backend to discover the API surface, and contract drift goes undetected until integration time. Introducing contract-first design makes the static `openapi.yaml` the single source of truth: server stubs are generated from it, annotation scanning is disabled, and CI validates the spec file on every build.

## What Changes

- Add `openapi-generator-maven-plugin` to `backend/pom.xml` (`generate-sources` phase) generating Spring MVC API interfaces only (`interfaceOnly=true`, `generateModels=false`, `library=spring`). Generated sources land in `target/generated-sources/openapi/`.
- Add `openapi-generator:validate` goal bound to the `validate` lifecycle phase for CI contract validation.
- Add authoritative spec file at `backend/src/main/resources/openapi/openapi.yaml` capturing the full existing API surface (5 controllers, all endpoints).
- Configure springdoc in pass-through mode: serve the static `openapi.yaml` via Swagger UI; disable annotation-based spec scanning.
- Each controller in `gateway/controllers/` implements its corresponding generated interface from `gateway.api` package.
- **BREAKING** (internal): Remove all `@Operation`, `@ApiResponse`, `@Parameter`, `@Tag` annotations from controller classes — the spec file is now the contract.
- Exclude `target/generated-sources/openapi/` from Spotless formatting.
- Update ArchUnit to allow `gateway.api` package (generated interfaces) within the gateway layer.

## Capabilities

### New Capabilities
- `openapi-contract`: The OpenAPI contract and its enforcement tooling — the static spec file, code-generation plugin configuration, CI validation, and springdoc pass-through mode.

### Modified Capabilities
<!-- No existing spec-level behavior changes — all five controllers keep identical HTTP routes, status codes, and payloads. This is a structural/tooling change, not a behavioral one. -->

## Impact

- **`backend/pom.xml`**: New plugin (`openapi-generator-maven-plugin`), new `jackson-databind-nullable` dependency (required by generator), Spotless exclusion for `target/generated-sources/openapi/`.
- **`backend/src/main/resources/openapi/openapi.yaml`**: New file — the authoritative API contract. Must be created before code-gen can run.
- **`backend/src/main/resources/application.properties`** (or `.yml`): Add springdoc properties to enable static YAML serving and disable annotation scanning.
- **All 5 controllers** (`CompensationRateController`, `OnCallPeriodController`, `ProfileController`, `IncidentController`, `HolidayController`): Strip swagger annotations; add `implements <GeneratedInterface>`.
- **`backend/src/test/java/.../ArchitectureTest.java`**: Allow `gateway.api` package in the gateway layer boundary rule.
- **CI** (`backend-ci.yml`): No change — `validate` lifecycle phase runs as part of `./mvnw clean package`, so `openapi-generator:validate` fires automatically.
