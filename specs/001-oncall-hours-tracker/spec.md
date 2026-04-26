# Feature Specification: On-Call Hours Tracker

**Feature Branch**: `001-oncall-hours-tracker`  
**Created**: 2026-04-25  
**Status**: Draft  
**Input**: User description: "We will work on a system that helps engineers that are on call to record their hours based on the working scheme we can find on confluence via rovo mcp in a page with title 'On-call and overtime registration'"

---

## Clarifications

### Session 2026-04-25

- Q: Does the system store submitted registrations historically, and does it support multiple engineers? → A: The system serves a single engineer only; no authentication or user accounts are required. Registrations are persisted locally so the engineer can review and retrieve past submissions.
- Q: Are overtime allowance rates fixed at build time or configurable by the engineer? → A: Configurable in-app through a dedicated configuration/settings area. The system ships with default values pre-loaded from the current WCA table. The engineer profile specifies the employee type (internal or external/consultant); each type has its own configurable rate set. Profile setup is a mandatory prerequisite before any hours can be registered.
- Q: Do external consultants follow the same on-call + overtime structure as internal employees, or a different scheme? → A: Same structure — on-call hours plus overtime entries with different rate values. The UI adapts to the engineer's profile type, hiding elements not applicable to their scenario to present a simplified view.
- Q: Can the engineer modify or delete saved registration summaries? → A: Full edit and delete at all times. Even entries on the reporting screen (the computed view) can be freely added, edited, or deleted by the engineer. The engineer always has final control over the data. The reporting screen offers quick-action popup shortcuts for convenience; primary editing flows live in their respective dedicated screens.
- Q: How is the engineer guided through mandatory setup on first launch? → A: A multi-step guided onboarding wizard on first launch. Steps include: profile setup (employee type, working schedule), display preferences (color scheme: dark / light / auto), and review of the pre-loaded Compensation Rate Table defaults. The engineer cannot access the main application until onboarding is complete. All settings configured during onboarding are also accessible and editable later through a dedicated Settings area (preferences, Compensation Rate Table, profile — subject to the profile-lock rule once registrations exist).

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Register On-Call Hours for a Week (Priority: P1)

An engineer finishes their on-call week and needs to submit the correct on-call hours to receive financial compensation. They provide their on-call period, their personal working schedule (which days are regular working days and at what times), and any holidays that fell within that period. The system calculates how many hours to register per day and at which rate (Monday–Saturday or Sunday/Holiday), respecting the 15-hour cap on regular working days and the 24-hour cap on non-working days.

**Why this priority**: On-call hour registration is the foundational use case and the primary source of compensation. Without this, no other part of the feature delivers value.

**Independent Test**: Can be fully tested by entering a week of on-call duty with a known schedule and verifying the system produces the correct per-day entries, rates, and hour caps — delivering a ready-to-submit registration summary.

**Acceptance Scenarios**:

1. **Given** an engineer was on-call from Monday 14:00 to the following Monday 14:00 with no holidays, **When** they enter their on-call period and their regular working schedule, **Then** the system produces one entry per day with the correct rate (Mon–Sat for weekdays, Sunday/Holiday for Sunday) and respects the 15-hour cap on their regular working days.
2. **Given** an engineer was on-call on a Sunday (not a working day), **When** they register their hours, **Then** the system assigns the `Sunday/Holiday` rate and allows up to 24 hours.
3. **Given** an engineer's on-call period spans two Mondays with different rates (e.g., the second Monday is a holiday), **When** they register their hours, **Then** the system splits the entries across both Mondays with the correct rate for each.
4. **Given** an engineer was on-call on a day they took off (regular working day), **When** they register, **Then** the system applies the 15-hour cap, assigns the Mon–Sat rate, and flags that a time-for-time arrangement should be discussed with their manager.
5. **Given** an engineer works part-time and does not work on certain weekdays, **When** they register on-call hours for those days, **Then** the system does not limit them to 15 hours on those days.

---

### User Story 2 - Record Overtime Hours for Incident Work (Priority: P2)

An engineer responds to one or more incidents during their on-call week outside of their normal working hours. For each incident they need to know the correct number of overtime hours to log, which allowance percentage(s) apply based on the time of day, and how to round up partial hours. The system produces the exact registration entries needed.

**Why this priority**: Overtime compensation is the second most common registration action and involves the most complex calculation rules (time-based allowance tiers, rounding, exclusions for normal hours and days off).

