import type { CompensationRateResponse, PivotRow, DayTypeCell } from '~/types/compensation'
import { formatTime } from '~/utils/dates'

export function buildPivotRows(rates: CompensationRateResponse[]): PivotRow[] {
  const allowanceRates = rates.filter(r => r.rateCategory === 'OVERTIME_ALLOWANCE')

  const map = new Map<string, {
    timeTo: string
    WEEKDAY?: DayTypeCell
    SATURDAY?: DayTypeCell
    SUNDAY_HOLIDAY?: DayTypeCell
  }>()

  for (const rate of allowanceRates) {
    if (!map.has(rate.timeFrom)) {
      map.set(rate.timeFrom, { timeTo: rate.timeTo })
    }
    const entry = map.get(rate.timeFrom)!
    entry[rate.overtimeDayType] = {
      id: rate.id,
      percentage: rate.percentage,
      label: rate.label
    }
  }

  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([timeFrom, entry]) => ({
      slot: `${formatTime(timeFrom)}–${formatTime(entry.timeTo)}`,
      timeFrom,
      weekday: entry.WEEKDAY!,
      saturday: entry.SATURDAY!,
      sundayHoliday: entry.SUNDAY_HOLIDAY!
    }))
}
