# Capability: Incident Overtime Calculation

## Purpose

Define how an incident's time range is converted into overtime entries and allowance entries based on the engineer's working days, working hours, day type, and configured holidays.

## ADDED Requirements

### Requirement: Overtime segments exclude only configured working hours on working days

The system SHALL treat an incident date as a non-working day when the date's day of week is not contained in the engineer's `workingDays`, or when the date is configured as a holiday for the on-call period. On a non-working day, the entire incident interval SHALL count as overtime. On a working day, only the portions of the incident that fall outside the configured `workStartTime` and `workEndTime` SHALL count as overtime.

#### Scenario: Saturday incident during former weekday working hours is fully overtime

- **WHEN** an incident runs from 10:00 to 11:00 on a Saturday
- **AND** the engineer profile's `workingDays` are Monday–Friday
- **THEN** the system produces one overtime segment of 10:00–11:00 (60 minutes, rounded up to 1 hour)

#### Scenario: Saturday incident before and during working hours is fully overtime

- **WHEN** an incident runs from 08:30 to 14:40 on a Saturday
- **AND** the engineer profile's `workingDays` are Monday–Friday
- **THEN** the system produces one overtime segment of 08:30–14:40 (6 hours 10 minutes, rounded up to 7 hours)

#### Scenario: Weekday incident inside working hours produces no overtime

- **WHEN** an incident runs from 10:00 to 11:00 on a Tuesday
- **AND** the engineer profile's `workingDays` include Tuesday
- **THEN** the system throws `IncidentDuringWorkingHoursException`

#### Scenario: Profile with Saturday as working day applies working-hours filter

- **WHEN** an incident runs from 10:00 to 11:00 on a Saturday
- **AND** the engineer profile's `workingDays` include Saturday
- **THEN** the system throws `IncidentDuringWorkingHoursException`

#### Scenario: Missing profile defaults to Monday–Friday working days

- **WHEN** an incident runs from 10:00 to 11:00 on a Saturday
- **AND** no engineer profile exists
- **THEN** the system produces one overtime segment of 10:00–11:00

### Requirement: Allowance rate lookup remains based on calendar day type

The system SHALL continue to select `OVERTIME_ALLOWANCE` rates using `OvertimeDayType.SUNDAY_HOLIDAY` for Sundays and configured holidays, `OvertimeDayType.SATURDAY` for Saturdays, and `OvertimeDayType.WEEKDAY` for all other days, regardless of whether the engineer profile marks that day as a working day.

#### Scenario: Saturday overtime segment uses Saturday allowance rates

- **WHEN** a Saturday overtime segment overlaps the 08:00–09:00 allowance slot
- **THEN** the allowance entry uses the `SATURDAY` 08:00–09:00 rate (50 % in the seeded WCA table)

#### Scenario: Holiday overtime segment uses Sunday/holiday allowance rates

- **WHEN** a configured holiday on a weekday produces a full overtime segment
- **THEN** the allowance entry uses the `SUNDAY_HOLIDAY` rate
