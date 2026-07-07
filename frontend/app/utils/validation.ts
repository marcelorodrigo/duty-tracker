import type { DateValue } from '@internationalized/date'
import { CalendarDate } from '@internationalized/date'
import { calendarDateToISO, buildCalendarDateTime } from '~/utils/dates'

const TIME_PATTERN = /^([01]\d|2[0-3]):([0-5]\d)$/

export function validateOnCallPeriodForm(
  start: DateValue | undefined,
  end: DateValue | undefined,
  startTime: string,
  endTime: string
): string | null {
  if (!start) return 'Please select a start date.'
  if (!end) return 'Please select an end date.'

  if (!startTime || !TIME_PATTERN.test(startTime)) {
    return 'Please enter a valid start time.'
  }
  if (!endTime || !TIME_PATTERN.test(endTime)) {
    return 'Please enter a valid end time.'
  }

  const startDT = buildCalendarDateTime(start as CalendarDate, startTime)
  const endDT = buildCalendarDateTime(end as CalendarDate, endTime)
  if (endDT.compare(startDT) <= 0) {
    return 'End date and time must be after start.'
  }

  return null
}

export function validateCustomHoliday(
  customDate: DateValue | undefined,
  rangeStart: DateValue | undefined,
  rangeEnd: DateValue | undefined,
  existingDates: string[]
): string | null {
  if (!customDate) {
    return 'Date is required.'
  }

  if (!rangeStart || !rangeEnd) {
    return 'Please select a period date range first.'
  }

  const date = customDate as CalendarDate
  const start = rangeStart as CalendarDate
  const end = rangeEnd as CalendarDate

  if (date.compare(start) < 0 || date.compare(end) > 0) {
    return 'Date must be within the on-call period.'
  }

  const dateISO = calendarDateToISO(date)
  if (existingDates.includes(dateISO)) {
    return 'A holiday on this date already exists.'
  }

  return null
}

export function extractTimeFromISO(isoString: string, fallback = '14:00'): string {
  if (!isoString.includes('T')) return fallback
  const timePart = isoString.split('T')[1] ?? ''
  return timePart.length >= 5 ? timePart.substring(0, 5) : fallback
}
