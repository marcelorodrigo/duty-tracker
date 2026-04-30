# HR Submission Guide (MyHR)

Step-by-step instructions for submitting your on-call and overtime registration.

## Steps

1. Open **MyHR** via Okta.
2. Under `Quick Actions`, select `Show More`.
3. Under `Compensation`, select `Manage Personal Contributions`.
4. Click `Manage Contributions`.
5. **For each standby day:**
   - Plan: `NL Allowances - Standby allowance`
   - Option: select the rate (`Monday-Saturday` or `Sunday/Holiday`)
   - Enter the date and hours
6. **For each overtime block** (see [detailed explanation below](#how-overtime-entries-work)):
   - **Entry 1** — Plan: `NL Overtime Hours`, Option: `Overtime hours` — the actual hours worked
   - **Entry 2** — Plan: `NL Overtime Hours`, Option: `Extra Hours #%` or `Hours overtime all #%` — the allowance hours for that time bracket
7. Click `Continue`, then `Submit`.
8. Your Lead Engineering will approve the registration. It is paid out with your next salary.

## How overtime entries work

Overtime always requires **two separate entries** in MyHR per time bracket — one for the hours worked, and one for the allowance on top. This is how you get paid both the base hourly rate **and** the additional allowance percentage.

### Example: 2 minutes of incident work on a Sunday

You worked 2 minutes on a Sunday. Rounded up, that's 1 hour. Sunday has a 100% allowance.

| # | Plan | Option | Date | Hours |
|---|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | Sunday | 1 |
| 2 | `NL Overtime Hours` | `Extra Hours 100%` | Sunday | 1 |

Entry 1 pays you the base hourly rate. Entry 2 pays you the 100% allowance on top.

### Example: 1 hour of overtime on a weekday evening (no allowance bracket)

You worked from 17:00 to 18:00 on a Monday. This time slot has a 0% allowance.

| # | Plan | Option | Date | Hours |
|---|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | Monday | 1 |

No second entry needed — there is no allowance for this bracket.

### Example: 2 hours spanning two rate brackets on a Saturday

You worked from 21:00 to 23:00 on a Saturday. The 21:00–22:00 bracket has one rate, and 22:00–23:00 has a different rate.

| # | Plan | Option | Date | Hours |
|---|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | Saturday | 2 |
| 2 | `NL Overtime Hours` | `Extra Hours #%` (21–22 rate) | Saturday | 1 |
| 3 | `NL Overtime Hours` | `Extra Hours #%` (22–23 rate) | Saturday | 1 |

One entry for the total overtime hours, then one entry per allowance bracket.

### Example: complete week registration

A full week on-call (Mon–Sun, no holidays), with two incidents:
- 1 hour on one day (no allowance bracket)
- 3 hours on another day (2 hours with allowance)

| # | Plan | Option | Date | Hours |
|---|---|---|---|---|
| 1 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Mon | 15 |
| 2 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Tue | 15 |
| 3 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Wed | 15 |
| 4 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Thu | 15 |
| 5 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Fri | 15 |
| 6 | `NL Allowances - Standby allowance` | `Monday-Saturday` | Sat | 24 |
| 7 | `NL Allowances - Standby allowance` | `Sunday/Holiday` | Sun | 24 |
| 8 | `NL Overtime Hours` | `Overtime hours` | (day 1) | 1 |
| 9 | `NL Overtime Hours` | `Overtime hours` | (day 2) | 3 |
| 10 | `NL Overtime Hours` | `Extra Hours #%` | (day 2) | 2 |

## On-call week boundary note

Standby shifts typically start and end on Mondays at 14:00. For convenience, you can register a single Monday with the full hours (capped at 15 for working days) instead of splitting across two Mondays. However, if either Monday is a holiday, you must split the hours so each Monday uses the correct rate.

## What Duty Tracker generates

The export report maps directly to the entries above:

- One entry per standby day with plan, option, date, and hours
- One entry per overtime block with plan, option, date, and hours
- Separate allowance percentage entries per time bracket
- Notes for days where time-for-time should be discussed instead

## Reference

Screenshotted examples are available on [Confluence](https://jumbo-supermarkten.atlassian.net/wiki/spaces/INC/pages/3625386419).
