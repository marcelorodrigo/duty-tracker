# API Contract: On-Call Hours Tracker

**Phase**: 1 | **Date**: 2026-04-25 | **Plan**: [../plan.md](../plan.md)

---

## Overview

- **Base URL**: `http://localhost:8080/api/v1`
- **Format**: JSON (`Content-Type: application/json`)
- **Auth**: None (single-user, local tool)
- **Error format**: RFC 7807 `ProblemDetail`
- **CORS**: `http://localhost:3000` allowed (Nuxt dev server)

### Standard Error Response

```json
{
  "type": "https://dutytracker/errors/profile-locked",
  "title": "Profile is locked",
  "status": 409,
  "detail": "Engineer profile cannot be modified after registrations exist."
}
```

Common HTTP status codes used:
- `200 OK` — successful read or update
- `201 Created` — successful resource creation
- `204 No Content` — successful deletion
- `400 Bad Request` — validation failure
- `404 Not Found` — resource not found
- `409 Conflict` — business rule violation (profile locked, duplicate holiday, etc.)

---

## Onboarding & Preferences

### `GET /api/v1/onboarding/status`

Returns the current onboarding state.

**Response 200**:
```json
{
  "step": "PROFILE",
  "completed": false
}
```
`step` values: `PROFILE` | `PREFERENCES` | `COMPENSATION_RATES` | `COMPLETE`

---

### `POST /api/v1/onboarding/advance`

Advance to the next onboarding step (called after each wizard step is saved).

**Request**:
```json
{ "currentStep": "PROFILE" }
```

**Response 200**:
```json
{
  "step": "PREFERENCES",
  "completed": false
}
```

**Error 400**: If `currentStep` does not match the stored step.

---

### `GET /api/v1/preferences`

**Response 200**:
```json
{
  "colorScheme": "AUTO"
}
```

---

### `PUT /api/v1/preferences`

**Request**:
```json
{ "colorScheme": "DARK" }
```

**Response 200**: Same as `GET /preferences`.

---

## Engineer Profile

### `POST /api/v1/profile`

Create the engineer profile (first-time setup, onboarding step 1).

**Request**:
```json
{
  "employeeType": "INTERNAL",
  "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "workStartTime": "09:00",
  "workEndTime": "17:00"
}
```

**Response 201**:
```json
{
  "id": 1,
  "employeeType": "INTERNAL",
  "workingDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
  "workStartTime": "09:00",
  "workEndTime": "17:00",
  "locked": false
}
```

**Error 409**: Profile already exists.

---

### `GET /api/v1/profile`

**Response 200**: Same shape as `POST` response. `"locked": true` when registrations exist.

---

### `PUT /api/v1/profile`

Update the profile. Fails if registrations exist.

**Request**: Same shape as `POST`.

**Response 200**: Updated profile.

**Error 409** (`profile-locked`): Registrations exist.

---

## Compensation Rate Table

### `GET /api/v1/compensation-rates`

Returns the full Compensation Rate Table. Optional query param `?employeeType=INTERNAL`.

**Response 200**:
```json
{
  "rates": [
    {
      "id": 1,
      "employeeType": "INTERNAL",
      "rateCategory": "ONCALL_WEEKDAY_SATURDAY",
      "label": "On-call Monday–Saturday",
      "timeFrom": null,
      "timeTo": null,
      "percentage": "0.00"
    },
    {
      "id": 3,
      "employeeType": "INTERNAL",
      "rateCategory": "OVERTIME_ALLOWANCE",
      "label": "Weekday evening (18:00–22:00)",
      "timeFrom": "18:00",
      "timeTo": "22:00",
      "percentage": "0.00"
    }
  ]
}
```

---

### `POST /api/v1/compensation-rates`

Add a new `OVERTIME_ALLOWANCE` row (used when the WCA defines allowance zones that differ from the seeded defaults).

**Request**:
```json
{
  "employeeType": "INTERNAL",
  "rateCategory": "OVERTIME_ALLOWANCE",
  "label": "Weekday evening (18:00–22:00)",
  "timeFrom": "18:00",
  "timeTo": "22:00",
  "percentage": "50.00"
}
```

**Response 201**: Created rate object.

**Error 400**: Validation failure (e.g. `timeFrom`/`timeTo` missing for `OVERTIME_ALLOWANCE`).  
**Error 409**: Duplicate `(employeeType, rateCategory, timeFrom, timeTo)` combination.

