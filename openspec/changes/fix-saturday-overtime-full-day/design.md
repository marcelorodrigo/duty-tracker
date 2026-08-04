## Context

`CalculateOvertimeEntriesUseCase` currently subtracts the standard 09:00–17:00 working-hours window from every incident unless the date is a Sunday or a configured holiday. The `EngineerProfile` already stores `workingDays` (seeded as Mon–Fri) and `CalculateOnCallDayEntriesUseCase` already uses it to classify days for standby calculations, but the overtime use case ignores it. This inconsistency causes Saturday incidents that occur during weekday working hours to be under-reported as overtime.

After this change, whether a day is treated as working is driven solely by `workingDays` and configured holidays. Sunday follows the same rule as any other day: it is working only if it is in `workingDays` and not a configured holiday. The existing `SUNDAY_HOLIDAY` allowance-rate table still applies to Sundays and configured holidays.

## Goals / Non-Goals

**Goals:**
- Make overtime segment calculation respect `EngineerProfile.workingDays()`.
- Keep allowance-rate lookup based on calendar day type (Saturday/Sunday/holiday/weekday).
- Maintain a sensible fallback when no profile exists.
- Provide test coverage for the Saturday-during-working-hours scenario.

**Non-Goals:**
- Changing the rounding logic (still ceiling to whole hours per segment).
- Changing standby/on-call day calculations.
- Changing API contracts or response shapes.
- Altering the seeded WCA allowance-rate table.

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Working-day source | `EngineerProfile.workingDays()` | Already used for standby; makes the two calculation paths consistent. |
| Saturday handling | Derived from profile | Default profile is Mon–Fri, so Saturday becomes a non-working day without hardcoding Saturday. Supports future configurations. |
| Holiday interaction | Configured holiday overrides working day | `isWorkingDay = workingDays.contains(day) && !holidayDates.contains(day)`. Sunday is treated like any other day for working-day classification, but still maps to `SUNDAY_HOLIDAY` allowance rates. |
| Missing-profile fallback | Mon–Fri default (`Set.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)`) | Matches the seeded default profile and the user's stated preference. |
| Allowance rate lookup | Unchanged | Still map Sunday/holiday → `SUNDAY_HOLIDAY`, Saturday → `SATURDAY`, else `WEEKDAY`. |
| computeOvertimeSegments signature | Replace `isHoliday` with `isWorkingDay` | The method cares whether the day is a working day, not specifically a holiday. |

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Existing tests that assume Saturday is a working day break | Update tests to reflect the new correct behavior; add a test for a profile that includes Saturday as working. |
| A user with a non-default profile that includes Saturday will get different behavior | This is desirable configurability; the fallback only applies when no profile exists. |
| Holiday on a Saturday will still use `SUNDAY_HOLIDAY` allowance rates | Confirmed correct by the existing seed data and WCA table. |

## Migration Plan

Not applicable. No data migration or API migration is required; deploy the new backend version.

## Open Questions

None.
