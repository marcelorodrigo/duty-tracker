# Overtime Compensation

When you are on-call and called to perform work based on your on-call responsibilities **outside your normal working hours**, you are entitled to overtime compensation.

## How it works

For each overtime hour you receive:
1. Your **base hourly rate**
2. Possibly an **additional allowance percentage** on top, depending on the time slot

Example: hourly rate = EUR 10, allowance = 50% => EUR 15 for that hour.

## Rounding rule

Time is rounded up to the nearest full hour:
- 5 minutes of work = 1 claimable hour
- 1h15m of work = 2 claimable hours

## Allowance percentages

The allowance varies by day and time of day. When an incident spans multiple rate brackets (e.g., Saturday 21:00–23:00), you must split the entry into separate blocks per bracket.

Refer to the [Jumbo Logistics WCA](https://jumbosupermarkten.sharepoint.com/:b:/r/sites/HumanResources/Gedeelde%20documenten/HR%20-%20Medewerkersregelingen/Supply%20Chain/Supply%20Chain%20-%20Beloning%20en%20Sociaal%20Begeleidingsregeling/01.03%20UK%20-%20WCA%20Jumbo%20Logistics%202023-2028%20version%20P7-2025.pdf?csf=1&web=1&e=PPFMm6) for the full allowance percentage table. The `Non-basic obligatory Saturday` column applies.

## MyHR entry

Each overtime block requires **two separate entries** in MyHR:

1. **Entry 1** — Plan: `NL Overtime Hours`, Option: `Overtime hours` — registers the actual hours worked (pays your base hourly rate)
2. **Entry 2** — Plan: `NL Overtime Hours`, Option: `Extra Hours #%` or `Hours overtime all #%` — registers the allowance hours for that time bracket (pays the additional percentage on top)

Both entries use the same date and hours. If the time slot has a 0% allowance, only entry 1 is needed.

See the [HR Submission Guide](./hr-submission-guide.md#how-overtime-entries-work) for detailed examples with tables.

## Examples

### Monday, incident 10:00–12:00 (your normal hours are 09:00–17:00)

Nothing to submit — work was within normal working hours.

### Monday, incident 17:00–18:00 (no allowance bracket)

| # | Plan | Option | Hours |
|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | 1 |

Only one entry — this time slot has a 0% allowance, so no allowance entry is needed.

### Monday, incident 04:00–06:00

| # | Plan | Option | Hours |
|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | 2 |
| 2 | `NL Overtime Hours` | `Extra Hours #%` | 2 |

Two entries: 2 hours of base pay + 2 hours of allowance at the applicable rate.

### Monday, incident 04:00–05:15 (rounding applies)

| # | Plan | Option | Hours |
|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | 2 |
| 2 | `NL Overtime Hours` | `Extra Hours #%` | 2 |

Rounded up from 1h15m to 2 hours. Same two entries as above.

### Saturday, incident 21:00–23:00 (two rate brackets)

| # | Plan | Option | Hours |
|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | 2 |
| 2 | `NL Overtime Hours` | `Extra Hours #%` (21–22 rate) | 1 |
| 3 | `NL Overtime Hours` | `Extra Hours #%` (22–23 rate) | 1 |

Three entries: the total overtime hours, then one allowance entry per bracket because 21:00–22:00 and 22:00–23:00 have different percentages.

### Monday (holiday), incident 08:00–10:00

| # | Plan | Option | Hours |
|---|---|---|---|
| 1 | `NL Overtime Hours` | `Overtime hours` | 2 |
| 2 | `NL Overtime Hours` | `Extra Hours #%` (holiday rate) | 2 |

Holidays have their own allowance percentage — use the holiday rate for the allowance entry.

### Monday (day off on a working day), incident 08:00–10:00

Nothing to submit — overtime does not apply on days off. Discuss time-for-time with your manager instead.

### Monday (part-time, don't normally work Monday), incident 08:00–10:00

Nothing to submit — same as above, time-for-time applies instead.