---

### `PUT /api/v1/compensation-rates/{id}`

Update a single rate's percentage (and label).

**Request**:
```json
{ "percentage": "50.00", "label": "Weekday evening (18:00–22:00)" }
```

**Response 200**: Updated rate object.

**Error 404**: Rate not found.

---

### `DELETE /api/v1/compensation-rates/{id}`

Delete a compensation rate row. Only `OVERTIME_ALLOWANCE` rows may be deleted; base rows (`ONCALL_WEEKDAY_SATURDAY`, `ONCALL_SUNDAY_HOLIDAY`, `OVERTIME_BASE`) are protected.

**Response 204**.

**Error 404**: Rate not found.  
**Error 409**: Attempt to delete a protected base rate row.

---

## On-Call Periods

### `POST /api/v1/oncall-periods`

**Request**:
```json
{
  "startDateTime": "2026-04-14T14:00:00",
  "endDateTime":   "2026-04-21T14:00:00"
}
```

**Response 201**:
```json
{
  "id": 1,
  "startDateTime": "2026-04-14T14:00:00",
  "endDateTime":   "2026-04-21T14:00:00",
  "holidayOverrides": [],
  "createdAt": "2026-04-25T10:00:00Z"
}
```

**Error 400**: `endDateTime` ≤ `startDateTime`.  
**Error 409** (`onboarding-incomplete`): Onboarding not finished.

---

### `GET /api/v1/oncall-periods`

**Response 200**:
```json
{
  "periods": [
    { "id": 1, "startDateTime": "...", "endDateTime": "...", "holidayOverrides": [], "createdAt": "..." }
  ]
}
```

---

### `GET /api/v1/oncall-periods/{id}`

**Response 200**: Single period object (same as create response).  
**Error 404**: Not found.

---

### `PUT /api/v1/oncall-periods/{id}`

**Request**: `startDateTime`, `endDateTime` (same as `POST`).  
**Response 200**: Updated period.

---

### `DELETE /api/v1/oncall-periods/{id}`

**Response 204**.

---

### `POST /api/v1/oncall-periods/{id}/holidays`

Add a holiday override date.

**Request**:
```json
{ "date": "2026-04-17" }
```

**Response 200**: Updated period object with new holiday in `holidayOverrides`.

**Error 409**: Date already marked as holiday.

---

### `DELETE /api/v1/oncall-periods/{id}/holidays/{date}`

**Response 204**.  
**Error 404**: Holiday override not found.

---

### `POST /api/v1/oncall-periods/{id}/calculate`

Trigger (re)calculation of on-call day entries for the period. Returns computed entries.

**Response 200**:
```json
{
  "periodId": 1,
  "entries": [
    {
      "id": 10,
      "date": "2026-04-14",
      "hours": "10.00",
      "rateType": "WEEKDAY_SATURDAY",
      "capped": false,
      "timeForTimeFlag": false,
      "manualOverride": false
    }
  ]
}
```

---

## Incidents

### `POST /api/v1/incidents`

**Request**:
```json
{
  "onCallPeriodId": 1,
  "date": "2026-04-15",
  "startTime": "02:00",
  "endTime": "03:45"
}
```
`onCallPeriodId` is nullable (omit for non-on-call incidents).

**Response 201**:
```json
{
  "id": 5,
  "onCallPeriodId": 1,
  "date": "2026-04-15",
  "startTime": "02:00",
  "endTime": "03:45",
  "createdAt": "2026-04-25T10:05:00Z"
}
```

**Error 400**: Validation failure.  
**Error 409** (`overtime-day-off`): Date is flagged as a day off.

---

### `GET /api/v1/incidents`

Optional query param: `?onCallPeriodId=1`.

**Response 200**:
```json
{
  "incidents": [
    { "id": 5, "onCallPeriodId": 1, "date": "2026-04-15", "startTime": "02:00", "endTime": "03:45", "createdAt": "..." }
  ]
}
```

---

### `GET /api/v1/incidents/{id}`

**Response 200**: Single incident.

---

### `PUT /api/v1/incidents/{id}`

**Request**: `date`, `startTime`, `endTime`.  
**Response 200**: Updated incident.

---

### `DELETE /api/v1/incidents/{id}`

**Response 204**.

---