**Independent Test**: Can be fully tested by entering incident start/end times on various days and times and verifying the system produces correctly rounded overtime entries with the right allowance percentages — without needing on-call registration to be set up first.

**Acceptance Scenarios**:

1. **Given** an engineer worked on an incident on a Monday between 17:00 and 18:00 (outside normal hours), **When** they enter the incident, **Then** the system registers one overtime hour with no additional allowance.
2. **Given** an engineer worked on an incident between 04:00 and 05:15, **When** they enter the incident, **Then** the system rounds up to two overtime hours and creates two allowance entries at the applicable rate.
3. **Given** an incident spans multiple allowance time zones (e.g., 21:00–23:00 on Saturday), **When** the engineer enters the incident, **Then** the system splits the entry into separate allowance line items per time zone with their respective rates.
4. **Given** an incident occurred during the engineer's normal working hours, **When** they try to register it, **Then** the system rejects or excludes those hours and explains why no overtime applies.
5. **Given** an incident occurred on a holiday, **When** the engineer registers it, **Then** the system applies the holiday overtime rate.
6. **Given** an incident occurred on a day the engineer took as a day off, **When** they try to register overtime, **Then** the system blocks overtime registration for that day and informs the engineer that time-for-time applies instead.

---

### User Story 3 - Identify Time-for-Time Scenarios (Priority: P3)

An engineer sometimes handles incidents outside normal hours without being on the on-call schedule (e.g., a team member not in rotation), or on days off. In these cases, overtime pay does not apply and time-for-time should be used instead. The system helps the engineer identify these situations and understand what to do.

**Why this priority**: Misclassifying a time-for-time situation as overtime leads to incorrect registrations and potential HR issues. Flagging these cases proactively prevents errors.

**Independent Test**: Can be fully tested independently by entering an incident from an engineer who is not on-call (or is on a day off) and verifying the system correctly identifies the scenario, blocks overtime registration, and surfaces the time-for-time guidance.

**Acceptance Scenarios**:

1. **Given** an engineer handles an incident but was not on-call that week, **When** they record the incident, **Then** the system informs them that overtime pay is not applicable and time-for-time should be requested from their manager.
2. **Given** an on-call engineer works on a day off, **When** they attempt to register overtime for that day, **Then** the system flags the day as a day off, allows the on-call hours (capped at 15h at Mon–Sat rate), and blocks overtime — directing them to time-for-time.

---

### User Story 4 - Review and Export Registration Summary (Priority: P4)

After entering all on-call and overtime data for a week, the engineer sees a dedicated reporting screen with a structured overview of all registration entries. The screen mirrors the field labels used in the HR system so engineers can transfer values row by row without needing to interpret the policy. Entries can also be saved as a personal record.

**Why this priority**: Delivering a usable output is what closes the loop for the engineer. Without a clear summary the tool has no practical value.

**Independent Test**: Can be fully tested by completing a full on-call week with incidents and verifying the summary contains all expected line items grouped by type (on-call vs overtime), with correct dates, hours, and rate labels.

**Acceptance Scenarios**:

1. **Given** an engineer has entered a full week of on-call and overtime data, **When** they view the reporting screen, **Then** all computed entries are listed with date, hours, rate/allowance type clearly labelled and grouped.
2. **Given** the engineer has a summary ready, **When** they use the "Print / Save as PDF" action on the reporting screen, **Then** the browser's print dialog opens with a clean, print-optimised layout of all entries that can be saved as a PDF or printed as a personal record.
3. **Given** the engineer previously saved a registration summary, **When** they return to the system in a later session, **Then** the past summary is retrievable and displayed correctly.
4. **Given** the engineer spots an error in an entry on the reporting screen, **When** they use the quick-action popup to edit or delete it, **Then** the change is saved and the summary updates immediately — without navigating away from the reporting screen.
5. **Given** the engineer needs to make substantial edits (e.g., change an on-call period or add a new incident), **When** they navigate to the dedicated input screen for that data, **Then** the reporting screen reflects the updated calculation on their return.

---

### User Story 5 - First-Run Onboarding Setup (Priority: P1)

On first launch, the engineer is guided through a multi-step onboarding wizard before accessing any feature. The wizard collects all mandatory setup in a logical sequence: employee type and working schedule, display preferences (color scheme), and a review of pre-loaded rate defaults with the option to adjust them. The engineer cannot proceed to the main application until all required steps are completed.

**Why this priority**: Without a completed profile and rate configuration the system cannot produce correct calculations. Onboarding is a hard prerequisite and belongs in P1 alongside the core registration flow.

