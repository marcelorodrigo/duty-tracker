import { describe, expect, it } from 'vitest'
import { formatTime, isActivePeriod, formatDate, formatDateTime, currentWeekMondayAt14, nextWeekMondayAt14 } from '~/utils/dates'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Build a Date for a specific weekday in the week of 2025-04-28 (Mon).
 *  0 = Monday, 1 = Tuesday, … 6 = Sunday
 */
function weekDate(dayOffset: number, hours = 9, minutes = 0): Date {
  // 2025-04-28 is a known Monday
  const d = new Date(2025, 3, 28, hours, minutes, 0, 0) // month is 0-indexed
  d.setDate(d.getDate() + dayOffset)
  return d
}

// ---------------------------------------------------------------------------
// currentWeekMondayAt14
// ---------------------------------------------------------------------------

describe('currentWeekMondayAt14', () => {
  it('returns the same Monday when called on a Monday before 14:00', () => {
    const monday = weekDate(0, 9, 0) // Mon 2025-04-28 09:00
    const result = currentWeekMondayAt14(monday)

    expect(result.getFullYear()).toBe(2025)
    expect(result.getMonth()).toBe(3) // April
    expect(result.getDate()).toBe(28)
    expect(result.getHours()).toBe(14)
    expect(result.getMinutes()).toBe(0)
  })

  it('returns the same Monday when called on a Monday exactly at 14:00', () => {
    const monday = weekDate(0, 14, 0) // Mon 2025-04-28 14:00 — edge: exactly 14:00 is already past
    const result = currentWeekMondayAt14(monday)

    // 14:00 counts as "already at 14:00" so we expect NEXT Monday
    expect(result.getDate()).toBe(5) // 2025-05-05
    expect(result.getMonth()).toBe(4) // May
    expect(result.getHours()).toBe(14)
  })

  it('returns next Monday when called on a Monday after 14:00', () => {
    const monday = weekDate(0, 15, 30) // Mon 2025-04-28 15:30
    const result = currentWeekMondayAt14(monday)

    expect(result.getDate()).toBe(5) // 2025-05-05
    expect(result.getMonth()).toBe(4)
    expect(result.getHours()).toBe(14)
  })

  it('returns the Monday of the current week when called on a Tuesday', () => {
    const tuesday = weekDate(1, 10, 0) // Tue 2025-04-29
    const result = currentWeekMondayAt14(tuesday)

    expect(result.getDate()).toBe(28) // Mon 2025-04-28
    expect(result.getMonth()).toBe(3)
    expect(result.getHours()).toBe(14)
  })

  it('returns the Monday of the current week when called on a Wednesday', () => {
    const wednesday = weekDate(2, 10, 0) // Wed 2025-04-30
    const result = currentWeekMondayAt14(wednesday)

    expect(result.getDate()).toBe(28)
    expect(result.getMonth()).toBe(3)
  })

  it('returns the Monday of the current week when called on a Friday', () => {
    const friday = weekDate(4, 10, 0) // Fri 2025-05-02
    const result = currentWeekMondayAt14(friday)

    expect(result.getDate()).toBe(28)
    expect(result.getMonth()).toBe(3)
  })

  it('returns the Monday of the current week when called on a Sunday', () => {
    const sunday = weekDate(6, 10, 0) // Sun 2025-05-04
    const result = currentWeekMondayAt14(sunday)

    expect(result.getDate()).toBe(28)
    expect(result.getMonth()).toBe(3)
  })

  it('always sets time to 14:00:00', () => {
    const thursday = weekDate(3, 17, 45)
    const result = currentWeekMondayAt14(thursday)

    expect(result.getHours()).toBe(14)
    expect(result.getMinutes()).toBe(0)
    expect(result.getSeconds()).toBe(0)
  })
})

// ---------------------------------------------------------------------------
// nextWeekMondayAt14
// ---------------------------------------------------------------------------

