## Why

Engineers need to configure their hourly rate so the system can calculate accurate compensation for on-call work and hours tracked across 13 annual payslip periods (every 4 weeks). Currently, there is no way for engineers to define their rate, blocking the implementation of earnings calculations in future reports.

## What Changes

- Add `hourlyRate` field (BigDecimal) to EngineerProfile entity
- Initialize all profiles with default rate of 1.00 (sentinel value indicating unconfigured)
- Engineers must set rate to > 1.00 when editing their profile
- Backend validation enforces rate > 1.00; returns 400 Bad Request if violated
- Frontend warns when rate > 200/hour ("unusual rate")
- Future report generation can detect unconfigured rates (== 1.00) and alert engineers

## Capabilities

### New Capabilities
- `engineer-hourly-rate`: Engineers can set and manage their hourly rate in their profile. Rates must be > 1.00. Default is 1.00 (unconfigured sentinel).

### Modified Capabilities
- `engineer-profile`: EngineerProfile now includes hourlyRate field; update operations must validate rate constraints.

## Impact

**Backend:**
- EngineerProfile domain entity: add `BigDecimal hourlyRate`
- EngineerProfileEntity JPA: add column `hourly_rate`
- UpdateEngineerProfileUseCase: validate hourlyRate > BigDecimal.ONE, throw InvalidHourlyRateException on violation
- Database migration: add column with default 1.00

**Frontend:**
- Profile edit form: add hourly rate input (numeric, BigDecimal-compatible)
- Validation: must be > 1.00, warn if > 200
- Profile form display: show current hourly rate

**Future Use:**
- Report generation will check if hourlyRate == 1.00 and display alert
