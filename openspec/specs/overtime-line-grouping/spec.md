# Capability: Overtime Line Grouping

## Purpose
Group flat overtime entry lines by date and option to provide aggregated overtime summaries in on-call period reports.

## ADDED Requirements

### Requirement: Overtime lines grouped by date and option
The system SHALL group flat overtime entry lines produced by `CalculateOvertimeEntriesUseCase` into one `GroupedOvertimeEntryResponse` per unique `(date, isAllowanceEntry, allowancePercentage)` key before including them in `OnCallPeriodReportResponse.overtimeLines`.

#### Scenario: Single incident, two entries on same date and option
- **WHEN** two `OvertimeEntry` segments for the same incident share the same `date`, `isAllowanceEntry=false`, and `allowancePercentage=null`
- **THEN** the grouped result contains exactly one entry for that date/option with `hours` equal to the sum of the two per-segment `overtimeHours` values

#### Scenario: Two incidents produce entries for the same date and option
- **WHEN** two different incidents both produce an `isAllowanceEntry=false` entry on the same `date`
- **THEN** the grouped result contains exactly one entry for that date/option with `hours` equal to the sum of both incidents' hours and `incidentIds` containing both incident IDs in insertion order

#### Scenario: Same date, different allowance percentages produce separate rows
- **WHEN** two entries share the same `date` and `isAllowanceEntry=true` but have different `allowancePercentage` values (e.g., 25 vs 50)
- **THEN** the grouped result contains two separate entries for that date, one per distinct `allowancePercentage`

#### Scenario: Midnight-spanning incident produces entries on two dates
- **WHEN** an incident spans midnight producing an `OvertimeEntry` on date D with hours H1 and another on date D+1 with hours H2 (same option)
- **THEN** the grouped result contains one entry for date D with `hours = H1` and one entry for date D+1 with `hours = H2`

#### Scenario: No overtime entries produces empty grouped list
- **WHEN** all incidents in the period fall entirely within working hours (no `OvertimeEntry` objects produced)
- **THEN** `OnCallPeriodReportResponse.overtimeLines` is an empty list

### Requirement: Grouped hours are summed from already-rounded per-segment values
The system SHALL sum `overtimeHours` (for non-allowance entries) or `allowanceHours` (for allowance entries) from each contributing segment without applying any additional rounding after the sum.

#### Scenario: Two 1-hour ceiling-rounded segments sum to 2
- **WHEN** two segments on the same date/option each round up to 1 hour
- **THEN** the grouped entry reports `hours = 2`, not 1

### Requirement: Grouped record carries contributing incident IDs
Each `GroupedOvertimeEntryResponse` SHALL include a deduplicated, insertion-ordered list of incident IDs that contributed segments to that group.

#### Scenario: Single incident contributes all segments
- **WHEN** all segments in a group originate from incident ID 42
- **THEN** `GroupedOvertimeEntryResponse.incidentIds` is `[42]`

#### Scenario: Multiple incidents contribute to the same group
- **WHEN** incident 10 and incident 20 both produce entries for the same date/option
- **THEN** `GroupedOvertimeEntryResponse.incidentIds` is `[10, 20]` (insertion order)

### Requirement: API response shape uses grouped overtime lines
The `GET /api/v1/oncall-periods/{id}/report` endpoint SHALL return `overtimeLines` as an array of grouped overtime entry objects. Each object SHALL contain: `date` (ISO-8601 LocalDate), `isAllowanceEntry` (boolean), `allowancePercentage` (decimal string or null), `hours` (decimal string), and `incidentIds` (array of integers). The fields `incidentId`, `incidentName`, `timeFrom`, and `timeTo` SHALL NOT be present on grouped entries.

#### Scenario: Report with one grouped overtime entry
- **WHEN** the report is fetched for a period with incidents that produce grouped overtime entries
- **THEN** each element of `overtimeLines` contains `date`, `isAllowanceEntry`, `allowancePercentage`, `hours`, and `incidentIds`, and does not contain `timeFrom` or `timeTo`

#### Scenario: Non-allowance grouped entry has null allowancePercentage
- **WHEN** a grouped entry is for `isAllowanceEntry=false`
- **THEN** `allowancePercentage` in the response is `null`
