## 1. Author the OpenAPI spec file

- [x] 1.1 Create `backend/src/main/resources/openapi/openapi.yaml` with `openapi: "3.1.0"`, `info`, and `servers` sections
- [x] 1.2 Add `/api/v1/profile` paths (POST, GET, PUT, DELETE) matching `ProfileController` exactly — request bodies: `CreateEngineerProfileRequest`, `UpdateEngineerProfileRequest`; response: `EngineerProfileResponse`
- [x] 1.3 Add `/api/v1/compensation-rates` paths (GET, POST, PUT `/{id}`, DELETE `/{id}`) matching `CompensationRateController` — request bodies: `CreateCompensationRateRequest`, `UpdateCompensationRateRequest`; response: `CompensationRateResponse`, `CompensationRateTableResponse`
- [x] 1.4 Add `/api/v1/oncall-periods` paths (POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}`, GET `/{id}/holidays`, PUT `/{id}/holidays`, POST `/{id}/calculate`, GET `/{id}/report`, GET `/{id}/earnings`) matching `OnCallPeriodController`
- [x] 1.5 Add `/api/v1/incidents` paths (POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}`, POST `/{id}/calculate`) matching `IncidentController` — include `UpdateIncidentBody` schema for PUT body
- [x] 1.6 Add `/api/v1/holidays/suggestions` path (GET with `start` and `end` query params) matching `HolidayController`
- [x] 1.7 Define all required schema components under `components/schemas` — reference the Java records in `usecase/request/` and `usecase/response/` for field names and types
- [x] 1.8 Run `./mvnw validate` (after plugin is configured in task 2) and confirm the spec passes `openapi-generator:validate` with no errors

## 2. Configure the Maven plugin

- [x] 2.1 Add `org.openapitools:openapi-generator-maven-plugin` (version `7.22.0`, not `7.12.0` as originally planned — v7.x renamed `library=spring` to `library=spring-boot`) to `backend/pom.xml` `<build><plugins>` with `generate-sources` execution: `inputSpec` pointing to `${project.basedir}/src/main/resources/openapi/openapi.yaml`, `generatorName=spring`, `output=${project.build.directory}/generated-sources/openapi`, `generateApis=true`, `generateModels=false` (top-level, not in configOptions), `interfaceOnly=true`, `library=spring-boot`, `apiPackage=com.github.marcelorodrigo.dutytracker.gateway.api`
- [x] 2.2 Add second execution binding `openapi-generator:validate` to the `validate` lifecycle phase with the same `inputSpec` (placed before `generate-api-interfaces` execution) — **Note**: the `validate` goal does not exist in openapi-generator-maven-plugin v7.22.0 (only `generate` and `help` are available). Spec validation is already performed by the `generate` goal via its built-in `skipValidateSpec=false` default. No separate validate execution was added.
- [x] 2.3 ~~Add `org.openapitools:jackson-databind-nullable:0.2.6` as a compile-scoped dependency in `pom.xml`~~ — **Removed**: not needed because `openApiNullable=false` is set in configOptions; no `JsonNullable` types are emitted
- [x] 2.4 Add Spotless exclusion for generated sources in the `<java>` block: `<excludes><exclude>**/generated-sources/**</exclude></excludes>`
- [x] 2.5 Run `./mvnw generate-sources` and verify `target/generated-sources/openapi/` contains the expected interface files in the `gateway.api` package
- [x] 2.6 Confirm `./mvnw spotless:check` passes with generated sources present

## 3. Configure springdoc pass-through mode

- [x] 3.1 Add springdoc properties to `backend/src/main/resources/application.properties`: set `springdoc.swagger-ui.url=/openapi/openapi.yaml` and `springdoc.packages-to-scan=` (empty) to disable annotation scanning
- [ ] 3.2 Start the application locally and verify `GET /openapi/openapi.yaml` returns HTTP 200 with the correct spec content — **deferred: requires running app**
- [ ] 3.3 Verify `/swagger-ui.html` renders the API operations from the static spec file — **deferred: requires running app**

## 4. Wire controllers to generated interfaces

- [x] 4.1 Update `CompensationRateController` — add `implements <GeneratedInterface>` matching the compensation-rates API interface; fix any method signature differences revealed by the compiler
- [x] 4.2 Update `OnCallPeriodController` — add `implements <GeneratedInterface>` for the oncall-periods API interface; fix signature mismatches
- [x] 4.3 Update `ProfileController` — add `implements <GeneratedInterface>` for the profile API interface; fix signature mismatches
- [x] 4.4 Update `IncidentController` — add `implements <GeneratedInterface>` for the incidents API interface; verify `UpdateIncidentBody` local record matches generated method parameter type
- [x] 4.5 Update `HolidayController` — add `implements <GeneratedInterface>` for the holidays API interface; fix signature mismatches
- [x] 4.6 Run `./mvnw compile` and confirm all five controllers compile cleanly with no unimplemented method errors

## 5. Strip swagger annotations from controllers

- [x] 5.1 Remove all `io.swagger.v3.oas.annotations.*` imports and `@Operation`, `@ApiResponse`, `@ApiResponses`, `@Parameter`, `@Tag`, `@Schema`, `@Content` annotations from `CompensationRateController`
- [x] 5.2 Remove swagger annotations from `OnCallPeriodController`
- [x] 5.3 Remove swagger annotations from `ProfileController`
- [x] 5.4 Remove swagger annotations from `IncidentController`
- [x] 5.5 Remove swagger annotations from `HolidayController`
- [x] 5.6 Run `./mvnw spotless:apply` to reformat the cleaned controller files
- [x] 5.7 Confirm no `io.swagger.v3.oas.annotations` import remains in any file under `gateway/controllers/`

## 6. Full build verification

- [x] 6.1 Run `./mvnw clean package` from repo root; confirm build passes including `validate`, `generate-sources`, `compile`, `spotless:check`, and all tests
- [x] 6.2 Verify `@WebMvcTest` slice tests for each controller still pass (no method signature regressions)
- [ ] 6.3 Start the app (`docker compose up --build` or local) and perform a manual smoke-check: confirm `/swagger-ui.html` loads, displays all 22 endpoints, and `GET /openapi/openapi.yaml` returns the spec file — **deferred: requires running app**
