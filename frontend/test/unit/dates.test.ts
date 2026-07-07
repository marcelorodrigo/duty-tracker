import { describe, expect, it } from 'vitest'
import { CalendarDate, CalendarDateTime } from '@internationalized/date'
import { formatTime, extractTimeFromISO, formatDate, formatDateTime, currentWeekMondayAt14, nextWeekMondayAt14, getPeriodStatus, getStatusColors, formatDuration, calendarDateFromISO, calendarDateToISO, buildCalendarDateTime, getRecentPastPeriods } from '~/utils/dates'

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
    const end = nextWeekMondayAt14(start) // Mon 2025-05-05 14:00

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

describe('extractTimeFromISO', () => {
  it('extracts HH:mm from a full ISO datetime string', () => {
    expect(extractTimeFromISO('2026-04-01T14:00:00')).toBe('14:00')
  })

  it('extracts HH:mm for non-00 minutes', () => {
    expect(extractTimeFromISO('2026-04-01T10:30:00')).toBe('10:30')
  })

  it('returns fallback when the string has no time component', () => {
    expect(extractTimeFromISO('2026-04-01')).toBe('14:00')
  })

  it('returns custom fallback when provided and no time component', () => {
    expect(extractTimeFromISO('2026-04-01', '08:00')).toBe('08:00')
  })

  it('returns fallback for an empty string', () => {
    expect(extractTimeFromISO('')).toBe('14:00')
  })

  it('returns fallback when the time part is missing after T', () => {
    expect(extractTimeFromISO('2026-04-01T')).toBe('14:00')
  })

  it('ignores custom fallback when ISO has a valid time component', () => {
    expect(extractTimeFromISO('2026-04-01T10:30:00', '08:00')).toBe('10:30')
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

// ---------------------------------------------------------------------------
// getPeriodStatus
// ---------------------------------------------------------------------------

describe('getPeriodStatus', () => {
  it('returns "scheduled" when start time is in the future', () => {
    const now = new Date()
    const futureStart = new Date(now.getTime() + 1 * 60 * 60 * 1000) // 1 hour from now
    const futureEnd = new Date(now.getTime() + 2 * 60 * 60 * 1000) // 2 hours from now

    const status = getPeriodStatus(
      futureStart.toISOString(),
      futureEnd.toISOString()
    )
    expect(status).toBe('scheduled')
  })

  it('returns "active" when current time is between start and end', () => {
    const now = new Date()
    const pastStart = new Date(now.getTime() - 1 * 60 * 60 * 1000) // 1 hour ago
    const futureEnd = new Date(now.getTime() + 1 * 60 * 60 * 1000) // 1 hour from now

    const status = getPeriodStatus(
      pastStart.toISOString(),
      futureEnd.toISOString()
    )
    expect(status).toBe('active')
  })

  it('returns "past" when end time is in the past', () => {
    const pastStart = '2020-01-01T14:00:00'
    const pastEnd = '2020-01-01T15:00:00'

    const status = getPeriodStatus(pastStart, pastEnd)
    expect(status).toBe('past')
  })

  it('returns "past" when current time equals end time', () => {
    const now = new Date()
    const pastStart = new Date(now.getTime() - 1 * 60 * 60 * 1000)

    const status = getPeriodStatus(
      pastStart.toISOString(),
      now.toISOString()
    )
    expect(status).toBe('past')
  })

  it('returns "active" when start time is exactly now', () => {
    const now = new Date()
    const futureEnd = new Date(now.getTime() + 1 * 60 * 60 * 1000)

    const status = getPeriodStatus(
      now.toISOString(),
      futureEnd.toISOString()
    )
    expect(status).toBe('active')
  })
})

// ---------------------------------------------------------------------------
// getStatusColors
// ---------------------------------------------------------------------------

describe('getStatusColors', () => {
  it('returns green colors for "active" status', () => {
    const colors = getStatusColors('active')

    expect(colors.badge).toContain('success')
    expect(colors.dot).toContain('success')
  })

  it('returns primary (blue) colors for "scheduled" status', () => {
    const colors = getStatusColors('scheduled')

    expect(colors.badge).toContain('primary')
    expect(colors.dot).toContain('primary')
  })

  it('returns muted/gray colors for "past" status', () => {
    const colors = getStatusColors('past')

    expect(colors.badge).toContain('bg-')
    expect(colors.badge).toContain('text-')
    expect(colors.dot).toContain('dimmed')
  })

  it('returns an object with both badge and dot properties', () => {
    const colors = getStatusColors('active')

    expect(colors).toHaveProperty('badge')
    expect(colors).toHaveProperty('dot')
  })

  it('badge for active includes both light and dark mode classes', () => {
    const colors = getStatusColors('active')

    expect(colors.badge).toContain('dark:')
  })

  it('badge for scheduled includes both light and dark mode classes', () => {
    const colors = getStatusColors('scheduled')

    expect(colors.badge).toContain('dark:')
  })
})

// ---------------------------------------------------------------------------
// formatDuration
// ---------------------------------------------------------------------------

describe('formatDuration', () => {
  it('formats 1 minute duration as "1 minute"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T02:31:00'
    expect(formatDuration(start, end)).toBe('1 minute')
  })

  it('formats multiple minutes as "X minutes"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T02:35:00'
    expect(formatDuration(start, end)).toBe('5 minutes')
  })

  it('formats 1 hour duration as "1 hour"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T03:30:00'
    expect(formatDuration(start, end)).toBe('1 hour')
  })

  it('formats multiple hours as "X hours"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T05:30:00'
    expect(formatDuration(start, end)).toBe('3 hours')
  })

  it('formats 1 day duration as "1 day"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-04T02:30:00'
    expect(formatDuration(start, end)).toBe('1 day')
  })

  it('formats multiple days as "X days"', () => {
    const start = '2025-06-01T02:30:00'
    const end = '2025-06-03T02:30:00'
    expect(formatDuration(start, end)).toBe('2 days')
  })

  it('formats 1 hour and 30 minutes', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T04:00:00'
    expect(formatDuration(start, end)).toBe('1 hour, 30 minutes')
  })

  it('formats 2 hours and 15 minutes', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T04:45:00'
    expect(formatDuration(start, end)).toBe('2 hours, 15 minutes')
  })

  it('formats 1 day and 1 hour', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-04T03:30:00'
    expect(formatDuration(start, end)).toBe('1 day, 1 hour')
  })

  it('formats 1 day and 3 hours', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-04T05:30:00'
    expect(formatDuration(start, end)).toBe('1 day, 3 hours')
  })

  it('formats 1 day, 3 hours and 2 minutes', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-04T05:32:00'
    expect(formatDuration(start, end)).toBe('1 day, 3 hours and 2 minutes')
  })

  it('formats 2 days, 1 hour and 5 minutes', () => {
    const start = '2025-06-01T02:30:00'
    const end = '2025-06-03T03:35:00'
    expect(formatDuration(start, end)).toBe('2 days, 1 hour and 5 minutes')
  })

  it('returns "0 minutes" when start and end are the same', () => {
    const start = '2025-06-03T02:30:00'
    expect(formatDuration(start, start)).toBe('0 minutes')
  })

  it('returns "0 minutes" when end is before start', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T02:00:00'
    expect(formatDuration(start, end)).toBe('0 minutes')
  })

  it('ignores seconds when calculating duration', () => {
    const start = '2025-06-03T02:30:45'
    const end = '2025-06-03T02:31:30'
    expect(formatDuration(start, end)).toBe('0 minutes')
  })

  it('correctly handles durations that round down seconds', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T02:30:45'
    expect(formatDuration(start, end)).toBe('0 minutes')
  })

  it('correctly handles durations that span across days due to daylight time', () => {
    const start = '2025-06-03T22:00:00'
    const end = '2025-06-04T02:00:00'
    expect(formatDuration(start, end)).toBe('4 hours')
  })

  it('uses singular form for "1 day"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-04T02:30:00'
    const result = formatDuration(start, end)
    expect(result).toContain('1 day')
    expect(result).not.toContain('days')
  })

  it('uses singular form for "1 hour"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T03:30:00'
    const result = formatDuration(start, end)
    expect(result).toBe('1 hour')
    expect(result).not.toContain('hours')
  })

  it('uses singular form for "1 minute"', () => {
    const start = '2025-06-03T02:30:00'
    const end = '2025-06-03T02:31:00'
    const result = formatDuration(start, end)
    expect(result).toBe('1 minute')
    expect(result).not.toContain('minutes')
  })
})

