# Duty Tracker

**Backend** &nbsp;
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-backend)
[![Tech Debt](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-backend&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-backend)

**Frontend** &nbsp;
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-frontend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-frontend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-frontend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-frontend)
[![Tech Debt](https://sonarcloud.io/api/project_badges/measure?project=marcelorodrigo_duty-tracker-frontend&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=marcelorodrigo_duty-tracker-frontend)

A local tool for engineers to track on-call shifts and incident work during their duty period, and generate a ready-to-submit report for HR compensation registration.

---

## The Problem

After every on-call week, engineers on-call must manually submit their hours to **the HR system** to receive financial compensation. This involves:

- Logging each day you were on-call, with the correct number of hours and the right rate (weekday vs. Sunday/holiday)
- Logging each overtime hour worked during incidents, separately from the on-call allowance
- Applying the correct allowance percentage per time slot (which varies by day of week and time of day)
- Doing all of this with precision: mistakes mean under or over-reporting compensation

This is tedious, error-prone, and easy to forget details of after a busy on-call week.

---

## The Solution

Duty Tracker runs on your machine throughout your on-call period. You log shifts and incidents as they happen. When the period ends, you get a clear report that tells you exactly what to enter in the HR system: no guesswork, no retroactive reconstruction from memory.

---

## Features

### Track on-call standby hours

Log each day you are on standby. The tracker automatically applies the correct rules:

- **Monday–Saturday (regular working day):** maximum 15 hours claimable per day
- **Monday–Saturday (non-working day):** up to 24 hours claimable
- **Sunday or public holiday:** up to 24 hours at the higher rate

### Track incident work (overtime)

When you are called to act on an incident, log the time you started and stopped. The tracker:

- Rounds up to the nearest full hour (e.g. 1 minute of work = 1 claimable hour; 1h15m = 2 claimable hours)
- Determines whether overtime applies (work outside your normal working hours)
- Calculates the correct allowance percentage for each time slot based on day and hour
- Splits entries when a single incident spans multiple rate brackets (e.g. 21:00–23:00 on a Saturday)

### Handle edge cases correctly

- Days off are still treated as regular working days: standby is capped at 15 hours and overtime does not apply; time-for-time applies instead
- Part-time schedules are respected: non-working days follow the non-working-day rules
- Public holidays are flagged automatically so the correct rate is applied
- Incidents worked entirely within normal hours are excluded from overtime claims

### Generate an HR submission report

At the end of the on-call period, export a structured report that maps directly to what you need to enter in MyHR:

- One entry per standby day, with the plan (`NL Allowances - Standby allowance`), option, date, and hours
- One entry per overtime block, with the plan (`NL Overtime Hours`), option, date, and hours
- Allowance percentage entries listed separately per time bracket, ready to enter under `Extra Hours #%` or `Hours overtime all #%`
- Notes on any days where time-for-time should be discussed with your manager instead

---

## How It Fits Into the Process

This tool does **not** submit anything to the HR system on your behalf. The submission still requires manual entry and approval from your Lead Engineering. Duty Tracker's job is to make sure you arrive at that step with accurate, complete data: so the submission takes minutes instead of requiring you to reconstruct a week of on-call from memory.

---

## Documentation

For detailed information about compensation rules, edge cases, and the MyHR submission process, see the [`doc/`](./doc/) folder:

- **[`overview.md`](./doc/overview.md)** — Project scope and reference links
- **[`on-call-compensation.md`](./doc/on-call-compensation.md)** — Standby allowance rules by day type
- **[`overtime-compensation.md`](./doc/overtime-compensation.md)** — Overtime and allowance percentage rules
- **[`hr-submission-guide.md`](./doc/hr-submission-guide.md)** — Step-by-step MyHR submission process
- **[`edge-cases.md`](./doc/edge-cases.md)** — Days off, part-time, holidays, and special scenarios

The local Docker Compose environment enables the `development` Spring profile, which exposes Swagger UI at
`http://localhost:8080/swagger-ui.html` and the generated API document at `http://localhost:8080/v3/api-docs`.
When running the backend directly, set `SPRING_PROFILES_ACTIVE=development` to enable those endpoints. They are
disabled by default so deployments must explicitly opt in to exposing API documentation.

### Observability

The backend exposes its management endpoints on the application port (`8080`) under `/actuator`. Only these
read-only endpoints are enabled and exposed:

- `/actuator/health` — aggregate service health without returning component details
- `/actuator/health/liveness` — process liveness for restart decisions
- `/actuator/health/readiness` — readiness including PostgreSQL availability
- `/actuator/prometheus` — Prometheus metrics for HTTP traffic, the JVM, the process, and the database pool

All metrics include the stable `application="duty-tracker-backend"` label. Other Actuator endpoints are disabled,
not merely hidden from the web. Because management traffic shares port `8080`, production ingress should allow
the health probes and restrict the Prometheus path to the monitoring network. Tracing is intentionally not enabled
until a collector and trace consumer are configured.

---

## Compensation rules (summary)

| Situation | What to claim |
|---|---|
| On-call on a regular working day (Mon–Sat) | Up to 15 standby hours at the Mon–Sat rate |
| On-call on a non-working day (Mon–Sat) | Up to 24 standby hours at the Mon–Sat rate |
| On-call on a Sunday or public holiday | Up to 24 standby hours at the Sunday/holiday rate |
| Incident work outside normal hours | Overtime hours + applicable allowance percentage per time slot |
| Incident work inside normal hours | Not claimable |
| On-call or incident work on a day off | Not claimable as overtime; discuss time-for-time with your manager |
| Incident work when not on-call | Not claimable as overtime; time-for-time applies |
