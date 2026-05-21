## Why

The MyHR overtime section of the on-call period report emits one row per incident segment per option, producing verbose output that HR staff must manually reconcile. Grouping by `(date, isAllowanceEntry, allowancePercentage)` condenses these into one row per date/option combination, matching the granularity that MyHR actually needs.

## What Changes

- **New**: `GroupOvertimeLinesUseCase` — pure grouping step that receives a flat list of `ReportOvertimeEntryResponse` and emits a grouped list of `GroupedOvertimeEntryResponse`.
- **New**: `GroupedOvertimeEntryResponse` record — replaces `ReportOvertimeEntryResponse` in `OnCallPeriodReportResponse`. Carries summed hours, the grouping key fields, and a list of contributing incident IDs for auditability.
- **BREAKING**: `OnCallPeriodReportResponse.overtimeLines` changes element type from `ReportOvertimeEntryResponse` to `GroupedOvertimeEntryResponse`. The frontend is the sole consumer; no versioning needed.
- `GenerateOnCallPeriodReportUseCase` is updated to call `GroupOvertimeLinesUseCase` after collecting raw overtime entries.
- Frontend `report.vue` and `types/report.ts` are updated to consume the new grouped shape (no logic change; the grouping is done by the backend).

## Capabilities

### New Capabilities
- `overtime-line-grouping`: Group flat overtime entry lines by `(date, isAllowanceEntry, allowancePercentage)`, summing hours from all contributing segments/incidents, for presentation in the MyHR report.

### Modified Capabilities
<!-- No existing spec-level requirements change. The report API shape change is a new capability, not a modification of an existing spec. -->

## Impact

- **Backend**: New use case class + request/response records in `usecase/`; `GenerateOnCallPeriodReportUseCase` delegates to it; `OnCallPeriodReportResponse` field type changes.
- **API**: `GET /api/v1/oncall-periods/{id}/report` — `overtimeLines` array element shape changes (breaking).
- **Frontend**: `app/types/report.ts` updated; `app/pages/oncall/[id]/report.vue` overtime table mapping updated.
- **Tests**: New unit test class for `GroupOvertimeLinesUseCase`; `GenerateOnCallPeriodReportUseCaseTest` updated to assert on grouped output.
