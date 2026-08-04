## 1. Core Implementation

- [x] 1.1 Load `EngineerProfile.workingDays()` in `CalculateOvertimeEntriesUseCase` and fall back to a `DEFAULT_WORKING_DAYS` constant (Mon–Fri) when the profile is absent.
- [x] 1.2 Compute `isWorkingDay` as `workingDays.contains(incidentDate.getDayOfWeek()) && !holidayDates.contains(incidentDate)`.
- [x] 1.3 Refactor `computeOvertimeSegments` to receive `isWorkingDay` instead of `isHoliday` and return the full incident interval when the day is not a working day.
- [x] 1.4 Keep the existing `overtimeDayType` resolution and allowance-rate lookup unchanged.

## 2. Tests

- [x] 2.1 Add a unit test for a Saturday 10:00–11:00 incident with default Mon–Fri profile → full 1 h overtime segment.
- [x] 2.2 Add a unit test for the reported Saturday 08:30–14:40 incident → 7 h overtime segment with 50 % allowance for the overlapping slots.
- [x] 2.3 Add a unit test proving a profile with Saturday in `workingDays` still applies the working-hours filter and throws `IncidentDuringWorkingHoursException` for a Saturday 10:00–11:00 incident.
- [x] 2.4 Add a unit test for missing profile with a Saturday incident → full overtime (fallback to Mon–Fri).
- [x] 2.5 Verify existing tests still pass, especially Sunday and holiday full-overtime tests.

## 3. Verification

- [x] 3.1 Run `./mvnw test -Dtest=CalculateOvertimeEntriesUseCaseTest`.
- [x] 3.2 Run `./mvnw test` for the backend.
- [x] 3.3 Run `./mvnw spotless:apply` to fix formatting.
- [x] 3.4 Run `./mvnw clean package` to ensure full build passes.
