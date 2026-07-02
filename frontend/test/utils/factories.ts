import type { OnCallPeriodResponse } from '~/types/onCallPeriod'
import type { IncidentResponse, CreateIncidentRequest } from '~/types/incident'
import type { HolidayResponse } from '~/types/holiday'
import type { EarningsResponse } from '~/types/earnings'
import type { EngineerProfileResponse } from '~/types/profile'
import type { CompensationRateResponse } from '~/types/compensation'
import type { OnCallPeriodReportResponse } from '~/types/report'

export function buildPeriod(overrides?: Partial<OnCallPeriodResponse>): OnCallPeriodResponse {
  return {
    id: 1,
    startDateTime: '2026-04-01T14:00:00',
    endDateTime: '2026-04-30T14:00:00',
    holidays: [],
    createdAt: '2026-01-01T00:00:00Z',
    ...overrides
  }
}

export function buildIncident(overrides?: Partial<IncidentResponse>): IncidentResponse {
  return {
    id: 1,
    onCallPeriodId: 10,
    name: 'Database failover',
    startDateTime: '2026-04-03T02:30:00',
    endDateTime: '2026-04-03T04:15:00',
    createdAt: '2026-04-03T10:00:00Z',
    ...overrides
  }
}

export function buildCreateIncidentRequest(overrides?: Partial<CreateIncidentRequest>): CreateIncidentRequest {
  return {
    onCallPeriodId: 10,
    name: 'New incident',
    startDateTime: '2026-04-03T02:30:00',
    endDateTime: '2026-04-03T04:15:00',
    ...overrides
  }
}

export function buildHoliday(overrides?: Partial<HolidayResponse>): HolidayResponse {
  return {
    date: '2026-04-27',
    name: 'Koningsdag',
    ...overrides
  }
}

export function buildEarnings(overrides?: Partial<EarningsResponse>): EarningsResponse {
  return {
    periodId: 1,
    periodStart: '2026-04-01T14:00:00',
    periodEnd: '2026-04-30T14:00:00',
    standbyLines: [],
    incidentLines: [],
    grandTotal: '123.45',
    ...overrides
  }
}

export function buildProfile(overrides?: Partial<EngineerProfileResponse>): EngineerProfileResponse {
  return {
    id: 1,
    workingDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
    workStartTime: '08:00:00',
    workEndTime: '16:30:00',
    hourlyRate: 50.0,
    standbyWeekdaySaturdayPercentage: 0.067,
    standbyWeekdaySundayHolidayPercentage: 0.084,
    ...overrides
  }
}

export function buildCompensationRate(overrides?: Partial<CompensationRateResponse>): CompensationRateResponse {
  return {
    id: 1,
    rateCategory: 'OVERTIME_ALLOWANCE',
    overtimeDayType: 'WEEKDAY',
    label: 'Mon-Fri 00:00',
    timeFrom: '00:00:00',
    timeTo: '01:00:00',
    percentage: 50,
    ...overrides
  }
}

export function buildReport(overrides?: Partial<OnCallPeriodReportResponse>): OnCallPeriodReportResponse {
  return {
    periodId: 1,
    periodStart: '2026-04-01T14:00:00',
    periodEnd: '2026-04-30T14:00:00',
    incidentCount: 2,
    incidentIds: [10, 11],
    holidays: [],
    standbyLines: [],
    overtimeLines: [],
    ...overrides
  }
}
