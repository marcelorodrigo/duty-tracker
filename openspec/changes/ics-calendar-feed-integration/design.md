## Context

Duty Tracker is a single-user application with no authentication layer; all endpoints assume a single trusted operator. It stores on-call periods in a local PostgreSQL database and calculates compensation from them. incident.io already publishes each engineer's schedule as a personal ICS feed; that feed URL is credential-equivalent and is echoed back in profile read responses only because the app is single-user/local. Today, engineers must manually recreate those periods in Duty Tracker.

CORS probing confirmed the incident.io feed cannot be fetched directly from the browser (no `Access-Control-Allow-Origin`), eliminating a pure frontend integration. The chosen approach is a server-side proxy: the backend fetches and parses the feed, returns ephemeral preview events, and the frontend promotes selected events to persisted on-call periods through the existing `/api/v1/oncall-periods` endpoint.

Existing conventions to respect:
- Clean Architecture: requests/responses in `usecase.request.*` / `usecase.response.*`, business logic in `usecase.*`, controllers in `gateway.controllers.*`, external adapters in `gateway.*`.
- OpenAPI-first: controllers implement interfaces generated from `backend/src/main/resources/openapi/openapi.yaml` with import mappings to hand-written usecase records.
- Tests: pure unit tests for use cases/validators, `@WebMvcTest` controller tests, Testcontainers integration tests, ArchUnit architecture tests.
- Profile lives in a single row with no relational complexity.

## Goals / Non-Goals

**Goals:**
- Let engineers store an optional personal ICS feed URL in their profile.
- Fetch and parse the feed server-side and expose preview events.
- Show upcoming and past on-call events as a list in the UI.
- Allow one-click promotion of a preview event to a tracked on-call period.
- Fetch the feed server-side because incident.io does not allow browser-side (CORS) fetches.

**Non-Goals:**
- A visual calendar grid widget.
- Automatic two-way synchronisation (updating Duty Tracker when the ICS feed changes after import).
- Persisting parsed calendar events as temporary database rows.
- Supporting multiple feeds or team-wide feeds.
- Real-time polling or push updates.

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Feed fetcher location | Backend proxy | incident.io sends no CORS headers, so browser fetch is blocked. Server-side fetch centralises parsing. Mirrors how Google Calendar consumes external ICS feeds. |
| HTTP client | `RestClient` with custom `ClientHttpRequestFactory` | Spring Boot 4.1 ships `RestClient`; this is the app's first outbound HTTP integration. The factory enforces connect/read timeouts, disables redirects, and caps response body size. |
| ICS parser | `ical4j` (`org.mnode.ical4j:ical4j`) | Dominant Java library for RFC 5545 parsing; handles `VTIMEZONE`, recurrence, and UTC/floating-time conversion. Configured offline: timezone registry updates and outbound `tzurl.org` calls are disabled. Alternatives considered: hand-rolled parser (fragile) and `biweekly` (less active). Caveat: ical4j has optional XML dependencies (`jakarta.xml.bind`) that may need exclusions in `pom.xml`. |
| Time representation | Normalise to `LocalDateTime` in business zone | Matches the existing `CreateOnCallPeriodRequest`/`OnCallPeriodResponse` contract and the database schema. The parser converts event `DTSTART`/`DTEND` to `LocalDateTime` using `BusinessClock.BUSINESS_ZONE` (`Europe/Amsterdam`). Floating times are explicitly reinterpreted against `BusinessClock.BUSINESS_ZONE`, not the JVM default zone. |
| Preview endpoint | `GET /api/v1/calendar-feed/preview` | Single endpoint that reads the URL from the current user's profile, fetches live data, and returns candidate events. No separate CRUD for cached entries. |
| Feed URL storage | Plain `VARCHAR` column on `engineer_profile` | Stored alongside the rest of the single-user profile. Validation: must be HTTPS, at most 2048 characters, and its host must exactly match `app.incident.io` (validated both on profile save and before fetch). The URL is returned in profile read responses so the form can display it, and is withheld from the calendar preview response. Update contract: the profile form always round-trips `calendarFeedUrl`; blank/whitespace clears the stored value, non-blank overwrites it, and `null`/omitted preserves it as a defensive fallback for direct API callers. |
| SSRF mitigation | Host allowlist + redirect block | The backend fetches a user-supplied URL, so the URL validator rejects non-allowed hosts; the HTTP client does not follow redirects. For a single-user local app, pinning the host to `app.incident.io` plus disabling redirects is the primary control. |
| Fetch bounds | Max 1 MB body, 100 events, 10 s timeout | Limits resource use and parser workload for a personal schedule feed. Excess body size or event count fails the request cleanly. |
| Duplicate detection | None in preview | The preview is ephemeral and the schedule can change after import. The existing create-period API already validates overlaps and rejects them. Importing a duplicate merely surfaces the existing error to the user. |
| UI shape | Simple chronological list | Two sections: "Upcoming" (ascending) and "Past" (descending, last 12 months). Each row has period dates, summary, and an import action. |
| Refresh model | Live fetch when preview section loads + manual refresh | Keeps data fresh and avoids cache invalidation complexity. The fetch is non-blocking on the home page and has a short timeout (e.g. 10s). Failures are rendered inline without breaking the rest of the dashboard. |
| Import action | Reuse existing create-period flow | Promoting an event POSTs `{ startDateTime, endDateTime }` to `/api/v1/oncall-periods`. This reuses validation, holiday suggestions, and persistence without duplicating logic. |
| Event filtering | Show all timed events from the feed | The incident.io schedule feed contains only timed on-call schedule blocks; no SUMMARY-based filtering, incident handling, or all-day event support is needed. DATE-valued `VEVENT`s are ignored. Recurring events (`RRULE`) are expanded into individual occurrences by the parser. |