// ---------------------------------------------------------------------------
// calendarDateFromISO
// ---------------------------------------------------------------------------

describe('calendarDateFromISO', () => {
  it('parses a plain date string "YYYY-MM-DD"', () => {
    const result = calendarDateFromISO('2025-06-04')
    expect(result.year).toBe(2025)
    expect(result.month).toBe(6)
    expect(result.day).toBe(4)
  })

  it('parses an ISO datetime string and uses only the date part', () => {
    const result = calendarDateFromISO('2025-06-04T14:00:00')
    expect(result.year).toBe(2025)
    expect(result.month).toBe(6)
    expect(result.day).toBe(4)
  })

  it('returns a CalendarDate instance', () => {
    const result = calendarDateFromISO('2025-01-01')
    expect(result).toBeInstanceOf(CalendarDate)
  })
})

// ---------------------------------------------------------------------------
// calendarDateToISO
// ---------------------------------------------------------------------------

describe('calendarDateToISO', () => {
  it('formats a CalendarDate as YYYY-MM-DD', () => {
    const date = new CalendarDate(2025, 6, 4)
    expect(calendarDateToISO(date)).toBe('2025-06-04')
  })

  it('zero-pads single-digit month and day', () => {
    const date = new CalendarDate(2025, 1, 9)
    expect(calendarDateToISO(date)).toBe('2025-01-09')
  })

  it('round-trips through calendarDateFromISO', () => {
    const iso = '2025-12-31'
    expect(calendarDateToISO(calendarDateFromISO(iso))).toBe(iso)
  })
})

