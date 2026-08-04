## ADDED Requirements

### Requirement: Engineer can store a personal calendar feed URL
The engineer profile SHALL include an optional `calendarFeedUrl` field. The URL SHALL be persisted with the profile on create/update and returned on read.

#### Scenario: Create profile with calendar feed URL
- **WHEN** an engineer creates a profile with `calendarFeedUrl = "https://app.incident.io/api/schedule_feeds/abc123"`
- **THEN** the profile is saved and the response includes the same URL

#### Scenario: Update profile calendar feed URL
- **WHEN** an engineer submits a full profile update that includes `calendarFeedUrl = "https://app.incident.io/api/schedule_feeds/xyz789"`
- **THEN** the URL is updated and all other profile fields are set to the values provided in the request

#### Scenario: Empty calendar feed URL clears the stored value
- **WHEN** an engineer updates a profile with `calendarFeedUrl = ""`
- **THEN** the stored `calendarFeedUrl` is set to null

#### Scenario: Missing calendar feed URL preserves the stored value
- **WHEN** an engineer submits a full profile update that does not include `calendarFeedUrl`
- **THEN** any existing `calendarFeedUrl` is preserved

#### Scenario: Updating other profile fields preserves the feed URL
- **WHEN** an engineer submits a full profile update that changes `workStartTime` but leaves `calendarFeedUrl` populated with its existing value
- **THEN** the existing `calendarFeedUrl` is preserved unchanged

#### Scenario: Invalid calendar feed URL is rejected
- **WHEN** an engineer submits a profile update with `calendarFeedUrl = "not-a-url"`
- **THEN** the system returns `400 Bad Request` with a message indicating the URL is invalid

#### Scenario: Non-allowed host is rejected on save
- **WHEN** an engineer submits a profile update with `calendarFeedUrl = "https://evil.example.com/feed.ics"`
- **THEN** the system returns `400 Bad Request` with a message indicating the URL host is not allowed