## Package / Component Overview

### Backend

| File | Role |
|------|------|
| `domain/CalendarFeedEvent.java` | Domain object for a parsed preview event: `summary`, `startDateTime`, `endDateTime`. |
| `domain/exceptions/CalendarFeedException.java` | Domain error for invalid URL, fetch failure, or parse failure. |
| `domain/exceptions/CalendarFeedNotConfiguredException.java` | Domain error when preview is requested but no `calendarFeedUrl` is stored. |
| `usecase/calendarfeed/PreviewCalendarFeedUseCase.java` | Orchestrates fetch → parse → normalise → response mapping. |
| `usecase/request/calendarfeed/GetCalendarFeedPreviewRequest.java` | Empty marker record for the use case request. |
| `usecase/response/calendarfeed/CalendarFeedEventResponse.java` | Candidate event returned to the client: `startDateTime`, `endDateTime`, `summary`. |
| `usecase/validator/calendarfeed/CalendarFeedUrlValidator.java` | Validates the stored URL is HTTPS, syntactically valid, and SSRF-safe (allowed host, no private IP, no localhost) before fetch. |
| `gateway/calendarfeed/CalendarFeedGateway.java` | Interface for fetching raw ICS text from a URL. |
| `gateway/calendarfeed/HttpCalendarFeedGateway.java` | `RestClient` implementation of `CalendarFeedGateway`. |
| `gateway/calendarfeed/CalendarFeedParser.java` | Interface: `String (ics) -> List<CalendarFeedEvent>`. |
| `gateway/calendarfeed/Ical4jCalendarFeedParser.java` | Offline `ical4j` implementation; ignores DATE-valued (all-day) events and forces floating times to `BusinessClock.BUSINESS_ZONE`. |
| `gateway/controllers/calendarfeed/CalendarFeedController.java` | Implements the generated `CalendarFeedApi` interface; delegates to `PreviewCalendarFeedUseCase`. |
| `gateway/postgres/entity/EngineerProfileEntity.java` | Add `calendarFeedUrl` column. |
| `gateway/postgres/profile/JpaEngineerProfileGateway.java` | Update mapping to include new field. |
| `gateway/profile/EngineerProfileMapper.java` | Update MapStruct mappings to include `calendarFeedUrl` in entity ↔ domain and domain ↔ response conversions. |

### Frontend

| File | Role |
|------|------|
| `app/types/profile.ts` | Add optional `calendarFeedUrl?: string`. |
| `app/types/calendarFeed.ts` | `CalendarFeedEvent { startDateTime, endDateTime, summary }`. |
| `app/composables/useCalendarFeed.ts` | Fetch preview from `/api/v1/calendar-feed/preview`; expose `pending`, `error`, `events`, `refresh`. |
| `app/pages/settings/profile.vue` | Add URL input section with help text and validation hint. The form must always include the current `calendarFeedUrl` value in the `PUT` payload, sending `""` only when the user has cleared the field. |
| `app/components/CalendarFeedPreview.vue` | List component for upcoming/past events with import actions; shows a prompt when no feed URL is configured. |
| `app/pages/index.vue` | Embed `CalendarFeedPreview` on the home page above or alongside the existing active/past period lists. |

## API Contract Changes

`openapi.yaml` extensions:
- `EngineerProfileResponse`, `CreateEngineerProfileRequest`, `UpdateEngineerProfileRequest`: add optional `calendarFeedUrl` string (`type: string, nullable: true`, max 2048, HTTPS).
- New path `GET /api/v1/calendar-feed/preview` returning `CalendarFeedPreviewResponse` (object with `upcoming` and `past` arrays of `CalendarFeedEventResponse`) under a new tag `Calendar Feed`.
- New error responses mapped by the global handler: `400` for invalid/SSRF-rejected URL, `422` when upstream rejects the request (401/403/404), `502` for upstream unreachable/5xx/timeout/malformed body, `404` when profile or feed URL is missing.

