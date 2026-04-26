# Duty Tracker

A personal on-call hours registration tool for engineers at Jumbo Logistics. It calculates WCA-compliant on-call and overtime entries from your schedule and incidents, then presents a structured reporting screen to guide manual entry into MyHR.

No HR system integration. No authentication. No cloud. Runs entirely on your machine via Docker.

---

## Why this exists

Calculating on-call compensation by hand means interpreting the WCA policy rules yourself: which rate applies per day, when the 15-hour cap kicks in, how to round partial overtime hours, which allowance percentage applies at 02:00 vs 18:00, and whether a day qualifies for overtime or time-for-time. One mistake means an incorrect registration.

Duty Tracker does the math for you and produces a ready-to-submit list of line items that maps directly to the fields in MyHR (Manage Personal Contributions).

---

## Features

### Guided onboarding wizard

On first launch, a three-step wizard walks you through mandatory setup before anything else is accessible:

1. **Profile** — select your employee type (Internal or External/Consultant), set your regular working days and daily start/end times.
2. **Preferences** — choose your color scheme (Dark / Light / Auto). The UI previews your selection immediately.
3. **Compensation Rate Table** — review the pre-loaded WCA default percentages for on-call and overtime rates. Adjust any value before proceeding.

The wizard remembers where you left off if you close the app mid-setup.

---

### On-call hour registration

Enter an on-call period (start date/time → end date/time) and the system generates one registration entry per calendar day:

- **Rate type** assigned automatically: `Monday–Saturday` for weekdays and Saturdays, `Sunday/Holiday` for Sundays and public holidays.
- **15-hour cap** applied on your regular working days; up to 24 hours on non-working days.
- **Dutch public holiday calendar** built in — no internet required. Override individual dates within a period if needed.
- **Midnight boundary** handled correctly — hours are split at calendar-day boundaries when your shift starts or ends mid-day.
- **Part-time schedules** supported — days outside your regular schedule are not capped.

---

### Incident and overtime calculation

Log each incident with a date, start time, and end time. The system calculates the exact overtime entries to register:

- **Working-hours exclusion** — hours that fall within your normal working schedule are automatically excluded.
- **Ceil rounding** — partial hours are rounded up to the next full hour (minimum 1 hour per entry).
- **Allowance zone splitting** — when an incident spans multiple WCA time zones (e.g., 21:00–23:00 spans two zones), the entry is split into separate line items, each with its own allowance percentage.
- **Holiday rates** — incidents on public holidays use the holiday overtime rate automatically.

---

### Time-for-time identification

The system distinguishes between scenarios that qualify for financial overtime and those that require time-for-time instead:

- **Day-off flag** — mark any on-call day as a day off directly in the day entry table. The system applies the correct 15h cap and rate, flags the entry, and blocks overtime calculation for incidents on that day.
- **Clear guidance** — instead of silently producing wrong entries, the app shows an informational alert: "Time-for-time applies — discuss with your manager."

---

### Registration summary and reporting screen

After entering all on-call and incident data for a period, generate a Registration Summary:

- **Structured overview** — all on-call day entries and overtime entries listed in one screen, grouped by type, with date, hours, and rate/allowance label per line item.
- **Field labels match MyHR** — column names mirror the HR system so you can transfer values row by row without interpretation.
- **Quick-edit popups** — edit or delete any entry directly on the reporting screen without navigating away.
- **Manual entries** — add extra on-call or overtime rows by hand if a calculated entry does not cover your situation.
- **Persistent history** — summaries are saved and retrievable at any time. Past registrations are never deleted automatically.

---

### Settings

All configuration is editable after onboarding:

- **Compensation Rate Table** — update any allowance percentage or on-call rate when the WCA is renewed. Add or remove `OVERTIME_ALLOWANCE` time-zone rows as needed.
- **Profile** — editable until the first registration summary is saved, then locked to preserve data consistency.
- **Preferences** — color scheme can be changed at any time.

---

## Tech stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4, Java 25, Spring Data JDBC |
| Database | PostgreSQL 18 (Docker) + Flyway migrations |
| Frontend | Nuxt 4, Vue 3, Nuxt UI v3 (Tailwind CSS v4) |
| State | Pinia |
| Utilities | VueUse, Day.js (Dutch locale) |
| Holidays | Jollyday (Dutch calendar, offline) |
| Runtime | Docker Compose — single `docker compose up` |

---

## Quick start

**Prerequisites**: Docker Desktop

```bash
git clone <repo>
cd duty-tracker
docker compose up --build
```

Open `http://localhost:3000` — the onboarding wizard starts automatically.

> See [`specs/001-oncall-hours-tracker/quickstart.md`](specs/001-oncall-hours-tracker/quickstart.md) for local development setup (without Docker) and reset instructions.

---

## Important — WCA rate values

The Compensation Rate Table ships with `0.00` placeholder percentages. **You must update these values from the WCA PDF (Jumbo Logistics Works Council Agreement, version P7-2025) before recording any registrations.** The onboarding wizard prompts you to do this on first launch.

---

## Scope

- Single engineer — no accounts, no authentication, no multi-user support.
- Dutch public holiday calendar only.
- Mobile-first — designed and tested on small-screen devices first; desktop browsers fully supported.
- No HR system integration — the reporting screen is your reference for manual MyHR entry.
- Freelancer WCA scheme is out of scope; Internal and External/Consultant employee types are supported.
