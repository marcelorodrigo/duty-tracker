## ADDED Requirements

### Requirement: Backend fetches ICS feed from configured URL
The system SHALL read the engineer's stored `calendarFeedUrl` and fetch the raw `.ics` content server-side when requested.

#### Scenario: Fetch succeeds for a valid feed
- **WHEN** a preview is requested and a valid HTTPS `calendarFeedUrl` is configured
- **THEN** the backend fetches the feed and receives `HTTP 200` with a valid iCalendar body

#### Scenario: Missing feed URL returns 404
- **WHEN** a preview is requested but the engineer profile has no `calendarFeedUrl`
- **THEN** the system returns `404 Not Found` with a message indicating the feed URL is not configured

#### Scenario: Non-HTTPS URL returns 400
- **WHEN** a preview is requested and the stored `calendarFeedUrl` does not start with `https://`
- **THEN** the system returns `400 Bad Request` with a message indicating the URL must be HTTPS

#### Scenario: Upstream rejects feed URL returns 422
- **WHEN** a preview is requested and the external feed returns `HTTP 401`, `403`, or `404`
- **THEN** the system returns `422 Unprocessable Entity` with a message indicating the feed URL should be checked

#### Scenario: Upstream unreachable or error returns 502
- **WHEN** a preview is requested and the external feed returns `HTTP 5xx` or times out
- **THEN** the system returns `502 Bad Gateway` with a user-friendly error message

#### Scenario: Disallowed or internal URL returns 400
- **WHEN** a preview is requested and the stored `calendarFeedUrl` points to `localhost`, a private IP, or any host other than exactly `app.incident.io`
- **THEN** the system returns `400 Bad Request` with a message indicating the URL is not allowed

---

### Requirement: Backend parses ICS feed into candidate on-call events
The system SHALL parse the fetched `.ics` content into a list of events, each with a summary, start datetime, and end datetime.

#### Scenario: Valid ICS with one UTC event is converted to business time
- **WHEN** the feed contains one `VEVENT` with `DTSTART:20250804T140000Z`, `DTEND:20250811T140000Z`, and `SUMMARY:Marcelo on-call`
- **THEN** the system produces one candidate event with start `2025-08-04T16:00:00`, end `2025-08-11T16:00:00`, and summary "Marcelo on-call" (business zone `Europe/Amsterdam`, UTC+2 in August)

#### Scenario: Floating event is interpreted in business zone
- **WHEN** the feed contains one `VEVENT` with floating `DTSTART:20250804T140000`, `DTEND:20250811T140000`, and `SUMMARY:Marcelo on-call`
- **THEN** the system produces one candidate event with start `2025-08-04T14:00:00`, end `2025-08-11T14:00:00`, and summary "Marcelo on-call" when the JVM default zone is UTC (business zone is `Europe/Amsterdam`)

#### Scenario: Invalid ICS returns 502
- **WHEN** the feed body is not a valid iCalendar document
- **THEN** the system returns `502 Bad Gateway` with a parse error message

#### Scenario: Valid but empty ICS returns empty lists
- **WHEN** the feed body is a valid iCalendar document but contains no `VEVENT` entries
- **THEN** the system returns `200 OK` with empty upcoming and past event lists

---

### Requirement: Parser operates offline
The parser SHALL NOT make outbound network requests while parsing; timezone registry updates and remote timezone lookups are disabled.

#### Scenario: Parse with unknown timezone makes no network calls
- **WHEN** the feed contains a `VTIMEZONE` or `TZID` the parser does not recognise
- **THEN** parsing completes without network egress

---

### Requirement: DATE-valued events are ignored
The system SHALL NOT produce candidate events for all-day (`VALUE=DATE`) `VEVENT`s; only timed events are supported.

#### Scenario: All-day event is ignored
- **WHEN** the feed contains an all-day `VEVENT` with `DTSTART;VALUE=DATE:20250804`
- **THEN** the event is omitted from the preview response

---

### Requirement: Backend returns sorted preview events
The system SHALL expose `GET /api/v1/calendar-feed/preview` returning candidate events grouped and sorted.

#### Scenario: Preview returns sorted upcoming and past events
- **WHEN** the feed contains events before and after today
- **THEN** the response lists upcoming events in ascending chronological order and past events within the last 12 months in descending chronological order

---

### Requirement: Frontend previews feed events as a list
The frontend SHALL render the preview response as a simple list of upcoming and past events, showing the period range, summary, and an import action.

#### Scenario: Render upcoming on-call period
- **WHEN** the preview response contains an upcoming event from `2025-08-04T16:00:00` to `2025-08-11T16:00:00`
- **THEN** the UI displays the period with the event summary and an import button

#### Scenario: No feed URL configured
- **WHEN** the home page preview section loads and the engineer profile has no `calendarFeedUrl`
- **THEN** the UI shows a prompt to add the incident.io feed URL in settings instead of an error

---

### Requirement: User imports a feed event as an on-call period
The frontend SHALL allow the user to create a tracked on-call period from a selected preview event by calling the existing `POST /api/v1/oncall-periods` endpoint.

#### Scenario: Import creates a period
- **WHEN** the user clicks "Import" on a feed event displayed as start `2025-08-04T16:00:00` and end `2025-08-11T16:00:00`
- **THEN** the frontend POSTs `{ "startDateTime": "2025-08-04T16:00:00", "endDateTime": "2025-08-11T16:00:00" }` and refreshes the preview list in place

#### Scenario: Import failure is surfaced
- **WHEN** the import POST fails because the period overlaps an existing tracked period
- **THEN** the frontend shows an error toast with the backend error message
