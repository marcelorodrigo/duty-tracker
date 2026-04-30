# Duty Tracker — Product Specification

This document describes **what** Duty Tracker should do, written as features. It does not prescribe how to implement them. Refer to the other documents in `doc/` for compensation rules, edge cases, and HR submission details.

---

## 1. On-call periods

### 1.1 Create an on-call period

The user can create an on-call period by providing a start date-time and an end date-time. The default period spans from Monday 14:00 to the following Monday 14:00, but both values are freely adjustable.

### 1.2 Edit an on-call period

The user can change the start and end date-time of an existing on-call period.

### 1.3 Delete an on-call period

The user can delete an on-call period. Deleting a period removes it and all its associated incidents permanently.

### 1.4 Active vs. past

An on-call period is **active** when its end date-time is in the future, and **past** when its end date-time is in the past. There is no manual status field — the distinction is derived entirely from the current time.

---

## 2. Incidents

### 2.1 Add an incident

The user can add an incident to any on-call period, regardless of whether the period is active or past. An incident has:

- **Name** (required) — free-text identifier, e.g. "INC-1981 Users cannot order picanha at Jumbo.com"
- **Start date-time** (required)
- **End date-time** (required)
- **Observation** (optional) — free-text notes

An incident may cross midnight. The user always enters it as a single block; any splitting by date or rate bracket happens only at report-generation time.

### 2.2 Edit an incident

The user can change any field of an existing incident.

### 2.3 Delete an incident

The user can remove an incident from its on-call period.

---

## 3. Public holidays

### 3.1 Auto-detection

The system automatically detects Dutch public holidays using the Jollyday library. These holidays affect both standby rates and overtime allowance percentages.

### 3.2 User override

The user can manually mark a date as a public holiday or remove the holiday designation from an auto-detected date, within the scope of a specific on-call period. This allows corrections for company-specific holidays or personal situations.

---

## 4. Report generation

### 4.1 Generate a report for an on-call period

The user can generate a report for any on-call period (active or past). The report is computed on demand from current data — it is never a stored snapshot. If the user adds or changes an incident after generating a report, the next generation reflects those changes.

### 4.2 Report content — standby entries

The report produces one standby entry per day of the on-call period. Each entry contains:

- **Plan**: `NL Allowances - Standby allowance`
- **Option**: `Monday-Saturday` or `Sunday/Holiday` (determined automatically based on day type and holiday status)
- **Date**
- **Hours** (determined automatically based on day type — 15h cap for working days, 24h for non-working days, Sundays, and public holidays)

### 4.3 Report content — overtime entries

For each incident, the report produces the corresponding overtime entries. Each overtime block contains:

- **Plan**: `NL Overtime Hours`
- **Option**: `Overtime hours` (base rate entry) and, when applicable, the allowance entry with the correct percentage
- **Date**
- **Hours** (rounded up to the nearest full hour per incident)

When an incident crosses midnight or spans multiple rate brackets, the report automatically splits it into the correct entries per date and bracket. The user never needs to consult the compensation table.

When the allowance percentage for a bracket is 0%, the report omits the allowance entry for that bracket.

### 4.4 Report format

The report is presented as a checklist of MyHR lines — one row per entry — ready for the user to manually enter into MyHR. Each row shows Plan, Option, Date, and Hours.

---

## 5. Navigation and views

### 5.1 Main screen — active on-call periods

The main screen displays all active on-call periods (those whose end date-time is in the future).

### 5.2 Past on-call periods

A separate view lists past on-call periods, ordered from newest to oldest.

### 5.3 On-call period detail

Clicking any on-call period (active or past) opens a detail view showing all of the period's information and its associated incidents.

---

## 6. Scope and constraints

### 6.1 Single user

Duty Tracker is a local, single-user tool. There is no login, authentication, or multi-tenancy.

### 6.2 No MyHR integration

The tool does not submit anything to MyHR. It generates the data; the user enters it manually and obtains Lead Engineering approval separately.

### 6.3 Internal employees only

The tool applies to internal employees. Freelancer compensation follows a separate process and is out of scope.
