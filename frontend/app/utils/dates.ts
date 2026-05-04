/**
 * currentWeekMondayAt14
 * Returns the Monday of the current week at 14:00 local time.
 * - Any day Mon–Sun: go back to the Monday that started this ISO week
 * - If today IS Monday and we are already at or past 14:00, advance to
 *   next Monday so the suggested start is always in the future
 */
export function currentWeekMondayAt14(from: Date = new Date()): Date {
  const date = new Date(from)
  const dayOfWeek = date.getDay() // 0=Sunday, 1=Monday, …, 6=Saturday

  // ISO week starts on Monday; Sunday (0) is treated as day 7
  const daysSinceMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
  date.setDate(date.getDate() - daysSinceMonday)
  date.setHours(14, 0, 0, 0)

  // If we landed on today's Monday but 14:00 has already passed, the
  // current week's slot is gone — move to next Monday instead
  const alreadyPast14 = from.getHours() * 60 + from.getMinutes() >= 14 * 60
  if (daysSinceMonday === 0 && alreadyPast14) {
    date.setDate(date.getDate() + 7)
  }

  return date
}

/**
 * nextWeekMondayAt14
 * Returns the Monday exactly one week after `from`, at 14:00 local time.
 * Intended as the suggested end date when `from` is the start of an on-call period.
 */
export function nextWeekMondayAt14(from: Date): Date {
  const date = new Date(from)
  date.setDate(date.getDate() + 7)
  date.setHours(14, 0, 0, 0)
  return date
}

/**
 * toDatetimeLocal
 * Formats a Date as "YYYY-MM-DDTHH:mm" for use in datetime-local inputs
 * @deprecated Use toCalendarDateTime instead for UInputDate component
 */
export function toDatetimeLocal(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

/**
 * fromDatetimeLocal
 * Parses a datetime-local string and returns ISO-8601 "YYYY-MM-DDTHH:mm:ss"
 * @deprecated Use fromCalendarDateTime instead for UInputDate component
 */
export function fromDatetimeLocal(value: string): string {
  return `${value}:00`
}

import { CalendarDateTime } from '@internationalized/date'

/**
 * toCalendarDateTime
 * Converts a Date to a CalendarDateTime instance for UInputDate component
 */
export function toCalendarDateTime(date: Date): CalendarDateTime {
  return new CalendarDateTime(
    date.getFullYear(),
    date.getMonth() + 1,
    date.getDate(),
    date.getHours(),
    date.getMinutes(),
    0
  )
}

/**
 * fromCalendarDateTime
 * Converts a CalendarDateTime to ISO-8601 "YYYY-MM-DDTHH:mm:ss" for API submission
 */
export function fromCalendarDateTime(calendarDateTime: CalendarDateTime): string {
  const year = calendarDateTime.year
  const month = String(calendarDateTime.month).padStart(2, '0')
  const day = String(calendarDateTime.day).padStart(2, '0')
  const hours = String(calendarDateTime.hour).padStart(2, '0')
  const minutes = String(calendarDateTime.minute).padStart(2, '0')
  const seconds = String(calendarDateTime.second).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

/**
 * isActivePeriod
 * Returns true if the current time is before the endDateTime (in local time)
 */
export function isActivePeriod(endDateTime: string): boolean {
  const endDate = new Date(endDateTime)
  const now = new Date()
  return now < endDate
}

/**
 * formatDate
 * Formats an ISO-8601 date string as "DD MMM YYYY" (e.g. "02 Jun 2025")
 */
export function formatDate(isoString: string): string {
  const date = new Date(isoString)
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  const day = String(date.getDate()).padStart(2, '0')
  const month = months[date.getMonth()]
  const year = date.getFullYear()
  return `${day} ${month} ${year}`
}

/**
 * formatDateTime
 * Formats an ISO-8601 datetime string as "DD MMM YYYY HH:mm"
 */
export function formatDateTime(isoString: string): string {
  const date = new Date(isoString)
  const dateStr = formatDate(isoString)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${dateStr} ${hours}:${minutes}`
}

/**
 * formatTime
 * Formats a LocalTime string "HH:mm:ss" or "HH:mm" as "HH:mm"
 */
export function formatTime(timeString: string): string {
  return timeString.substring(0, 5)
}
