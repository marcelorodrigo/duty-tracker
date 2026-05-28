import type { CompensationRateResponse, PivotRow, DayTypeCell } from '~/types/compensation'

interface CompensationRateTableResponse {
  rates: CompensationRateResponse[]
}

function formatTime(time: string): string {
  return time.slice(0, 5)
}

function buildPivotRows(rates: CompensationRateResponse[]): PivotRow[] {
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

export function useCompensationRates() {
  const config = useRuntimeConfig()
  const toast = useToast()

  const { data, pending, error } = useFetch<CompensationRateTableResponse>(
    '/api/v1/compensation-rates',
    {
      baseURL: config.public.apiBase,
      timeout: 10_000
    }
  )

  const pivotRows = computed<PivotRow[]>(() => {
    if (!data.value?.rates) return []
    return buildPivotRows(data.value.rates)
  })

  async function updateRate(id: number, percentage: number): Promise<void> {
    const rates = data.value?.rates
    if (!rates) return

    const target = rates.find(r => r.id === id)
    if (!target) return

    const original = target.percentage
    target.percentage = percentage
    triggerRef(data)

    try {
      await $fetch(`/api/v1/compensation-rates/${id}`, {
        baseURL: config.public.apiBase,
        method: 'PUT',
        body: { rateId: id, percentage, label: target.label }
      })
      toast.add({
        title: 'Saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err: unknown) {
      target.percentage = original
      triggerRef(data)
      toast.add({
        title: 'Failed to save',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
    }
  }

  return { data, pivotRows, pending, error, updateRate }
}