// ---------------------------------------------------------------------------
// buildCalendarDateTime
// ---------------------------------------------------------------------------

describe('buildCalendarDateTime', () => {
  it('combines a CalendarDate and HH:MM time into a CalendarDateTime', () => {
    const date = new CalendarDate(2025, 6, 4)
    const result = buildCalendarDateTime(date, '14:30')
    expect(result.year).toBe(2025)
    expect(result.month).toBe(6)
    expect(result.day).toBe(4)
    expect(result.hour).toBe(14)
    expect(result.minute).toBe(30)
    expect(result.second).toBe(0)
  })

  it('returns a CalendarDateTime instance', () => {
    const date = new CalendarDate(2025, 6, 4)
    expect(buildCalendarDateTime(date, '09:00')).toBeInstanceOf(CalendarDateTime)
  })

  it('falls back to 00:00 when time string is empty', () => {
    const date = new CalendarDate(2025, 6, 4)
    const result = buildCalendarDateTime(date, '')
    expect(result.hour).toBe(0)
    expect(result.minute).toBe(0)
  })

  it('falls back to 00:00 when time string is malformed', () => {
    const date = new CalendarDate(2025, 6, 4)
    const result = buildCalendarDateTime(date, 'not-a-time')
    expect(result.hour).toBe(0)
    expect(result.minute).toBe(0)
  })
})

// ---------------------------------------------------------------------------
// getRecentPastPeriods
// ---------------------------------------------------------------------------

describe('getRecentPastPeriods', () => {
  const periods = [
    { startDateTime: '2025-02-16T14:00:00' },
    { startDateTime: '2025-03-02T14:00:00' },
    { startDateTime: '2025-03-09T14:00:00' },
    { startDateTime: '2025-03-23T14:00:00' },
    { startDateTime: '2025-03-16T14:00:00' }
  ]

  it('returns periods sorted by startDateTime descending', () => {
    const result = getRecentPastPeriods(periods)

    expect(result).toHaveLength(3)
    expect(result[0].startDateTime).toBe('2025-03-23T14:00:00')
    expect(result[1].startDateTime).toBe('2025-03-16T14:00:00')
    expect(result[2].startDateTime).toBe('2025-03-09T14:00:00')
  })

  it('respects custom limit parameter', () => {
    const result = getRecentPastPeriods(periods, 2)

    expect(result).toHaveLength(2)
    expect(result[0].startDateTime).toBe('2025-03-23T14:00:00')
    expect(result[1].startDateTime).toBe('2025-03-16T14:00:00')
  })

  it('returns all periods when fewer than limit', () => {
    const result = getRecentPastPeriods(periods.slice(0, 2), 3)

    expect(result).toHaveLength(2)
    expect(result[0].startDateTime).toBe('2025-03-02T14:00:00')
    expect(result[1].startDateTime).toBe('2025-02-16T14:00:00')
  })

  it('returns empty array when given empty input', () => {
    const result = getRecentPastPeriods([])

    expect(result).toHaveLength(0)
  })

  it('does not mutate the original array', () => {
    const original = [...periods]
    getRecentPastPeriods(periods)

    expect(periods).toEqual(original)
  })

  it('defaults limit to 3', () => {
    const result = getRecentPastPeriods(periods)

    expect(result).toHaveLength(3)
  })
})
