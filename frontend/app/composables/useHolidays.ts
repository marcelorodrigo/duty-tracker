import type { HolidayResponse } from '~/types/holiday'

export function useHolidays(periodId: number) {
  const config = useRuntimeConfig()
  const toast = useToast()

  const holidays = ref<HolidayResponse[]>([])
  const suggestions = ref<HolidayResponse[]>([])
  const pending = ref(false)
  const savePending = ref(false)
  const error = ref<Error | null>(null)

  async function fetchHolidays(): Promise<void> {
    pending.value = true
    error.value = null
    try {
      holidays.value = await $fetch<HolidayResponse[]>(`/api/v1/oncall-periods/${periodId}/holidays`, {
        baseURL: config.public.apiBase
      })
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to load holidays')
    } finally {
      pending.value = false
    }
  }

  async function fetchSuggestions(start: string, end: string): Promise<void> {
    pending.value = true
    error.value = null
    try {
      suggestions.value = await $fetch<HolidayResponse[]>('/api/v1/holidays/suggestions', {
        baseURL: config.public.apiBase,
        query: { start, end }
      })
    } catch (err) {
      error.value = err instanceof Error ? err : new Error('Failed to load holiday suggestions')
      suggestions.value = []
    } finally {
      pending.value = false
    }
  }

  async function saveHolidays(updated: HolidayResponse[]): Promise<void> {
    savePending.value = true
    try {
      holidays.value = await $fetch<HolidayResponse[]>(`/api/v1/oncall-periods/${periodId}/holidays`, {
        baseURL: config.public.apiBase,
        method: 'PUT',
        body: updated
      })
      toast.add({
        title: 'Holidays saved',
        color: 'success',
        icon: 'i-lucide-check'
      })
    } catch (err) {
      toast.add({
        title: 'Failed to save holidays',
        description: extractErrorDetail(err),
        color: 'error',
        icon: 'i-lucide-x'
      })
      throw err
    } finally {
      savePending.value = false
    }
  }

  return {
    holidays,
    suggestions,
    pending,
    savePending,
    error,
    fetchHolidays,
    fetchSuggestions,
    saveHolidays
  }
}
