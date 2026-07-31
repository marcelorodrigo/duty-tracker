## Why

Engineers currently create every on-call period manually in Duty Tracker, even though incident.io already publishes their personal schedule as an ICS feed. Pasting the feed URL once and previewing events from it removes redundant data entry, reduces copy/paste errors, and makes the tool stay in sync with the real schedule without reconstructing weeks from memory.

## What Changes

- Add an optional `calendarFeedUrl` field to the engineer profile.
- Add a backend endpoint that fetches the configured ICS feed server-side, parses it, and returns a list of on-call events.
- Add a list on the home page that shows upcoming and past on-call events from the feed (no calendar grid).
- Let the user promote any feed event to a tracked Duty Tracker on-call period with one click, using the existing create-period API.
- Keep parsed calendar events ephemeral; only the feed URL and explicitly imported periods are persisted.

## Capabilities

### New Capabilities
- `ics-calendar-feed`: Fetch, parse, and preview personal on-call events from an external ICS feed and import them as tracked on-call periods.

### Modified Capabilities
- `engineer-profile`: Profile requests and responses now include an optional `calendarFeedUrl` field for storing the personal incident.io schedule feed.

## Impact

- Backend: new HTTP client dependency to fetch external feeds, new ICS parser dependency, new gateway/controller/usecase/response records, schema migration for profile URL.
- Frontend: profile form extended for URL input, new page/section for previewing feed events, new composables for fetching and importing events.
- API contract: `openapi.yaml` updated for profile and new `/api/v1/calendar-feed/preview` endpoint.
- CORS/external fetch: parsing and fetching happen server-side because incident.io does not allow browser-side CORS fetches.
