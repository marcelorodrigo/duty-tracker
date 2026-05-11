## Context

The EngineerProfile entity currently tracks working days and hours but lacks an hourly rate field. This blocks the implementation of earnings calculations for on-call periods and payslip reports. The system needs to store engineers' hourly rates and validate them before any compensation-related features are built.

The codebase already has infrastructure for on-call period tracking (OnCallPeriod), overtime calculation (CalculateOvertimeEntriesUseCase), and compensation gateways. This change provides the missing piece: per-engineer hourly rates.

## Goals / Non-Goals

**Goals:**
- Add hourly rate capability to EngineerProfile with proper validation
- Establish a foundation for future earnings calculations
- Detect unconfigured rates in future reports via sentinel value
- Enforce rate > 1.00 at both frontend and backend

**Non-Goals:**
- Implement earnings calculation logic (future work)
- Build earnings reports (future work)
- Support rate history or historical rate lookups
- Integrate with CompensationRateGateway (will be future enhancement)

## Decisions

**1. Use BigDecimal for hourlyRate**
- **Why**: Currency calculations require precision; float/double risks rounding errors
- **Alternative considered**: Separate cents field; rejected as more complex

**2. Default to 1.00 as sentinel value**
- **Why**: Allows database to always have a non-null value while marking "unconfigured" state. Future reports can detect == 1.00 and alert engineers.
- **Alternative considered**: Nullable BigDecimal; rejected because 1) every query needs null checks, 2) makes it unclear if null means "unconfigured" or "error"

**3. Validation rule: hourlyRate > BigDecimal.ONE (strictly greater)**
- **Why**: Prevents accidental 1.00 values post-configuration and ensures any configured rate is > sentinel
- **Alternative considered**: hourlyRate >= 1.01; rejected as overly specific; > 1.00 is cleaner

**4. No rate history tracking**
- **Why**: Simplifies schema and MVP scope. Earnings calculations will use current rate (no retroactive adjustments)
- **Trade-off**: If rate changes mid-payslip-period, all hours use new rate. Acceptable for v1.

**5. Frontend warning at > 200/hour**
- **Why**: Catches typos (e.g., $2000 instead of $200) while allowing unusual but valid rates
- **Alternative considered**: Hard cap at 200; rejected because some engineers may earn more

**6. Backend exception: InvalidHourlyRateException → 400 Bad Request**
- **Why**: Clear, testable contract. Frontend must validate before submit; backend is defense-in-depth.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Engineers leave rate at 1.00 (default) | Future report generation shows alert pointing to settings page. Earnings will be incorrect, not silently wrong. |
| Rate changes mid-payslip-period | All hours use new rate. Document this as expected behavior for v1. Future: implement rate history if needed. |
| Unusual rates (e.g., $1000/hr) allowed | Frontend warning helps catch typos. No hard cap, as legitimacy depends on context. |
| No audit trail of rate changes | Not required for MVP. Can add in future if compensation becomes auditable. |

## Migration Plan

1. Add `hourly_rate DECIMAL(19, 2) DEFAULT 1.00` column to V1__create_schema.sql (merged into initial schema, no separate migration)
2. Existing engineers automatically get rate = 1.00
3. UpdateEngineerProfileUseCase validates and persists new rate
4. Frontend form updated to include hourly rate input and validation
5. No rollback needed (column is additive, default is safe)

## Open Questions

None identified. Implementation can proceed.
