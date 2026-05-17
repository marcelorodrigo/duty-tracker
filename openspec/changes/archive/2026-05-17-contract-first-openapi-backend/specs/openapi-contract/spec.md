## ADDED Requirements

### Requirement: Static OpenAPI spec is the authoritative contract
The backend SHALL maintain a single static `openapi.yaml` file at `backend/src/main/resources/openapi/openapi.yaml` that defines the complete API surface. This file is the single source of truth; all other representations (Swagger UI, generated interfaces) SHALL be derived from it.

#### Scenario: Spec file is reachable from the classpath
- **WHEN** the Spring application starts
- **THEN** the file `openapi/openapi.yaml` is resolvable from the classpath without any additional resource mapping

#### Scenario: Spec file passes schema validation during build
- **WHEN** `./mvnw clean package` (or any lifecycle phase ≥ `generate-sources`) is executed
- **THEN** the `openapi-generator:generate` goal validates the spec implicitly (via `skipValidateSpec=false` default) before generating sources, and the build fails if the YAML is malformed or violates the OpenAPI 3.x schema; no separate `validate`-phase execution is required because the `validate` goal does not exist in plugin v7.22.0

### Requirement: Generated Spring MVC interfaces bind controllers to the contract
The Maven build SHALL generate one Spring MVC API interface per controller group from `openapi.yaml` during the `generate-sources` phase. Each controller class SHALL implement its corresponding generated interface. A method signature mismatch between the spec and the controller SHALL cause a compile error.

#### Scenario: Code generation runs before compilation
- **WHEN** `./mvnw compile` (or `./mvnw clean package`) is executed
- **THEN** generated interface sources are present in `target/generated-sources/openapi/` before Java compilation begins

#### Scenario: Generated interfaces land in the correct package
- **WHEN** code generation completes
- **THEN** all generated API interfaces are in the package `com.github.marcelorodrigo.dutytracker.gateway.api`

#### Scenario: Controller implements generated interface
- **WHEN** the project compiles successfully
- **THEN** each of the five controllers (`CompensationRateController`, `OnCallPeriodController`, `ProfileController`, `IncidentController`, `HolidayController`) carries `implements <GeneratedInterface>` on its class declaration

#### Scenario: Spec-controller divergence is caught at compile time
- **WHEN** a route, HTTP method, or response type in `openapi.yaml` is changed without updating the corresponding controller
- **THEN** the build fails with a Java compile error (method signature mismatch on the `implements` clause)

### Requirement: Swagger UI serves the static spec file
springdoc-openapi SHALL be configured in pass-through mode: it SHALL serve the static `openapi.yaml` file at Swagger UI and SHALL NOT scan Spring MVC controller annotations to construct a spec at runtime.

#### Scenario: Swagger UI is accessible and renders the correct spec
- **WHEN** a browser navigates to `/swagger-ui.html`
- **THEN** Swagger UI loads and displays the API operations defined in the static `openapi.yaml`

#### Scenario: Static spec file is directly accessible
- **WHEN** an HTTP `GET /openapi/openapi.yaml` request is made
- **THEN** the server responds with HTTP 200 and a body whose first non-whitespace content starts with `openapi: "3.`

#### Scenario: Annotation-driven spec generation is disabled
- **WHEN** an `@Operation` or `@Tag` annotation is present on a controller method
- **THEN** that annotation has no effect on the spec served by springdoc (the static file is served unchanged)

### Requirement: Generated sources are excluded from Spotless formatting
The Spotless Maven plugin SHALL NOT format files under `target/generated-sources/`. A formatting-check failure in generated code SHALL NOT fail the build.

#### Scenario: Build succeeds with generated code present
- **WHEN** `./mvnw spotless:check` runs after code generation
- **THEN** the check passes regardless of the formatting style used in `target/generated-sources/openapi/`

### Requirement: Swagger annotations are removed from controllers
All springdoc/Swagger annotation imports and usages (`@Operation`, `@ApiResponse`, `@ApiResponses`, `@Parameter`, `@Tag`, `@Schema`, `@Content`) SHALL be removed from all five controller classes. The contract is expressed solely in `openapi.yaml`.

#### Scenario: Controllers compile without springdoc annotation imports
- **WHEN** the project compiles
- **THEN** none of the five controller files contain an import for `io.swagger.v3.oas.annotations.*`
