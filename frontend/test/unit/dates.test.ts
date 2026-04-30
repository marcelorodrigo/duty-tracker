import { describe, expect, it } from 'vitest'
import { formatTime, isActivePeriod, formatDate, formatDateTime } from '~/utils/dates'

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
  it('returns true when end date is in the future', () => {
    const futureDate = new Date()
    futureDate.setFullYear(futureDate.getFullYear() + 1)
    expect(isActivePeriod(futureDate.toISOString())).toBe(true)
  })

  it('returns true when end date is today', () => {
    const today = new Date()
    const iso = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}T14:00:00`
    expect(isActivePeriod(iso)).toBe(true)
  })

  it('returns false when end date is in the past', () => {
    expect(isActivePeriod('2020-01-01T14:00:00')).toBe(false)
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
