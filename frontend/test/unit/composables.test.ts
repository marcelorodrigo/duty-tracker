import { describe, expect, it } from 'vitest'
import { buildPivotRows } from '~/utils/compensation'
import type { CompensationRateResponse } from '~/types/compensation'

describe('compensation rate pivoting', () => {
  const mockRates: CompensationRateResponse[] = [
    {
      id: 1,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'WEEKDAY',
      label: 'Mon-Fri 00:00',
      timeFrom: '00:00:00',
      timeTo: '01:00:00',
      percentage: 50
    },
    {
      id: 2,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'SATURDAY',
      label: 'Sat 00:00',
      timeFrom: '00:00:00',
      timeTo: '01:00:00',
      percentage: 50
    },
    {
      id: 3,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'SUNDAY_HOLIDAY',
      label: 'Sun/PH 00:00',
      timeFrom: '00:00:00',
      timeTo: '01:00:00',
      percentage: 100
    },
    {
      id: 4,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'WEEKDAY',
      label: 'Mon-Fri 18:00',
      timeFrom: '18:00:00',
      timeTo: '19:00:00',
      percentage: 35
    },
    {
      id: 5,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'SATURDAY',
      label: 'Sat 18:00',
      timeFrom: '18:00:00',
      timeTo: '19:00:00',
      percentage: 50
    },
    {
      id: 6,
      rateCategory: 'OVERTIME_ALLOWANCE',
      overtimeDayType: 'SUNDAY_HOLIDAY',
      label: 'Sun/PH 18:00',
      timeFrom: '18:00:00',
      timeTo: '19:00:00',
      percentage: 100
    }
  ]

  it('should pivot 6 flat rates into 2 rows with 3 day-type columns', () => {
    const result = buildPivotRows(mockRates)

    expect(result).toHaveLength(2)
    expect(result[0]).toEqual({
      slot: '00:00–01:00',
      timeFrom: '00:00:00',
      weekday: { id: 1, percentage: 50, label: 'Mon-Fri 00:00' },
      saturday: { id: 2, percentage: 50, label: 'Sat 00:00' },
      sundayHoliday: { id: 3, percentage: 100, label: 'Sun/PH 00:00' }
    })
  })

  it('should sort rows by time ascending', () => {
    const result = buildPivotRows(mockRates)

    expect(result[0].timeFrom).toBe('00:00:00')
    expect(result[1].timeFrom).toBe('18:00:00')
  })

  it('should format time slot label as HH:mm–HH:mm', () => {
    const result = buildPivotRows(mockRates)

    expect(result[0].slot).toBe('00:00–01:00')
    expect(result[1].slot).toBe('18:00–19:00')
  })

  it('should filter out non-OVERTIME_ALLOWANCE rates', () => {
    const mixed: CompensationRateResponse[] = [
      ...mockRates,
      {
        id: 99,
        rateCategory: 'ONCALL_WEEKDAY_SATURDAY',
        overtimeDayType: 'WEEKDAY',
        label: 'Oncall',
        timeFrom: '00:00:00',
        timeTo: '01:00:00',
        percentage: 10
      }
    ]

    const result = buildPivotRows(mixed)

    expect(result).toHaveLength(2)
    expect(result[0].weekday.id).toBe(1)
  })

  it('should handle empty rates array', () => {
    const result = buildPivotRows([])

    expect(result).toHaveLength(0)
  })

  it('should preserve all cell properties: id, percentage, label', () => {
    const result = buildPivotRows(mockRates)

    const firstRow = result[0]
    expect(firstRow.weekday).toHaveProperty('id', 1)
    expect(firstRow.weekday).toHaveProperty('percentage', 50)
    expect(firstRow.weekday).toHaveProperty('label', 'Mon-Fri 00:00')
  })
})
