import { describe, expect, it } from 'vitest'
import type { HolidayInput } from '~/types/holiday'
import { mergeHolidays } from '~/utils/holidays'

describe('mergeHolidays', () => {
  it('returns an empty list when there are no inputs', () => {
    const result = mergeHolidays([], [], '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(0)
  })

  it('filters out holidays that fall before the new start date', () => {
    const current: HolidayInput[] = [
      { date: '2026-03-15', name: 'Old holiday' },
      { date: '2026-04-06', name: 'In range' }
    ]

    const result = mergeHolidays(current, [], '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(1)
    expect(result[0].date).toBe('2026-04-06')
  })

  it('filters out holidays that fall after the new end date', () => {
    const current: HolidayInput[] = [
      { date: '2026-04-06', name: 'In range' },
      { date: '2026-05-14', name: 'Hemelvaartsdag' }
    ]

    const result = mergeHolidays(current, [], '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(1)
    expect(result[0].date).toBe('2026-04-06')
  })

  it('includes holidays on the start boundary date', () => {
    const current: HolidayInput[] = [{ date: '2026-04-01', name: 'Boundary start' }]

    const result = mergeHolidays(current, [], '2026-04-01', '2026-04-30')

    expect(result).toEqual(current)
  })

  it('includes holidays on the end boundary date', () => {
    const current: HolidayInput[] = [{ date: '2026-04-30', name: 'Boundary end' }]

    const result = mergeHolidays(current, [], '2026-04-01', '2026-04-30')

    expect(result).toEqual(current)
  })

  it('adds suggestions that are not already present', () => {
    const suggestions = [{ date: '2026-04-27', name: 'Koningsdag' }]

    const result = mergeHolidays([], suggestions, '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(1)
    expect(result[0]).toEqual({ date: '2026-04-27', name: 'Koningsdag' })
  })

  it('does not add a suggestion when the date already exists in current list', () => {
    const current: HolidayInput[] = [{ date: '2026-04-27', name: 'King\'s Day' }]
    const suggestions = [{ date: '2026-04-27', name: 'Koningsdag' }]

    const result = mergeHolidays(current, suggestions, '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(1)
    expect(result[0].name).toBe('King\'s Day') // existing name wins
  })

  it('converts a null suggestion name to an empty string', () => {
    const suggestions = [{ date: '2026-04-27', name: null }]

    const result = mergeHolidays([], suggestions, '2026-04-01', '2026-04-30')

    expect(result[0].name).toBe('')
  })

  it('sorts the merged result by date ascending', () => {
    const current: HolidayInput[] = [{ date: '2026-04-27', name: 'Koningsdag' }]
    const suggestions = [{ date: '2026-04-06', name: 'Tweede Paasdag' }]

    const result = mergeHolidays(current, suggestions, '2026-04-01', '2026-04-30')

    expect(result[0].date).toBe('2026-04-06')
    expect(result[1].date).toBe('2026-04-27')
  })

  it('keeps multiple in-range holidays and merges multiple suggestions', () => {
    const current: HolidayInput[] = [
      { date: '2026-04-06', name: 'Tweede Paasdag' },
      { date: '2026-05-10', name: 'Out of range' }
    ]
    const suggestions = [
      { date: '2026-04-27', name: 'Koningsdag' },
      { date: '2026-04-06', name: 'Duplicate' }
    ]

    const result = mergeHolidays(current, suggestions, '2026-04-01', '2026-04-30')

    expect(result).toHaveLength(2)
    expect(result.map(h => h.date)).toEqual(['2026-04-06', '2026-04-27'])
  })
})
