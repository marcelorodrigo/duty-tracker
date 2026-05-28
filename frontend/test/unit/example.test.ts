import { describe, expect, it } from 'vitest'
import { formatDate, formatDuration } from '~/utils/dates'

describe('formatDate', () => {
  it('formats an ISO date string as DD MMM YYYY', () => {
    expect(formatDate('2025-06-02T00:00:00')).toBe('02 Jun 2025')
  })
})

describe('formatDuration', () => {
  it('returns 0 minutes for equal start and end', () => {
    expect(formatDuration('2025-06-02T10:00:00', '2025-06-02T10:00:00')).toBe('0 minutes')
  })

  it('formats a duration spanning days, hours and minutes', () => {
    expect(formatDuration('2025-06-02T08:00:00', '2025-06-03T10:30:00')).toBe('1 day, 2 hours and 30 minutes')
  })
})
