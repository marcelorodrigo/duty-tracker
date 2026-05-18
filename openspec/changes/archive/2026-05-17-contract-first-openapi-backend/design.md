## Context

The backend exposes a REST API through 5 controllers (`CompensationRateController`, `OnCallPeriodController`, `ProfileController`, `IncidentController`, `HolidayController`) covering 22 endpoints. Currently, springdoc scans `@Operation`/`@Tag`/`@ApiResponse`/`@Parameter` annotations at startup to generate the OpenAPI spec. This is code-first: the annotations are the contract, and the spec is a derivative artifact.

The change inverts this: a hand-authored `openapi.yaml` becomes the single source of truth. The Maven plugin generates Spring MVC interfaces from it; controllers implement those interfaces. The spec is static — springdoc serves it as-is to Swagger UI, with annotation scanning disabled.

**ArchUnit context:** The existing `ArchitectureTest.java` scans `com.dutytracker` (note: the `@AnalyzeClasses` base package `com.dutytracker` does not match the actual package root `com.github.marcelorodrigo.dutytracker`, so the ArchUnit rules currently match zero classes — `allowEmptyShould(true)` suppresses the failure). The generated `gateway.api` package sits within `gateway`, which is already the outermost layer; no rule currently forbids imports into it, so no ArchUnit changes are needed for correctness. However, the test annotation should be fixed as a separate concern — this change will note but not fix the package mismatch.

## Goals / Non-Goals

**Goals:**
- `openapi.yaml` is the authoritative, version-controlled API contract.
- Controller code is structurally bound to the spec (via generated interface `implements`); spec drift causes a compile error.
- CI validates the spec file on every build (no malformed YAML, no schema violations).
- Swagger UI continues to serve the full API spec at `/swagger-ui.html`.
- No behavioral change to any endpoint — routes, status codes, and payloads remain identical.

**Non-Goals:**
- Generating Java model/DTO classes from the spec. Use-case request/response records remain as HTTP body types; this defers model generation to a follow-up change.
- Introducing a new gateway DTO layer between generated models and use-case records.
- Fixing the ArchUnit base-package mismatch (pre-existing issue, separate concern).
- Client SDK generation (frontend TypeScript types).
- Multi-file spec splitting.

## Decisions

### Decision 1: `interfaceOnly=true`, `generateModels=false`

**Chosen:** Generate only Spring MVC API interfaces; no model classes.

**Rationale:** The use-case request/response records already serve as HTTP body types and are annotated with `@JsonProperty` / Bean Validation implicitly via Jackson defaults. Generating parallel DTO classes would require either MapStruct mappers between them or discarding the existing records — both are out of scope. Interface-only generation gives compile-time binding to the contract with zero model duplication.

**Alternative considered:** Generate models too, replace request/response records with generated DTOs. Rejected — large blast radius (all use cases), zero functional benefit for this change, and model generation is a separate concern.

---

### Decision 2: Spec file at `backend/src/main/resources/openapi/openapi.yaml`

**Chosen:** `src/main/resources/openapi/openapi.yaml`

**Rationale:** This location is on the Spring resource classpath, so springdoc can serve it via `spring.webmvc.openapi.api-docs.path` or `springdoc.swagger-ui.url` without an additional resource handler. It is also inside the Maven source tree, so it participates in normal `clean` cycles (not under `target/`).

**Alternative considered:** `src/main/openapi/openapi.yaml` (outside resources). Rejected — requires a separate `<resource>` declaration in `pom.xml` and cannot be served from classpath easily.

---

### Decision 3: Generated sources in `target/generated-sources/openapi/`; excluded from Spotless

**Chosen:** Default openapi-generator output directory; added to Spotless `<excludes>`.

**Rationale:** `target/` is the conventional Maven output root; generated code there is never committed. Spotless runs on `src/main/java/**` by default; the exclusion ensures Palantir format checker ignores generated files that may use different style conventions.

**Spotless exclusion config:**
```xml
<java>
  <excludes>
    <exclude>**/generated-sources/**</exclude>
  </excludes>
  ...
</java>
```

---

### Decision 4: springdoc in pass-through mode

**Chosen:** Configure springdoc to serve the static `openapi.yaml` file; disable annotation scanning entirely.

**Rationale:** With contract-first, the spec file *is* the truth. Annotation scanning would produce a second, potentially diverging spec. Pass-through eliminates that divergence and removes the need for any swagger annotations on controllers.

**Configuration:**
```properties
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.url=/openapi/openapi.yaml
springdoc.api-docs.path=/v3/api-docs
# Disable classpath scanning for annotations:
springdoc.packages-to-scan=
springdoc.paths-to-match=
```

springdoc serves `/openapi/openapi.yaml` from the classpath resource location automatically when the file is present at `src/main/resources/openapi/openapi.yaml`.

---

### Decision 5: `openapi-generator:validate` wired to `validate` lifecycle phase

**Chosen:** Bind `validate` goal to the `validate` phase (runs before `compile`).

**Rationale:** `validate` is the first phase in `./mvnw clean package`, so a malformed or schema-violating spec fails fast before any code generation or compilation. No CI change needed — the existing `./mvnw clean package` command already covers it.