**Independent Test**: Can be fully tested by launching the application for the first time and verifying the wizard appears, progresses through all steps in order, prevents navigation to main screens until complete, and stores all entered data correctly.

**Acceptance Scenarios**:

1. **Given** the application is launched for the first time with no profile configured, **When** the engineer opens the app, **Then** the onboarding wizard is displayed and all other application screens are inaccessible.
2. **Given** the engineer is on the profile step, **When** they select their employee type (internal or external) and enter their working schedule, **Then** the system validates the input and enables navigation to the next step.
3. **Given** the engineer is on the preferences step, **When** they select a color scheme (dark, light, or auto), **Then** the UI immediately previews the selected theme.
4. **Given** the engineer is on the Compensation Rate Table step, **When** they review the pre-loaded WCA defaults, **Then** they can accept them as-is or adjust any value before proceeding.
5. **Given** the engineer completes all onboarding steps, **When** they confirm the final step, **Then** all settings are saved and the main application is unlocked.
6. **Given** the engineer abandons the wizard mid-way (e.g., closes the app), **When** they relaunch, **Then** the wizard resumes from the last incomplete step.

---

### Edge Cases

- What happens when an on-call shift spans the boundary between a regular day and a holiday (e.g., Sunday 22:00 to Monday 02:00, where Monday is Christmas)?
- How does the system handle an on-call week that contains multiple consecutive public holidays?
- What if the engineer enters an incident time range that overlaps both normal working hours and outside hours (e.g., 08:00–18:00 when they work 09:00–17:00)?
- What if an on-call shift is less than a full week (e.g., covering only two days for a colleague)?
- What if the engineer's normal working hours differ per day of the week?
- What if the engineer manually edits a computed entry to a value that conflicts with policy rules (e.g., exceeds the 24-hour cap)? The system saves the engineer's override without blocking, as the engineer has full control.
- What if the engineer resets the application after onboarding (e.g., wants to change employee type)? Behavior on data reset must be defined.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow the engineer to set up a profile specifying their employee type (internal employee or external consultant), regular working days, and daily start/end times. Profile setup MUST be completed before any on-call period or incident can be registered.
- **FR-001b**: The system MUST prevent the engineer from modifying their profile once at least one registration has been saved, to ensure consistency across recorded data.
- **FR-002**: The system MUST allow an engineer to enter an on-call period (start date/time and end date/time).
- **FR-003**: The system MUST allow an engineer to mark specific dates as public holidays within their on-call period.
- **FR-004**: The system MUST calculate on-call registration entries per day, applying the `Monday–Saturday` rate on weekdays and Saturdays, and the `Sunday/Holiday` rate on Sundays and public holidays.
- **FR-005**: The system MUST enforce the 15-hour maximum on days that are regular working days for the engineer, and allow up to 24 hours on non-regular working days.
- **FR-006**: The system MUST apply the 15-hour cap and Mon–Sat rate on days the engineer has taken as a day off (and flag that time-for-time should be discussed with the manager for the time worked).
- **FR-007**: The system MUST allow an engineer to log one or more incidents, each with a start time, end time, and date.
- **FR-008**: The system MUST calculate overtime entries from incident logs, rounding up partial hours to the next full hour.
- **FR-009**: The system MUST exclude incident hours that fall within the engineer's normal working hours from overtime calculations.
- **FR-010**: The system MUST split overtime entries when an incident spans multiple allowance time zones, producing a separate entry per zone with the applicable allowance percentage.
- **FR-011**: The system MUST apply holiday overtime rates when an incident occurs on a public holiday.
- **FR-012**: The system MUST block overtime registration for incidents on days the engineer has taken as a day off, and inform the engineer that time-for-time applies.
- **FR-013**: The system MUST identify and inform engineers when a situation qualifies for time-for-time rather than financial overtime compensation (not on-call, day off, etc.).
- **FR-014**: The system MUST produce a structured registration summary containing all on-call and overtime entries, with date, hours, and rate/allowance type per line item.
- **FR-015**: The system MUST provide a dedicated reporting screen that presents all calculated on-call and overtime entries in a structured, easy-to-follow format mirroring HR system fields. The reporting screen MUST offer quick-action shortcuts (e.g., inline popups) to edit or delete individual entries without leaving the screen. Full editing of on-call periods, incidents, and profile data is handled through their respective dedicated screens; the reporting screen shortcuts are a convenience, not the primary editing path.
- **FR-016**: The system MUST handle on-call shifts that span midnight and correctly attribute hours to the right calendar day.
- **FR-017**: The system MUST provide a Settings area where the engineer can view and update the Compensation Rate Table — the set of allowance percentages and on-call rates — separately for internal and external employee types.
- **FR-018**: The system MUST ship with the Compensation Rate Table pre-loaded with default values from the current WCA (Jumbo Logistics Works Council Agreement), so the engineer can use the system immediately without manual configuration.
- **FR-019**: The system MUST persist all registration summaries and allow the engineer to retrieve, edit, and delete any saved entry or summary at any time, with no restrictions on when edits can occur.
- **FR-020**: The system MUST adapt the registration and reporting UI based on the engineer's profile type, presenting only the fields, rates, and entry types relevant to that type (internal or external/consultant).
- **FR-021**: The system MUST present a multi-step onboarding wizard on first launch, collecting in order: (1) engineer profile and working schedule, (2) display preferences including color scheme (dark / light / auto), (3) review and optional adjustment of the pre-loaded Compensation Rate Table. The main application MUST remain inaccessible until the wizard is completed.
- **FR-022**: The system MUST resume an incomplete onboarding wizard from the last unfinished step if the engineer exits and relaunches before completing setup.
- **FR-023**: The system MUST provide a Settings area, accessible at any time after onboarding, where the engineer can update: display preferences (color scheme), the Compensation Rate Table, and their profile (subject to the profile-lock rule once registrations exist).
- **FR-024**: The system MUST apply the selected color scheme (dark / light / auto) across all screens immediately upon selection.