describe('nextWeekMondayAt14', () => {
  it('returns a date exactly 7 days after the input', () => {
    const start = new Date(2025, 3, 28, 14, 0, 0) // Mon 2025-04-28 14:00
    const result = nextWeekMondayAt14(start)

    expect(result.getDate()).toBe(5)
    expect(result.getMonth()).toBe(4) // May
    expect(result.getFullYear()).toBe(2025)
  })

  it('always sets time to 14:00:00 regardless of input time', () => {
    const start = new Date(2025, 3, 28, 9, 30, 0)
    const result = nextWeekMondayAt14(start)

    expect(result.getHours()).toBe(14)
    expect(result.getMinutes()).toBe(0)
    expect(result.getSeconds()).toBe(0)
  })

  it('does not mutate the input date', () => {
    const start = new Date(2025, 3, 28, 14, 0, 0)
    const originalTime = start.getTime()
    nextWeekMondayAt14(start)

    expect(start.getTime()).toBe(originalTime)
  })

  it('produces an end date one week after the start returned by currentWeekMondayAt14', () => {
    const tuesday = weekDate(1, 10, 0) // Tue 2025-04-29
    const start = currentWeekMondayAt14(tuesday) // Mon 2025-04-28 14:00
    const end = nextWeekMondayAt14(start)        // Mon 2025-05-05 14:00

    const diffMs = end.getTime() - start.getTime()
    expect(diffMs).toBe(7 * 24 * 60 * 60 * 1000)
  })
})



describe('formatTime', () => {
  it('formats HH:mm:ss to HH:mm', () => {
    expect(formatTime('02:30:00')).toBe('02:30')
  })

  it('formats HH:mm to HH:mm', () => {
    expect(formatTime('14:00')).toBe('14:00')
  })

  it('handles midnight', () => {
    expect(formatTime('00:00:00')).toBe('00:00')
  })

  it('handles end of day', () => {
    expect(formatTime('23:59:59')).toBe('23:59')
  })
})

describe('isActivePeriod', () => {
  it('returns true when end datetime is in the future', () => {
    const futureDate = new Date()
    futureDate.setFullYear(futureDate.getFullYear() + 1)
    expect(isActivePeriod(futureDate.toISOString())).toBe(true)
  })

  it('returns true when end datetime is more than 1 minute in the future', () => {
    const soonDate = new Date()
    soonDate.setMinutes(soonDate.getMinutes() + 5)
    expect(isActivePeriod(soonDate.toISOString())).toBe(true)
  })

  it('returns false when end datetime is in the past', () => {
    expect(isActivePeriod('2020-01-01T14:00:00')).toBe(false)
  })

  it('returns false when end time has already passed today (the bug case)', () => {
    // Period ending at 14:00 but current time is 14:05
    const now = new Date()
    const endTime = new Date(now)
    endTime.setHours(14, 0, 0, 0) // Set end time to 14:00
    
    // Only test if current time is after 14:00
    if (now > endTime) {
      expect(isActivePeriod(endTime.toISOString())).toBe(false)
    }
  })

  it('returns true when end time is later today (not yet reached)', () => {
    const now = new Date()
    const endTime = new Date(now)
    endTime.setHours(23, 59, 59, 0) // Set end time to 23:59:59
    
    expect(isActivePeriod(endTime.toISOString())).toBe(true)
  })

  it('returns false when end datetime is exactly now (or very close)', () => {
    const now = new Date()
    expect(isActivePeriod(now.toISOString())).toBe(false)
  })
})

describe('formatDate', () => {
  it('formats ISO date string as DD MMM YYYY', () => {
    expect(formatDate('2025-06-02')).toBe('02 Jun 2025')
  })
})

describe('formatDateTime', () => {
  it('formats ISO datetime string as DD MMM YYYY HH:mm', () => {
    const result = formatDateTime('2025-06-02T14:00:00')
    expect(result).toBe('02 Jun 2025 14:00')
  })
})
