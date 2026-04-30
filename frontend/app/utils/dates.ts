/**
 * nextMondayAt14
 * Returns the next Monday at 14:00 local time.
 * - If today is Monday and time < 14:00, returns today at 14:00
 * - Otherwise returns next Monday at 14:00
 */
export function nextMondayAt14(from: Date = new Date()): Date {
  const date = new Date(from)
  const day = date.getDay() // 0=Sunday, 1=Monday, ..., 6=Saturday
  const daysUntilMonday = day === 1 ? 0 : (8 - day) % 7

  date.setDate(date.getDate() + daysUntilMonday)
  date.setHours(14, 0, 0, 0)

  // If today is Monday and we're already past 14:00, move to next Monday
  const minutesPast = from.getHours() * 60 + from.getMinutes()
  if (daysUntilMonday === 0 && minutesPast >= 14 * 60) {
    date.setDate(date.getDate() + 7)
  }

  return date
}

/**
 * followingMondayAt14
 * Returns the Monday 7 days after `from`, at 14:00:00 local time
 */
export function followingMondayAt14(from: Date): Date {
  const date = new Date(from)
  date.setDate(date.getDate() + 7)
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
 * Returns true if the date part of endDateTime >= today's date (in local time)
 */
export function isActivePeriod(endDateTime: string): boolean {
  const endDatePart = endDateTime.split('T')[0]
  const today = new Date()
  const todayPart = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  return (endDatePart ?? '') >= todayPart
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
