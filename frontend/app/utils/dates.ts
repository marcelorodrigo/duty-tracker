/**
 * currentWeekMondayAt14
 * Returns the Monday of the current week at 14:00 local time.
 * - Any day Mon–Sun: go back to the Monday that started this ISO week
 * - If today IS Monday and we are already at or past 14:00, advance to
 *   next Monday so the suggested start is always in the future
 */
import { CalendarDate, CalendarDateTime } from '@internationalized/date'

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
 * getPeriodStatus
 * Determines the status of an on-call period based on current time
 * @returns 'scheduled' if start is in the future, 'active' if currently ongoing, 'past' if ended
 */
export function getPeriodStatus(startDateTime: string, endDateTime: string): 'scheduled' | 'active' | 'past' {
  const startDate = new Date(startDateTime)
  const endDate = new Date(endDateTime)
  const now = new Date()

  if (now < startDate) {
    return 'scheduled'
  } else if (now < endDate) {
    return 'active'
  } else {
    return 'past'
  }
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
 * formatDateShort
 * Formats an ISO-8601 date string as "DD/MM/YYYY" (e.g. "02/06/2025")
 * Used for compact date display, e.g. in holiday lists
 */
export function formatDateShort(isoString: string): string {
  const date = new Date(isoString)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}/${month}/${year}`
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
 * Formats a time string as "HH:mm"
 * Accepts: "HH:mm:ss", "HH:mm", or ISO-8601 datetime "YYYY-MM-DDTHH:mm:ss"
 */
export function formatTime(timeString: string): string {
  // If it's an ISO-8601 datetime, extract the time portion after 'T'
  if (timeString.includes('T')) {
    const timePart = timeString.split('T')[1]
    return timePart?.substring(0, 5) || ''
  }
  // Otherwise, assume it's already a time string
  return timeString.substring(0, 5)
}

/**
 * formatDuration
 * Converts a duration in milliseconds to human-readable format
 * Examples:
 * - 60000 → "1 minute"
 * - 120000 → "2 minutes"
 * - 3600000 → "1 hour"
 * - 5400000 → "1 hour, 30 minutes"
 * - 86400000 → "1 day"
 * - 90000000 → "1 day, 1 hour"
 * - 93720000 → "1 day, 2 hours, 2 minutes"
 */
export function formatDuration(startISO: string, endISO: string): string {
  const startDate = new Date(startISO)
  const endDate = new Date(endISO)
  const durationMs = endDate.getTime() - startDate.getTime()

  if (durationMs <= 0) {
    return '0 minutes'
  }

  const totalSeconds = Math.floor(durationMs / 1000)
  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)

  const parts: string[] = []

  if (days > 0) {
    parts.push(`${days} ${days === 1 ? 'day' : 'days'}`)
  }
  if (hours > 0) {
    parts.push(`${hours} ${hours === 1 ? 'hour' : 'hours'}`)
  }
  if (minutes > 0) {
    parts.push(`${minutes} ${minutes === 1 ? 'minute' : 'minutes'}`)
  }

  if (parts.length === 0) {
    return '0 minutes'
  }

  if (parts.length === 1) {
    return parts[0]!
  }

  if (parts.length === 2) {
    return `${parts[0]!}, ${parts[1]!}`
  }

  // parts.length === 3: "X days, Y hours and Z minutes"
  return `${parts.slice(0, -1).join(', ')} and ${parts.at(-1)!}`
}

/**
 * calendarDateFromISO
 * Parses an ISO-8601 date string ("YYYY-MM-DD" or "YYYY-MM-DDTHH:mm:ss") into a CalendarDate.
 * Only the date part is used; time is ignored.
 */
export function calendarDateFromISO(iso: string): CalendarDate {
  const datePart = iso.includes('T') ? iso.split('T')[0]! : iso
  const [year, month, day] = datePart.split('-').map(Number)
  return new CalendarDate(year!, month!, day!)
}

/**
 * calendarDateToISO
 * Serialises a CalendarDate to an ISO-8601 date string ("YYYY-MM-DD").
 */
export function calendarDateToISO(date: CalendarDate): string {
  const month = String(date.month).padStart(2, '0')
  const day = String(date.day).padStart(2, '0')
  return `${date.year}-${month}-${day}`
}

/**
 * buildCalendarDateTime
 * Combines a CalendarDate and an HH:MM time string into a CalendarDateTime.
 * Falls back to 00:00 when the time string is missing or malformed.
 */
export function buildCalendarDateTime(date: CalendarDate, time: string): CalendarDateTime {
  const [hourStr, minuteStr] = (time ?? '').split(':')
  const hour = Number.parseInt(hourStr ?? '0', 10)
  const minute = Number.parseInt(minuteStr ?? '0', 10)
  const safeHour = Number.isNaN(hour) ? 0 : hour
  const safeMinute = Number.isNaN(minute) ? 0 : minute
  return new CalendarDateTime(date.year, date.month, date.day, safeHour, safeMinute, 0)
}

/**
 * getRecentPastPeriods
 * Sorts periods by startDateTime descending and returns the first `limit` items.
 */
export function getRecentPastPeriods<T extends { startDateTime: string }>(periods: T[], limit = 3): T[] {
  const normalizedLimit = Math.max(0, Math.floor(limit))
  return [...periods]
    .sort((a, b) => new Date(b.startDateTime).getTime() - new Date(a.startDateTime).getTime())
    .slice(0, normalizedLimit)
}

/**
 * getStatusColors
 * Returns CSS class strings for status badge and dot based on period status
 */
export function getStatusColors(status: 'scheduled' | 'active' | 'past') {
  switch (status) {
    case 'active':
      return {
        badge: 'bg-(--ui-color-success-50) text-(--ui-color-success-600) dark:bg-(--ui-color-success-950) dark:text-(--ui-color-success-400)',
        dot: 'bg-(--ui-color-success-600)'
      }
    case 'scheduled':
      return {
        badge: 'bg-(--ui-color-primary-50) text-(--ui-color-primary-500) dark:bg-(--ui-color-primary-950) dark:text-(--ui-color-primary-400)',
        dot: 'bg-(--ui-color-primary-500)'
      }
    case 'past':
      return {
        badge: 'bg-(--ui-bg-elevated) text-(--ui-text-muted)',
        dot: 'bg-(--ui-text-dimmed)'
      }
  }
}