---

### Decision 6: Generated interface package `com.github.marcelorodrigo.dutytracker.gateway.api`

**Chosen:** `gateway.api` sub-package inside the existing `gateway` layer.

**Rationale:** Generated interfaces are gateway-layer artifacts — they define the HTTP contract that controllers (also in `gateway`) implement. Placing them in `gateway.api` keeps them within the existing clean-architecture boundary without crossing layer rules. The use-case layer never imports from `gateway`, so no architectural rule is violated.

**Alternative considered:** A top-level `api` package outside `gateway`. Rejected — would be a new layer not recognized by existing structure; controllers in `gateway.controllers` would import from `api`, which is outside the gateway, adding confusion.

---

### Decision 7: `jackson-databind-nullable` dependency

**Chosen:** Add `org.openapitools:jackson-databind-nullable` as a compile-scoped dependency.

**Rationale:** The openapi-generator Spring library emits imports of `org.openapitools.jackson.nullable.JsonNullable` in generated interfaces even when `useBeanValidation=false`. Without this dependency, the generated code does not compile. Version `0.2.6` is the stable release compatible with Jackson 2.x/3.x.

**Alternative:** Use `useOptional=true` generator config. Rejected — changes method signatures in generated interfaces in ways that complicate controller implementations.

---

### Decision 8: `UpdateIncidentBody` local record handling

**Chosen:** `IncidentController` currently defines a private `UpdateIncidentBody` record as the `@RequestBody` type for `PUT /api/v1/incidents/{id}`. The generated interface will reference a schema type for this operation — the spec will define an `UpdateIncidentBody` schema to match, and the controller will continue using its local record (which Jackson binds without the generated model, since models are not generated).

**Rationale:** Since `generateModels=false`, the generated interface method will reference the same Java type the controller already uses. The spec schema name must match what the generator expects. The controller method signature must match the generated interface exactly — if they diverge, the `implements` clause causes a compile error, which is the desired catch.

## Risks / Trade-offs

- **Spec-code drift during authoring:** When writing the initial `openapi.yaml`, a mismatch between the spec schema types and the controller's existing request/response Java records (which are not generated) will only be caught at runtime (Jackson deserialization errors), not compile-time. This is an inherent limitation of `generateModels=false`. Mitigation: integration tests in `@WebMvcTest` slices continue to exercise the full request/response binding.

- **`UpdateIncidentBody` local record:** The `PUT /incidents/{id}` endpoint uses a controller-private record as its request body. Since models are not generated, the generated interface method must reference the same type. This requires care when authoring the spec's request body schema — the Implementer must verify the generated method signature matches the controller method signature exactly.

- **springdoc annotation removal is non-reversible in this branch:** Stripping `@Operation` etc. from 5 controllers means the annotation-driven path is gone. If the static spec is misconfigured, Swagger UI silently serves an empty or wrong spec. Mitigation: add a smoke test that `GET /openapi/openapi.yaml` returns 200 with `openapi: 3.` prefix.

- **ArchUnit base-package mismatch (pre-existing):** `ArchitectureTest.java` scans `com.dutytracker` but the actual root is `com.github.marcelorodrigo.dutytracker`. All rules currently match zero classes — `allowEmptyShould(true)` hides this. This change does not worsen the situation, but it remains a latent risk that architectural violations go undetected.

## Migration Plan

1. **Author `openapi.yaml`** — extract the full API surface from the existing controllers. This is the prerequisite for code-gen.
2. **Configure plugin** — add `openapi-generator-maven-plugin` to `pom.xml`; add `jackson-databind-nullable` dependency; add Spotless exclusion.
3. **Verify generation** — run `./mvnw generate-sources`; inspect `target/generated-sources/openapi/`; confirm 5 interface files are produced in `gateway.api` package.
4. **Wire controllers** — add `implements <Interface>` to each controller; fix any method signature mismatches revealed by the compiler.
5. **Strip swagger annotations** — remove all `@Operation`, `@ApiResponse`, `@Parameter`, `@Tag` imports and annotations from all 5 controllers.
6. **Configure springdoc pass-through** — add properties to `application.properties`.
7. **Run full build** — `./mvnw clean package`; verify `validate` phase runs and tests pass.
8. **Smoke-check Swagger UI** — start the app locally and confirm `/swagger-ui.html` renders the correct spec.

**Rollback:** Revert `pom.xml` and controller changes; springdoc annotation scanning resumes on next build.

## Open Questions

- **springdoc `packages-to-scan` empty string behavior:** Verify that setting `springdoc.packages-to-scan=` (empty) completely suppresses annotation scanning, or whether `springdoc.api-docs.enabled=false` combined with `springdoc.swagger-ui.url` override is needed. The Implementer should test with the running app.
- **`GET /incidents/{id}` implementation pattern:** The current implementation calls `listIncidents` then filters in memory. The spec can codify this as a normal `GET /{id}` returning 200/404 — no behavioral change needed, but worth confirming the spec reflects actual behavior (including the 404 path that goes through the domain exception handler) rather than what an ideal implementation would do.
