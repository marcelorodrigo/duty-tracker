## Context

`GenerateOnCallPeriodReportUseCase` currently iterates over all incidents in an on-call period and, for each incident, calls `CalculateOvertimeEntriesUseCase`. The latter splits multi-day incidents at midnight and produces one `OvertimeEntry` per time-segment per compensation-rate band — two entries per band (one `isAllowanceEntry=false`, one `isAllowanceEntry=true`). These are mapped 1:1 to `ReportOvertimeEntryResponse` objects and returned in `OnCallPeriodReportResponse.overtimeLines`.

A 2-hour incident spanning midnight therefore produces 4 or more rows: one "Overtime hours" row for each calendar day, plus corresponding allowance rows. When multiple incidents share the same date, the rows multiply further. HR staff must manually sum rows by date/option before entering values in MyHR.

The midnight split logic in `CalculateOvertimeEntriesUseCase.splitAtMidnight` is correct and must not change. Hours are already ceiling-rounded to whole hours per segment (minimum 1 hour) before this grouping layer sees them.

## Goals / Non-Goals

**Goals:**
- Collapse flat `ReportOvertimeEntryResponse` list into `GroupedOvertimeEntryResponse` list keyed by `(date, isAllowanceEntry, allowancePercentage)`.
- Sum already-rounded hours across all contributing segments and incidents for each key.
- Carry contributing incident IDs in each grouped record for auditability.
- Update `OnCallPeriodReportResponse.overtimeLines` to use the new grouped type.
- Update frontend type definitions and overtime table mapping to consume the new shape.

**Non-Goals:**
- Changing rounding rules (sum of already-rounded per-segment hours; no re-round after grouping).
- Changing `CalculateOvertimeEntriesUseCase` or `splitAtMidnight`.
- Sorting or display of grouping key fields (backend returns natural ordering from `LinkedHashMap`; frontend renders as received).
- Programmatic MyHR submission.
- Versioning the API endpoint; the frontend is the sole consumer.

## Decisions

### D1: Extract grouping into `GroupOvertimeLinesUseCase`, not inline in `GenerateOnCallPeriodReportUseCase`

`GenerateOnCallPeriodReportUseCase` is already responsible for loading the period, collecting incidents, calling `CalculateOvertimeEntriesUseCase` per incident, and assembling the report. Adding grouping logic inline would violate single responsibility and make unit testing the grouping algorithm harder. A dedicated use case isolates the algorithm and can be tested without any mocks (pure transformation).

**Alternative considered:** inline reduce inside `GenerateOnCallPeriodReportUseCase`. Rejected because it couples the grouping rule (key definition, summation) to the orchestration loop, complicating future changes to either.

### D2: Grouping key is `(LocalDate date, boolean isAllowanceEntry, BigDecimal allowancePercentage)`

`isAllowanceEntry` alone is insufficient to differentiate rows when multiple OVERTIME_ALLOWANCE rates exist for different time bands on the same date (e.g., 25% for 06:00–22:00 and 50% for 22:00–06:00 on a Saturday). Including `allowancePercentage` in the key handles this correctly. `allowancePercentage` is `null` for non-allowance entries, which is a valid key component in Java `LinkedHashMap` with a record key.

**Note:** `incidentName` is deliberately excluded from the grouping key. The grouped row represents cross-incident sums; individual incident names are surfaced via the `incidentIds` list.

### D3: `GroupedOvertimeEntryResponse` is a new record; `ReportOvertimeEntryResponse` is retained unchanged

`ReportOvertimeEntryResponse` is the internal flat representation produced by `GenerateOnCallPeriodReportUseCase` before grouping. It is not serialized to the API response after this change. Retaining it avoids modifying the `CalculateOvertimeEntriesUseCase` response path and keeps internal plumbing intact. The new `GroupedOvertimeEntryResponse` record is the API-visible grouped shape.

**Fields of `GroupedOvertimeEntryResponse`:**
```
LocalDate date
boolean isAllowanceEntry
BigDecimal allowancePercentage  // null for non-allowance rows
BigDecimal hours                // sum of already-rounded per-segment hours
List<Long> incidentIds          // contributing incident IDs, deduplicated, insertion-ordered
```

`timeFrom` / `timeTo` are dropped from the grouped record — they are segment-specific and meaningless after merging multiple segments.

### D4: Grouping implementation uses `LinkedHashMap<GroupKey, GroupedOvertimeEntryResponse>` with record key