### Key Entities

- **Engineer Profile**: A single, persistent configuration representing the engineer's working schedule — employee type (internal **or** external/consultant, mutually exclusive), regular working days, daily start/end times. Only one profile exists in the system. The profile must be completed before any hours can be registered and cannot be changed once registrations exist.
- **Compensation Rate Table**: The configurable set of allowance percentages and on-call rates, maintained separately for internal and external employee types. Ships with pre-loaded WCA defaults and is editable through the Settings area.
- **User Preferences**: Application-level settings for the engineer, including color scheme (dark / light / auto). Collected during onboarding and editable at any time thereafter.
- **On-Call Period**: A continuous block of time during which the engineer is on-call, with a start date/time and end date/time.
- **On-Call Day Entry**: A single day within an on-call period with calculated hours to register, the applicable rate type, and any cap or flag applied.
- **Incident**: A discrete work event triggered by on-call responsibilities, with a start time, end time, and associated date.
- **Overtime Entry**: A calculated line item derived from an incident, containing hours, allowance percentage, and rate type — split per time zone where applicable.
- **Registration Summary**: The complete set of on-call day entries and overtime entries for a period, persisted for future retrieval and reference during HR system submission.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Engineers can produce a complete, submission-ready registration summary for a full on-call week in under 5 minutes.
- **SC-002**: The system correctly applies on-call hour caps (15h/24h) and rate types (Mon–Sat/Sunday/Holiday) for 100% of calculated entries.
- **SC-003**: Overtime entries are calculated with correct rounding, allowance percentages, and time-zone splits, matching expected results for all documented example scenarios from the policy.
- **SC-004**: Time-for-time scenarios are correctly identified and distinguished from overtime-eligible situations in 100% of applicable cases.
- **SC-005**: Engineers report a reduction in time spent manually interpreting the on-call policy when filling in their registration.
- **SC-006**: Zero registration errors attributable to incorrect rate or hour-cap application by engineers using the system.

---

## Assumptions

- The system supports both internal employees and external consultants as selectable employee types; the single engineer profile is set to one type only. Each type has distinct configurable rates and an adapted UI that hides inapplicable elements.
- The system persists all registration summaries locally so the engineer can retrieve and review past submissions at any time.
- The Compensation Rate Table (allowance percentages and on-call rates from the Jumbo Logistics WCA) is pre-loaded as default configuration; engineers can update these values in-app via the Settings area when rates change (e.g., on WCA renewal).
- Engineers submit their final registrations manually into the HR system; this tool does not automate the submission but provides a dedicated reporting screen that maps calculated entries to HR system fields.
- On-call shifts follow a weekly rotation; the system is designed around per-week registration cycles.
- The system assumes a standard Dutch public holiday calendar; engineers can override by marking individual dates as holidays.
- The `Non-basic obligatory Saturday` category from the WCA applies for all engineers using this system.
- The frontend is mobile-first. All screens are designed and tested for small-screen touch devices first, then scaled up to desktop. Desktop browsers are fully supported.
