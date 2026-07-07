import type { CompensationRateResponse, PivotRow, DayTypeCell } from '~/types/compensation'
import { formatTime } from '~/utils/dates'

function timeToMinutes(time: string): number {
  const [h, m] = time.split(':')
  return Number(h ?? 0) * 60 + Number(m ?? 0)
}

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
    .sort(([a], [b]) => timeToMinutes(a) - timeToMinutes(b))
    .map(([timeFrom, entry]) => ({
      slot: `${formatTime(timeFrom)}–${formatTime(entry.timeTo)}`,
      timeFrom,
      weekday: entry.WEEKDAY!,
      saturday: entry.SATURDAY!,
      sundayHoliday: entry.SUNDAY_HOLIDAY!
    }))
}
