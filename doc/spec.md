# Duty Tracker — Product Specification

This document describes **what** Duty Tracker should do, written as features. It does not prescribe how to implement them. Refer to the other documents in `doc/` for compensation rules, edge cases, and HR submission details.

---

## 1. On-call periods

### 1.1 Create an on-call period

The user can create an on-call period by providing a start date-time and an end date-time. The default period spans from Monday 14:00 to the following Monday 14:00, but both values are freely adjustable. The end date-time must be after the start date-time.

### 1.2 Edit an on-call period

The user can change the start and end date-time of an existing on-call period. The end date-time must remain after the start date-time.

### 1.3 Delete an on-call period

The user can delete an on-call period. Deleting a period removes it and all its associated incidents permanently.

### 1.4 Active vs. past

An on-call period is **active** when its end date-time is today or in the future, and **past** when its end date-time is before today. There is no manual status field — the distinction is derived entirely from the current date.

---

## 2. Incidents

### 2.1 Add an incident

The user can add an incident to any on-call period, regardless of whether the period is active or past. An incident has:

- **Name** (required) — free-text identifier, e.g. "INC-1981 Users cannot order picanha at Jumbo.com"
- **Start date-time** (required)
- **End date-time** (required) — must be after the start date-time
- **Observation** (optional) — free-text notes

An incident may cross midnight. The user always enters it as a single block; any splitting by date or rate bracket happens only at report-generation time.

### 2.2 Edit an incident

The user can change any field of an existing incident. The end date-time must remain after the start date-time.

### 2.3 Delete an incident

The user can remove an incident from its on-call period.

---

## 3. Public holidays

### 3.1 Auto-detection

The system automatically detects Dutch public holidays using the Jollyday library. These holidays affect both standby rates and overtime allowance percentages.

---

## 4. Engineer profile

### 4.1 Set up a profile

The user sets up their engineer profile once. The profile captures:

- **Employee type** (required) — internal or external
- **Working days** (required) — which days of the week the user normally works (e.g. Monday through Friday for full-time, a subset for part-time)
- **Work start time** (required) — when the regular working day begins (e.g. 09:00)
- **Work end time** (required) — when the regular working day ends (e.g. 17:00)

Only one profile exists at a time. The work end time must be after the work start time.

### 4.2 Edit the profile

The user can change any field of their profile at any time.

### 4.3 How the profile affects the system

The profile determines two things for report generation:

- **Working hours** — incident work that falls within the user's normal working hours is not claimable as overtime. The profile's work start/end times define this boundary.
- **Compensation rates** — the employee type determines which set of compensation rates applies when calculating overtime allowances.

If no profile exists, the system uses sensible defaults for working hours and generates only base overtime entries (without allowance percentages).

---

## 5. Compensation table

### 5.1 View the compensation table

The user can view the full compensation rate table. The table contains the overtime allowance percentages that apply per time slot, organized by day type (weekday, Saturday, Sunday/holiday).

The table is pre-populated with the rates from the Jumbo Logistics WCA. Each row represents a one-hour slot and shows the allowance percentage for that slot.

### 5.2 Edit a rate

The user can change the percentage and label of any rate in the table. This allows corrections if the official rates change. Setting the percentage to 0% effectively disables the allowance for that slot — no allowance entry will be generated for it in reports.

### 5.3 Add a custom rate

The user can add a new overtime allowance rate for a specific day type and time slot.

### 5.4 How the compensation table affects the system

The report generator consults this table when splitting overtime entries. For each hour of incident work outside normal working hours, the system looks up the matching rate by day type and time slot to determine the allowance percentage. This is how the report knows whether to generate an allowance entry and at what percentage.

---

## 6. Report generation

### 6.1 Generate a report for an on-call period

The user can generate a report for any on-call period (active or past). The report is computed on demand from current data — it is never a stored snapshot. If the user adds or changes an incident after generating a report, the next generation reflects those changes.

### 6.2 Report structure

The report is divided into two parts:

**Part 1 — Summary**

An overview of the on-call period, including:

- Period start and end date-time
- Total number of incidents
- Per-incident breakdown: name, start/end date-time, and total hours (after rounding)
- Any relevant notes (e.g. incidents that fall fully within working hours and yield no overtime, or days that trigger time-for-time instead of overtime)

**Part 2 — MyHR entry instructions**

A checklist of lines ready to enter into MyHR, structured as described in sections 6.3 and 6.4 below. Each line shows Plan, Option, Date, and Hours.

### 6.3 MyHR lines — standby entries

One standby line per day of the on-call period:

- **Plan**: `NL Allowances - Standby allowance`
- **Option**: `Monday-Saturday` or `Sunday/Holiday` (determined automatically based on day type and holiday status)
- **Date**
- **Hours** (determined automatically based on day type — 15h cap for working days, 24h for non-working days, Sundays, and public holidays)

### 6.4 MyHR lines — overtime entries

For each incident, one or more overtime lines:

- **Plan**: `NL Overtime Hours`
- **Option**: `Overtime hours` (base rate entry) and, when applicable, the allowance entry with the correct percentage
- **Date**
- **Hours** (rounded up to the nearest full hour per incident)

When an incident crosses midnight or spans multiple rate brackets, the report automatically splits it into the correct entries per date and bracket. The user never needs to consult the compensation table.

When the allowance percentage for a bracket is 0%, the report omits the allowance entry for that bracket.

---

## 7. Navigation and views

### 7.1 Main screen — active on-call periods

The main screen displays all active on-call periods (those whose end date-time is today or in the future).

### 7.2 Past on-call periods

A separate view lists past on-call periods, ordered from newest to oldest. Because the list can grow indefinitely, it is paginated — the user can navigate through pages rather than seeing all records at once.

### 7.3 On-call period detail

Clicking any on-call period (active or past) opens a detail view showing all of the period's information and its associated incidents.

---

## 8. Scope and constraints

### 8.1 Single user

Duty Tracker is a local, single-user tool. There is no login, authentication, or multi-tenancy.

### 8.2 No MyHR integration

The tool does not submit anything to MyHR. It generates the data; the user enters it manually and obtains Lead Engineering approval separately.

### 8.3 Internal employees only

The tool applies to internal employees. Freelancer compensation follows a separate process and is out of scope.
