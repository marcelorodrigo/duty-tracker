import { describe, expect, it } from 'vitest'
import { CalendarDate } from '@internationalized/date'
import {
  validateOnCallPeriodForm,
  validateCustomHoliday,
  extractTimeFromISO
} from '~/utils/validation'

describe('validateOnCallPeriodForm', () => {
  const apr1 = new CalendarDate(2026, 4, 1)
  const apr30 = new CalendarDate(2026, 4, 30)

  it('returns error when start date is missing', () => {
    expect(validateOnCallPeriodForm(undefined, apr30, '14:00', '18:00'))
      .toBe('Please select a start date.')
  })

  it('returns error when end date is missing', () => {
    expect(validateOnCallPeriodForm(apr1, undefined, '14:00', '18:00'))
      .toBe('Please select an end date.')
  })

  it('returns error for an invalid start time format', () => {
    expect(validateOnCallPeriodForm(apr1, apr30, 'bad', '18:00'))
      .toBe('Please enter a valid start time.')
  })

  it('returns error for an empty start time', () => {
    expect(validateOnCallPeriodForm(apr1, apr30, '', '18:00'))
      .toBe('Please enter a valid start time.')
  })

  it('returns error for an invalid end time format', () => {
    expect(validateOnCallPeriodForm(apr1, apr30, '14:00', '25:00'))
      .toBe('Please enter a valid end time.')
  })

  it('returns error when end datetime is not after start (equal)', () => {
    expect(validateOnCallPeriodForm(apr1, apr1, '14:00', '14:00'))
      .toBe('End date and time must be after start.')
  })

  it('returns error when end datetime is before start', () => {
    expect(validateOnCallPeriodForm(apr30, apr1, '14:00', '14:00'))
      .toBe('End date and time must be after start.')
  })

  it('returns null when all inputs are valid', () => {
    expect(validateOnCallPeriodForm(apr1, apr30, '10:00', '18:00'))
      .toBeNull()
  })

  it('returns null for valid midnight times', () => {
    expect(validateOnCallPeriodForm(apr1, apr30, '00:00', '23:59'))
      .toBeNull()
  })
})

describe('validateCustomHoliday', () => {
  const apr1 = new CalendarDate(2026, 4, 1)
  const apr30 = new CalendarDate(2026, 4, 30)
  const apr15 = new CalendarDate(2026, 4, 15)

  it('returns error when no custom date is selected', () => {
    expect(validateCustomHoliday(undefined, apr1, apr30, []))
      .toBe('Date is required.')
  })

  it('returns error when period range start is missing', () => {
    expect(validateCustomHoliday(apr15, undefined, apr30, []))
      .toBe('Please select a period date range first.')
  })

  it('returns error when period range end is missing', () => {
    expect(validateCustomHoliday(apr15, apr1, undefined, []))
      .toBe('Please select a period date range first.')
  })

  it('returns error when date is before the period start', () => {
    const mar31 = new CalendarDate(2026, 3, 31)
    expect(validateCustomHoliday(mar31, apr1, apr30, []))
      .toBe('Date must be within the on-call period.')
  })

  it('returns error when date is after the period end', () => {
    const may1 = new CalendarDate(2026, 5, 1)
    expect(validateCustomHoliday(may1, apr1, apr30, []))
      .toBe('Date must be within the on-call period.')
  })

  it('returns error when a holiday on that date already exists', () => {
    expect(validateCustomHoliday(apr15, apr1, apr30, ['2026-04-15']))
      .toBe('A holiday on this date already exists.')
  })

  it('returns null when all conditions are met', () => {
    expect(validateCustomHoliday(apr15, apr1, apr30, []))
      .toBeNull()
  })

  it('returns null for boundary dates (start)', () => {
    expect(validateCustomHoliday(apr1, apr1, apr30, []))
      .toBeNull()
  })

  it('returns null for boundary dates (end)', () => {
    expect(validateCustomHoliday(apr30, apr1, apr30, []))
      .toBeNull()
  })

  it('clears any previous error before validation', () => {
    // Calling with valid input after a guarded case: the function
    // starts fresh each call — it's pure, no state to clear
    expect(validateCustomHoliday(apr15, apr1, apr30, [])).toBeNull()
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
})
