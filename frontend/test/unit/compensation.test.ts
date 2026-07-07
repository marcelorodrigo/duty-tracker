import { describe, expect, it } from 'vitest'
import type { CompensationRateResponse } from '~/types/compensation'
import { buildPivotRows } from '~/utils/compensation'

function rate(overrides?: Partial<CompensationRateResponse>): CompensationRateResponse {
  return {
    id: 1,
    rateCategory: 'OVERTIME_ALLOWANCE',
    overtimeDayType: 'WEEKDAY',
    label: 'Mon-Fri 00:00–01:00',
    timeFrom: '00:00:00',
    timeTo: '01:00:00',
    percentage: 50,
    ...overrides
  }
}

describe('buildPivotRows', () => {
  it('returns an empty array when given no rates', () => {
    expect(buildPivotRows([])).toHaveLength(0)
  })

  it('returns an empty array when no rates have OVERTIME_ALLOWANCE category', () => {
    const rates = [
      rate({ rateCategory: 'OVERTIME_BASE', timeFrom: '00:00:00', timeTo: '01:00:00' }),
      rate({ rateCategory: 'ONCALL_WEEKDAY_SATURDAY', timeFrom: '00:00:00', timeTo: '23:00:00' })
    ]
    expect(buildPivotRows(rates)).toHaveLength(0)
  })

  it('creates a single row for one OT_ALLOWANCE rate', () => {
    const rates = [rate({ id: 1, timeFrom: '22:00:00', timeTo: '23:00:00', percentage: 50 })]
    const rows = buildPivotRows(rates)

    expect(rows).toHaveLength(1)
    expect(rows[0]).toMatchObject({
      timeFrom: '22:00:00',
      slot: '22:00–23:00',
      weekday: { id: 1, percentage: 50, label: 'Mon-Fri 00:00–01:00' }
    })
  })

  it('groups rates with the same timeFrom into a single row', () => {
    const rates: CompensationRateResponse[] = [
      rate({ id: 1, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'WEEKDAY', percentage: 50 }),
      rate({ id: 2, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'SATURDAY', percentage: 75 }),
      rate({ id: 3, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'SUNDAY_HOLIDAY', percentage: 100 })
    ]

    const rows = buildPivotRows(rates)

    expect(rows).toHaveLength(1)
    expect(rows[0].weekday).toEqual({ id: 1, percentage: 50, label: 'Mon-Fri 00:00–01:00' })
    expect(rows[0].saturday).toEqual({ id: 2, percentage: 75, label: 'Mon-Fri 00:00–01:00' })
    expect(rows[0].sundayHoliday).toEqual({ id: 3, percentage: 100, label: 'Mon-Fri 00:00–01:00' })
  })

  it('creates separate rows for different timeFrom values', () => {
    const rates: CompensationRateResponse[] = [
      rate({ id: 1, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'WEEKDAY', percentage: 50 }),
      rate({ id: 2, timeFrom: '01:00:00', timeTo: '02:00:00', overtimeDayType: 'WEEKDAY', percentage: 60 })
    ]

    const rows = buildPivotRows(rates)

    expect(rows).toHaveLength(2)
    expect(rows[0].timeFrom).toBe('00:00:00')
    expect(rows[1].timeFrom).toBe('01:00:00')
  })

  it('sorts rows by timeFrom ascending', () => {
    const rates: CompensationRateResponse[] = [
      rate({ id: 1, timeFrom: '22:00:00', timeTo: '23:00:00', overtimeDayType: 'WEEKDAY', percentage: 75 }),
      rate({ id: 2, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'WEEKDAY', percentage: 50 }),
      rate({ id: 3, timeFrom: '01:00:00', timeTo: '02:00:00', overtimeDayType: 'WEEKDAY', percentage: 60 })
    ]

    const rows = buildPivotRows(rates)

    expect(rows).toHaveLength(3)
    expect(rows[0].timeFrom).toBe('00:00:00')
    expect(rows[1].timeFrom).toBe('01:00:00')
    expect(rows[2].timeFrom).toBe('22:00:00')
  })

  it('filters out non-OT_ALLOWANCE rates from the result', () => {
    const rates: CompensationRateResponse[] = [
      rate({ id: 1, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'WEEKDAY', rateCategory: 'OVERTIME_ALLOWANCE' }),
      rate({ id: 2, timeFrom: '00:00:00', timeTo: '01:00:00', overtimeDayType: 'WEEKDAY', rateCategory: 'OVERTIME_BASE' })
    ]

    const rows = buildPivotRows(rates)

    expect(rows).toHaveLength(1)
  })

  it('renders time slot using formatTime (HH:mm format)', () => {
    const rates = [rate({ timeFrom: '08:30:00', timeTo: '16:45:00' })]
    const rows = buildPivotRows(rates)

    expect(rows[0].slot).toBe('08:30–16:45')
  })
})