A `record` for the key (`GroupKey(LocalDate, boolean, BigDecimal)`) provides correct `equals`/`hashCode` without manual implementation. `LinkedHashMap` preserves insertion order (date-ascending within the flat list produced by `GenerateOnCallPeriodReportUseCase`, since incidents are iterated sequentially and each incident's entries are date-ordered by `splitAtMidnight`).

**Alternative considered:** `Collectors.groupingBy` with a `Stream`. Viable but produces a `HashMap` (unordered) and requires a merge step for the `incidentIds` list. The explicit loop over `LinkedHashMap` is slightly more readable for this case.

### D5: `GroupOvertimeLinesUseCase` receives `List<ReportOvertimeEntryResponse>` and returns `List<GroupedOvertimeEntryResponse>`

This keeps the use case interface consistent with the existing `UseCase<Req, Res>` functional interface pattern. The request record `GroupOvertimeLinesRequest` wraps the flat list. The response record `GroupedOvertimeLinesResponse` wraps the grouped list. This is consistent with `OvertimeEntriesResponse` wrapping `List<OvertimeEntryResponse>`.

### D6: `GenerateOnCallPeriodReportUseCase` delegates grouping as a final step

After the existing incident loop populates `overtimeLines: List<ReportOvertimeEntryResponse>`, the use case calls `groupOvertimeLines.execute(new GroupOvertimeLinesRequest(overtimeLines))` and uses the result's grouped list in the `OnCallPeriodReportResponse` constructor. The `OnCallPeriodReportResponse` record's `overtimeLines` field changes type from `List<ReportOvertimeEntryResponse>` to `List<GroupedOvertimeEntryResponse>`.

## Risks / Trade-offs

- **Hours summation is additive over already-rounded values.** If two 31-minute segments on the same date/option both round up to 1 hour, the grouped total is 2 hours, not 1 hour (which a fresh ceiling of 62 minutes would give). This is the intended behavior per the brief ("sum the already-rounded per-segment hours") but HR staff should be aware that grouped totals can exceed what a single-segment rounding would produce.
- **`allowancePercentage` scale equality in map key.** `BigDecimal("25")` and `BigDecimal("25.00")` are not `equals` despite representing the same value. The existing code stores `allowancePercentage` from `CompensationRate.percentage()` without scale changes, so the same `CompensationRate` will always produce the same `BigDecimal` instance across calls. This is safe as long as `CompensationRate` is not constructed with varying scales for the same logical rate. Document as a constraint.
- **Breaking API change accepted.** Frontend is the sole consumer. No backward-compatibility wrapper is needed.

### D7: Frontend test strategy — update existing nuxt test, no new unit test file

`test/nuxt/OnCallReportPage.test.ts` already exists and mounts `report.vue` against a `mockReport` fixture that uses the old `ReportOvertimeEntryResponse` shape (`incidentId`, `incidentName`, `timeFrom`, `timeTo`, `overtimeHours`, `allowanceHours`). After this change that fixture will fail TypeScript compilation and the mock will no longer match the live `OnCallPeriodReportResponse` type — so the test file **must** be updated rather than supplemented with a separate file.

The updated test covers:
1. **Mock fixture update** — replace `overtimeLines` entries with `GroupedOvertimeEntryResponse` shape (`date`, `isAllowanceEntry`, `allowancePercentage`, `hours`, `incidentIds`).
2. **Column rendering** — assert the overtime table renders 4 columns (`date`, `plan`, `option`, `hours`) and does **not** render an `Incident` or `Time` column.
3. **Hours display** — assert that `e.hours` is rendered directly (not conditional on `isAllowanceEntry`).
4. **Option label** — assert that a non-allowance row shows "Overtime hours" and an allowance row shows e.g. "50% allowance".
5. **Row toggle** — existing click-to-strikethrough tests are retained; only the fixture changes.

A separate `test/unit/` file for `types/report.ts` is not warranted — TypeScript interfaces carry no runtime logic and `pnpm typecheck` already validates them statically. The `test/nuxt/` project runs in the nuxt + happy-dom environment, which is correct for mounting `report.vue`.

## Open Questions

_None — all questions from the Brainstorm Brief are resolved above (D2 covers the grouping key; D3 covers incident ID list; DST and multi-midnight are out of scope; rounding is D1/context; plan name is not part of the grouped record)._
