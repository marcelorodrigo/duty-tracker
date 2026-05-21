## 1. Backend — New Records

- [x] 1.1 Create `GroupedOvertimeEntryResponse` record in `usecase/response/oncall/` with fields: `LocalDate date`, `boolean isAllowanceEntry`, `BigDecimal allowancePercentage`, `BigDecimal hours`, `List<Long> incidentIds`
- [x] 1.2 Create `GroupOvertimeLinesRequest` record in `usecase/request/oncall/` wrapping `List<ReportOvertimeEntryResponse> entries`
- [x] 1.3 Create `GroupedOvertimeLinesResponse` record in `usecase/response/oncall/` wrapping `List<GroupedOvertimeEntryResponse> entries`

## 2. Backend — GroupOvertimeLinesUseCase

- [x] 2.1 Create `GroupOvertimeLinesUseCase` in `usecase/oncall/` implementing `UseCase<GroupOvertimeLinesRequest, GroupedOvertimeLinesResponse>`
- [x] 2.2 Implement grouping using `LinkedHashMap` keyed on a private `record GroupKey(LocalDate date, boolean isAllowanceEntry, BigDecimal allowancePercentage)`
- [x] 2.3 For each entry: accumulate `overtimeHours` (non-allowance) or `allowanceHours` (allowance) into the group's `hours`; add `incidentId` to the group's `incidentIds` list (skip duplicates)
- [x] 2.4 Annotate with `@Service`; use `@RequiredArgsConstructor` (no collaborators needed — pure transformation)

## 3. Backend — Wire into GenerateOnCallPeriodReportUseCase

- [x] 3.1 Add `GroupOvertimeLinesUseCase groupOvertimeLines` as a constructor dependency in `GenerateOnCallPeriodReportUseCase`
- [x] 3.2 After the incident loop, call `groupOvertimeLines.execute(new GroupOvertimeLinesRequest(overtimeLines))` and pass the result's `entries()` to `OnCallPeriodReportResponse`
- [x] 3.3 Change `OnCallPeriodReportResponse` record's `overtimeLines` field type from `List<ReportOvertimeEntryResponse>` to `List<GroupedOvertimeEntryResponse>`

## 4. Backend — Tests

- [x] 4.1 Create `GroupOvertimeLinesUseCaseTest` in `usecase/oncall/` covering: single-incident grouping, multi-incident same key, same date different percentages, empty input, midnight-span two dates
- [x] 4.2 Update `GenerateOnCallPeriodReportUseCaseTest`: inject mock `GroupOvertimeLinesUseCase`; update assertions to check `GroupedOvertimeEntryResponse` fields (date, hours, incidentIds) instead of `ReportOvertimeEntryResponse` fields
- [x] 4.3 Run `./mvnw spotless:apply` then `./mvnw clean package` to confirm all tests pass

## 5. Frontend — Type Definitions

- [x] 5.1 In `app/types/report.ts`: add `GroupedOvertimeEntryResponse` interface with fields `date: string`, `isAllowanceEntry: boolean`, `allowancePercentage: string | null`, `hours: string`, `incidentIds: number[]`
- [x] 5.2 Update `OnCallPeriodReportResponse` interface: change `overtimeLines: ReportOvertimeEntryResponse[]` to `overtimeLines: GroupedOvertimeEntryResponse[]`
- [x] 5.3 Remove (or retain if used elsewhere) `ReportOvertimeEntryResponse` interface from `report.ts`

## 6. Frontend — Report Page

- [x] 6.1 In `app/pages/oncall/[id]/report.vue`: update `OvertimeRow` type — remove `incident` and `time` columns; keep `date`, `plan`, `option`, `hours`
- [x] 6.2 Update `overtimeColumns` to remove `incident` and `time` column definitions
- [x] 6.3 Update the `report.overtimeLines.map(...)` expression: use `e.hours` directly (no `isAllowanceEntry` branch needed), derive `option` label from `e.isAllowanceEntry` and `e.allowancePercentage`, remove `e.incidentName` and time range references
- [x] 6.4 Update `overtimeOptionLabel` helper (or inline) to accept `{ isAllowanceEntry: boolean, allowancePercentage: string | null }` — shape is unchanged but now sourced from `GroupedOvertimeEntryResponse`
- [x] 6.5 Run `pnpm typecheck` from `frontend/` to confirm no type errors

## 7. Frontend — Tests

- [x] 7.1 Update `test/nuxt/OnCallReportPage.test.ts`: replace `overtimeLines` in `mockReport` with `GroupedOvertimeEntryResponse` shape — fields `date`, `isAllowanceEntry`, `allowancePercentage`, `hours`, `incidentIds`; remove `incidentId`, `incidentName`, `timeFrom`, `timeTo`, `overtimeHours`, `allowanceHours` from fixtures
- [x] 7.2 Add test: overtime table renders exactly 4 columns (Date, Plan, Option, Hours) — assert no "Incident" or "Time" column header is present
- [x] 7.3 Add test: non-allowance grouped entry renders option label "Overtime hours" and displays `hours` value
- [x] 7.4 Add test: allowance grouped entry renders option label "{percentage}% allowance" and displays `hours` value
- [x] 7.5 Retain (and verify still pass) the existing row-click strikethrough toggle tests — they are unaffected by the shape change but depend on the updated fixture
- [x] 7.6 Run `pnpm test:nuxt` from `frontend/` to confirm all nuxt tests pass
