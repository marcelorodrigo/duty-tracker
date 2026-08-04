## 1. Database & Model Foundation

- [ ] 1.1 Add Flyway migration `V3__add_calendar_feed_url_to_profile.sql` adding `calendar_feed_url VARCHAR(2048) NULL` to `engineer_profile`.
- [ ] 1.2 Add `calendarFeedUrl` field to `EngineerProfile` domain record.
- [ ] 1.3 Add `calendarFeedUrl` column to `EngineerProfileEntity` with JPA mapping.
- [ ] 1.4 Update `EngineerProfileMapper` (MapStruct) and `JpaEngineerProfileGateway` to map the new field.
- [ ] 1.5 Update `UpdateEngineerProfileUseCase` to save the provided `calendarFeedUrl` (trim whitespace; blank/whitespace clears, non-blank overwrites, null/omitted preserves as a defensive fallback).

## 2. Profile API Contract Changes

- [ ] 2.1 Add optional `calendarFeedUrl` to `EngineerProfileResponse`, `CreateEngineerProfileRequest`, and `UpdateEngineerProfileRequest` records.
- [ ] 2.2 Add optional `calendarFeedUrl` to the corresponding schemas in `backend/src/main/resources/openapi/openapi.yaml`.
- [ ] 2.3 Update `UpdateEngineerProfileValidator` to reject non-null `calendarFeedUrl` values that are not HTTPS, exceed 2048 characters, or whose host is not exactly `app.incident.io`.
- [ ] 2.4 Regenerate OpenAPI interfaces with `./mvnw generate-sources` and verify `ProfileController` still compiles.
- [ ] 2.5 Update frontend `types/profile.ts` to include optional `calendarFeedUrl`.
- [ ] 2.6 Add URL input to `frontend/app/pages/settings/profile.vue` with help text and validation UX.

## 3. Calendar Feed Backend — Domain & Use Case

- [ ] 3.1 Create `CalendarFeedEvent` domain record (`summary`, `startDateTime`, `endDateTime`).
- [ ] 3.2 Create domain exceptions: `CalendarFeedNotConfiguredException`, `InvalidCalendarFeedUrlException`, `CalendarFeedAuthenticationException`, `CalendarFeedFetchException`, `CalendarFeedParseException`.
- [ ] 3.3 Create `GetCalendarFeedPreviewRequest` and `CalendarFeedEventResponse` usecase records.
- [ ] 3.4 Create `CalendarFeedUrlValidator` to enforce HTTPS, max length, exact host `app.incident.io`, and SSRF safety before fetch.
- [ ] 3.5 Create `PreviewCalendarFeedUseCase` that orchestrates URL validation, fetch, parse, and response mapping. Enforce a maximum of 100 parsed events; a valid but empty ICS feed must return empty lists, not an error.

## 4. Calendar Feed Backend — External Adapters

- [ ] 4.1 Add `ical4j` dependency to `backend/pom.xml` and exclude any unwanted transitive XML/JAXB libraries.
- [ ] 4.2 Define `CalendarFeedGateway` interface for fetching raw ICS text.
- [ ] 4.3 Implement `HttpCalendarFeedGateway` using Spring `RestClient` backed by a `JdkClientHttpRequestFactory` (or equivalent) configured with `Redirect.NEVER`, connect/read timeouts, and a 1 MB response body cap.
- [ ] 4.4 Define `CalendarFeedParser` interface: `String -> List<CalendarFeedEvent>`.
- [ ] 4.5 Implement `Ical4jCalendarFeedParser` normalising timed `DTSTART`/`DTEND` to `LocalDateTime` using `BusinessClock.BUSINESS_ZONE`; ignore DATE-valued (all-day) events; explicitly reinterpret floating times against `BusinessClock.BUSINESS_ZONE`; configure ical4j offline (disable `timezone.update.enabled` and any remote registry lookups).

## 5. Calendar Feed Backend — Controller & API

- [ ] 5.1 Add `GET /api/v1/calendar-feed/preview` path and `CalendarFeedPreviewResponse` schema to `openapi.yaml` under a new `Calendar Feed` tag.
- [ ] 5.2 Add import mappings in `pom.xml` for the new request/response records.
- [ ] 5.3 Create `CalendarFeedController` implementing the generated `CalendarFeedApi` and delegating to `PreviewCalendarFeedUseCase`.
- [ ] 5.4 Extend `GlobalExceptionHandler` to map the new domain exceptions to `400`, `404`, `422`, and `502` responses.
- [ ] 5.5 Regenerate sources and verify compilation.

## 6. Frontend Calendar Feed Preview

- [ ] 6.1 Create `frontend/app/types/calendarFeed.ts` with `CalendarFeedEvent` type.
- [ ] 6.2 Create `frontend/app/composables/useCalendarFeed.ts` to fetch preview from `/api/v1/calendar-feed/preview`.
- [ ] 6.3 Create `frontend/app/components/CalendarFeedPreview.vue` to render upcoming/past lists with import actions.
- [ ] 6.4 Embed `CalendarFeedPreview` on the home page (`frontend/app/pages/index.vue`) as a non-blocking section that only fetches when a `calendarFeedUrl` is configured; failures render inline.
- [ ] 6.5 Wire the "Import" action to call the existing create-period API; on success toast and refresh the preview list in place, on failure show the error toast.

## 7. Tests

- [ ] 7.1 Add unit tests for `CalendarFeedUrlValidator` covering HTTPS, HTTP, malformed URL, max length, exact host `app.incident.io`, other hosts, private IPs, and localhost.
- [ ] 7.2 Add unit tests for `Ical4jCalendarFeedParser` covering UTC, floating under a non-Amsterdam JVM default zone, named-zone, DATE-valued events being ignored, malformed inputs, and a test asserting parsing an unknown timezone triggers zero network egress.
- [ ] 7.3 Add `@WebMvcTest` for `CalendarFeedController` covering missing profile, invalid URL, SSRF-rejected URL, upstream 401/403/404, upstream 5xx/timeout, redirect response, parse failure, empty feed, and successful preview.
- [ ] 7.4 Add unit tests for `PreviewCalendarFeedUseCase` sorting of upcoming/past events.
- [ ] 7.5 Add unit tests for `UpdateEngineerProfileUseCase` covering: preserving `calendarFeedUrl` when omitted, clearing it on empty/whitespace string, saving a new valid URL, and preserving it when other fields change.
- [ ] 7.6 Add tests for `HttpCalendarFeedGateway` asserting redirect responses are not followed and response bodies exceeding 1 MB are rejected.
- [ ] 7.7 Add integration test that spins up a WireMock/simulated HTTP server and verifies end-to-end preview.
- [ ] 7.8 Add/update frontend unit tests for profile URL validation (including round-trip of `calendarFeedUrl`) and nuxt tests for `CalendarFeedPreview` rendering, empty state, and import action; assert the imported payload uses the same business-zone datetime shown in the preview.
- [ ] 7.9 Add ArchUnit rule ensuring `ical4j` types are only referenced inside `gateway.calendarfeed`.

## 8. Build & Quality

- [ ] 8.1 Run `./mvnw clean package` and fix any Spotless or test failures.
- [ ] 8.2 Run `pnpm install`, `pnpm lint`, `pnpm typecheck`, and `pnpm test` from `frontend/`.
- [ ] 8.3 Run `./run.sh` and manually verify: save URL, preview loads, import creates a period.
- [ ] 8.4 Sync any OpenAPI deltas back to the main spec with `openspec sync` if required.