### `POST /api/v1/incidents/{id}/calculate`

Trigger (re)calculation of overtime entries for the incident.

**Response 200**:
```json
{
  "incidentId": 5,
  "entries": [
    {
      "id": 20,
      "overtimeHours": "2.00",
      "allowanceHours": null,
      "allowancePercentage": null,
      "timeFrom": "02:00",
      "timeTo": "04:00",
      "isAllowanceEntry": false,
      "manualOverride": false
    },
    {
      "id": 21,
      "overtimeHours": null,
      "allowanceHours": "2.00",
      "allowancePercentage": "50.00",
      "timeFrom": "02:00",
      "timeTo": "04:00",
      "isAllowanceEntry": true,
      "manualOverride": false
    }
  ]
}
```

**Error 409** (`incident-during-working-hours`): All hours fall within normal working hours.  
**Error 409** (`overtime-day-off`): Date is a day off — time-for-time applies.

---

## Registration Summaries

### `GET /api/v1/summaries`

**Response 200**:
```json
{
  "summaries": [
    { "id": 1, "label": "Week 15 — Apr 6–12, 2026", "periodStart": "2026-04-06", "periodEnd": "2026-04-12", "createdAt": "...", "updatedAt": "..." }
  ]
}
```

---

### `POST /api/v1/summaries`

Create a registration summary for a period.

**Request**:
```json
{
  "periodId": 1,
  "label": "Week 16 — Apr 13–19, 2026"
}
```
`label` is optional — auto-generated from period dates if omitted.

**Response 201**: Summary object.

---

### `GET /api/v1/summaries/{id}`

Full summary including all on-call day entries and overtime entries.

**Response 200**:
```json
{
  "id": 1,
  "label": "Week 16 — Apr 13–19, 2026",
  "periodStart": "2026-04-13",
  "periodEnd": "2026-04-19",
  "createdAt": "...",
  "updatedAt": "...",
  "onCallEntries": [
    { "id": 10, "date": "2026-04-14", "hours": "10.00", "rateType": "WEEKDAY_SATURDAY", "capped": false, "timeForTimeFlag": false, "manualOverride": false }
  ],
  "overtimeEntries": [
    { "id": 20, "incidentId": 5, "overtimeHours": "2.00", "allowanceHours": null, "allowancePercentage": null, "timeFrom": "02:00", "timeTo": "04:00", "isAllowanceEntry": false, "manualOverride": false }
  ]
}
```

---

### `DELETE /api/v1/summaries/{id}`

**Response 204**.

---

### `POST /api/v1/summaries/{id}/oncall-entries`

Manually add a new on-call day entry to an existing summary (edge case: entry not covered by the calculated set).

**Request**:
```json
{ "date": "2026-04-18", "hours": "8.00", "rateType": "SUNDAY_HOLIDAY" }
```

**Response 201**: Created entry with `"manualOverride": true`.

**Error 404**: Summary not found.

---

### `PUT /api/v1/summaries/{id}/oncall-entries/{entryId}`

Manual override of an on-call day entry (reporting screen quick-edit).

**Request**:
```json
{ "hours": "12.00", "rateType": "WEEKDAY_SATURDAY" }
```

**Response 200**: Updated entry with `"manualOverride": true`.

---

### `DELETE /api/v1/summaries/{id}/oncall-entries/{entryId}`

**Response 204**.

---

### `POST /api/v1/summaries/{id}/overtime-entries`

Manually add a new overtime entry to an existing summary (edge case: entry not covered by the calculated set).

**Request**:
```json
{
  "incidentId": 5,
  "overtimeHours": "2.00",
  "allowanceHours": "2.00",
  "allowancePercentage": "50.00",
  "timeFrom": "02:00",
  "timeTo": "04:00",
  "isAllowanceEntry": true
}
```

**Response 201**: Created entry with `"manualOverride": true`.

**Error 404**: Summary not found.  
**Error 404**: Incident not found.

---

### `PUT /api/v1/summaries/{id}/overtime-entries/{entryId}`

Manual override of an overtime entry.

**Request**:
```json
{
  "overtimeHours": "3.00",
  "allowanceHours": "3.00",
  "allowancePercentage": "50.00"
}
```

**Response 200**: Updated entry with `"manualOverride": true`.

---

### `DELETE /api/v1/summaries/{id}/overtime-entries/{entryId}`

**Response 204**.
