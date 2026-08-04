## Why

Saturday on-call incidents are currently filtered by the standard weekday working-hours window (09:00–17:00), so only the small slice before 09:00 counts as overtime. For teams that follow the Jumbo Logistics WCA “non-basic obligatory Saturday” scheme and do not work Saturdays, the whole Saturday incident should count as overtime. The bug was reported for a 6 h 10 min Saturday incident that produced only 1 h base + 1 h 50 % allowance instead of the full 7 h.

## What Changes

- Modify `CalculateOvertimeEntriesUseCase` to determine whether the incident date is a *working day* from `EngineerProfile.workingDays()` instead of treating only Sunday and configured holidays as full days off.
- When the incident date is not a working day, the entire incident interval is returned as overtime, while `OvertimeDayType` (and therefore allowance-rate lookup) remains based on the calendar day/holiday status.
- Add a default Mon–Fri working-days fallback when no engineer profile exists, matching the seeded default profile.
- Add/update unit tests in `CalculateOvertimeEntriesUseCaseTest` for Saturday incidents during former "working hours" and for profile configurability.
- No API, database schema, or frontend changes are required.

## Capabilities

### New Capabilities

- `incident-overtime-calculation`: Defines how incident time is classified as overtime based on the engineer's configured working days, standard working hours, day-type allowance rates, and holidays.

### Modified Capabilities

- None. The `engineer-profile` capability's existing requirement (engineers can configure working days) is unchanged; this change consumes that existing configuration.

## Impact

- Affects `backend/src/main/java/.../usecase/incident/CalculateOvertimeEntriesUseCase.java`.
- Affects `backend/src/test/java/.../usecase/incident/CalculateOvertimeEntriesUseCaseTest.java`.
- Reported on-call period reports will now show the full Saturday overtime hours.
