import type { HolidayInput, HolidaySuggestionItem } from '~/types/holiday'
import { calendarDateFromISO } from '~/utils/dates'

export function mergeHolidays(
  current: readonly HolidayInput[],
  newSuggestions: readonly HolidaySuggestionItem[],
  newStart: string,
  newEnd: string
): HolidayInput[] {
  const start = calendarDateFromISO(newStart)
  const end = calendarDateFromISO(newEnd)
  const filtered = current.filter((holiday) => {
    const date = calendarDateFromISO(holiday.date)
    return date.compare(start) >= 0 && date.compare(end) <= 0
  })
  const existingDates = new Set(filtered.map(holiday => holiday.date))

  for (const suggestion of newSuggestions) {
    if (!existingDates.has(suggestion.date)) {
      filtered.push({
        date: suggestion.date,
        name: suggestion.name ?? ''
      })
    }
  }

  return filtered.sort((first, second) => (
    calendarDateFromISO(first.date).compare(calendarDateFromISO(second.date))
  ))
}