## Data Flow

1. User pastes the ICS URL into the profile form and saves -> `PUT /api/v1/profile` persists the URL.
2. User opens the home page (`/`) -> frontend fetches preview from `GET /api/v1/calendar-feed/preview`.
3. Backend reads the profile row, validates the URL (HTTPS, allowlist, no internal address), and fetches the raw `.ics` text via `CalendarFeedGateway` without following redirects.
4. `CalendarFeedParser` parses the text into `CalendarFeedEvent` domain objects.
5. `PreviewCalendarFeedUseCase` sorts events into upcoming/past lists.
6. Response returns sorted lists; frontend renders the sections.
7. User clicks "Import" -> frontend POSTs to `/api/v1/oncall-periods`, then refreshes the preview list.

## Error Handling

| Error | Type | Status | When |
|-------|------|--------|------|
| `CalendarFeedNotConfiguredException` | domain | 404 | Profile has no `calendarFeedUrl`. |
| `InvalidCalendarFeedUrlException` | domain | 400 | URL is malformed, not HTTPS, or rejected by SSRF checks (wrong host, private IP, localhost, etc.). |
| `CalendarFeedAuthenticationException` | domain | 422 | Upstream returns 401/403/404, indicating the stored feed URL is wrong or revoked. |
| `CalendarFeedFetchException` | domain | 502 | Upstream is unreachable, returns 5xx, or times out. |
| `CalendarFeedParseException` | domain | 502 | ICS body is malformed. |

All exceptions are thrown from use case / gateway layers and mapped by the existing `GlobalExceptionHandler` (or a new handler method added to it).

## Risks / Trade-offs

- **[Risk] SSRF via user-supplied feed URL** → Mitigation: validate the URL host against an exact allowlist (`app.incident.io`), disable HTTP redirects, cap response size, and treat validation failures as `400` before any outbound request.
- **[Risk] ical4j makes its own outbound calls** → Mitigation: disable timezone registry updates (`net.fortuna.ical4j.timezone.update.enabled=false`) and any other network-dependent ical4j options; test that parsing a feed with unknown `VTIMEZONE`s does not produce network traffic.
- **[Risk] ical4j dependency weight** → Mitigation: pin a recent stable version and exclude XML/JAXB modules if the build pulls them in; add an architecture test to ensure no direct ical4j imports leak out of the `gateway.calendarfeed` package.
- **[Risk] Feed format changes** → Mitigation: parser is behind the `CalendarFeedParser` interface; if incident.io changes output, only one implementation changes.
- **[Risk] Floating times use JVM default zone** → Mitigation: do not rely on ical4j defaults; explicitly reinterpret floating `LocalDateTime` values against `BusinessClock.BUSINESS_ZONE`. Test with the JVM default zone set to something other than Amsterdam.
- **[Risk] Time-zone misinterpretation of instants** → Mitigation: convert UTC/zoned instants to `LocalDateTime` using `BusinessClock.BUSINESS_ZONE`; document this in `spec.md`. Add unit tests for UTC, floating, and named-zone events with the business zone explicitly set.
- **[Trade-off] Live fetch on preview load** adds latency. Acceptable because the fetch is non-blocking on the home page. If latency becomes painful, a short-lived backend cache can be added without changing the public API.
- **[Trade-off] No duplicate hint in preview** means a user may try to import an already-tracked period. The existing create-period validator already rejects overlaps with a clear error, so this is a minor UX cost for a much simpler design. Note that schedule drift (e.g. incident.io editing a block, or a DST transition) can shift the rendered business-zone time so the imported value no longer exactly overlaps the previously tracked period.

## Migration Plan

1. Flyway migration `V3__add_calendar_feed_url_to_profile.sql` adds `calendar_feed_url VARCHAR(2048) NULL` to `engineer_profile`.
2. Update `openapi.yaml`, regenerate interfaces with `./mvnw generate-sources`.
3. Implement backend changes in dependency order: domain → gateway interfaces → use case → parser/gateway implementations → controller.
4. Implement frontend changes: types → profile form → preview composable → embed `CalendarFeedPreview` on `app/pages/index.vue`.
5. Tests: unit tests for validator and parser; `@WebMvcTest` for `CalendarFeedController`; integration test for end-to-end preview with a WireMock feed.
6. Rollback: delete or disable the new code files. Retain the `engineer_profile.calendar_feed_url` column; dropping it requires a separate forward migration with a retention and backup plan.

## Open Questions

None.
